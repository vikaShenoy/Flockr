package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;
import models.PersonalPhoto;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import repository.PhotoRepository;
import repository.UserRepository;
import util.PhotoUtil;
import util.Security;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PhotoController extends Controller {

    private final Security security;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final PhotoUtil photoUtil;
    private final HttpExecutionContext httpExecutionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public PhotoController(Security security, PhotoRepository photoRepository, UserRepository userRepository, PhotoUtil photoUtil, HttpExecutionContext httpExecutionContext) {
        this.security = security;
        this.photoRepository = photoRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.userRepository = userRepository;
        this.photoUtil = photoUtil;
    }

    /**
     * This function is responsible for changing the permissions of a photo to either a private or a public.
     *
     * @param photoId the id of the photo to be changed
     * @param request the Http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updatePermission(int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        JsonNode requestBody = request.body().asJson();
        ObjectNode response = Json.newObject();

        // If the request body does not contain is public key
        if (!requestBody.has("isPublic") || !requestBody.has("isPrimary")) {
            response.put("message", "Please specify the JSON body as specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // If the JSON is empty
        if (requestBody.size() == 0) {
            response.put("message", "The request body is empty. Please specify the JSON body as " +
                    "specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        boolean isPublic = requestBody.get("isPublic").asBoolean();
        boolean isPrimary = requestBody.get("isPrimary").asBoolean();
        return photoRepository.getPhotoById(photoId)
                .thenApplyAsync(optionalPhoto -> {
                    // If the photo with the given photo id does not exists
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    // Checks that the user is either the admin or the owner of the photo to change permission groups
                    if (!user.isAdmin() && user.getUserId() != optionalPhoto.get().getUser().getUserId()) {
                        throw new CompletionException(new ForbiddenRequestException("You're not allowed to change the permission group."));
                    }

                    PersonalPhoto photo = optionalPhoto.get();
                    photo.setPublic(isPublic);
                    photo.setPrimary(isPrimary);
                    return photoRepository.updatePhoto(photo);
                }).thenApplyAsync(PersonalPhoto -> ok("Successfully updated permission groups"))
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundException) {
                        response.put("message", "Could not find a photo with the given photo ID");
                        return notFound(response);
                    } catch (ForbiddenRequestException forbiddenException) {
                        response.put("message", "You are unauthorised to change the photo permission of the photo");
                        return forbidden(response);
                    } catch (Throwable serverException) {
                        response.put("message", "Endpoint under development");
                        return internalServerError(response);
                    }
                });
    }

    /**
     * Endpoint controller method for undoing a personal photo deletion.
     * return status codes are as follows:
     * - 200 - OK - successful undo.
     * - 400 - Bad Request - The photo has not been deleted.
     * - 401 - Unauthorised - the user is not authorised.
     * - 403 - Forbidden - The user does not have permission to undo this deletion.
     * - 404 - Not Found - The photo cannot be found.
     *
     * @param photoId the id of the photo.
     * @param request the http request.
     * @return the completion stage containing the result.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> undoDelete(int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return photoRepository.getPhotoByIdWithSoftDelete(photoId)
                .thenComposeAsync(optionalPhoto -> {
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException("Photo not found"));
                    }
                    PersonalPhoto photo = optionalPhoto.get();
                    if (!user.isAdmin() && user.getUserId() != photo.getOwnerId()) {
                        throw new CompletionException(new ForbiddenRequestException(
                                "You do not have permission to undo this deletion"));
                    }
                    if (!photo.isDeleted()) {
                        throw new CompletionException(new BadRequestException("This photo has not been deleted"));
                    }
                    return photoRepository.undoPhotoDelete(photo);
                })
                .thenApplyAsync(photo -> ok(Json.toJson(photo)))
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (BadRequestException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", error.getMessage());
                        return badRequest(message);
                    } catch (NotFoundException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", error.getMessage());
                        return notFound(message);
                    } catch (ForbiddenRequestException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", error.getMessage());
                        return forbidden(message);
                    } catch (Throwable throwable) {
                        log.error("500 - Internal Server Error", throwable);
                        return internalServerError();
                    }
                });
    }

    /**
     * This function is responsible for deleting the photo with the given ID
     *
     * @param photoId       the id of the photo to be deleted
     * @param request       the Http request sent
     * @return              a Play result
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deletePhoto(int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return photoRepository.getPhotoById(photoId)
                .thenComposeAsync((optionalPhoto) -> {
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    PersonalPhoto photo = optionalPhoto.get();
                    if (user.getUserId() != photo.getUser().getUserId() && !user.isAdmin()) {
                        throw new CompletionException(new ForbiddenRequestException("You are not permitted to do that"));
                    }
                    return this.photoRepository.deletePhoto(photo);
                })
                .thenApplyAsync(photo -> (Result) ok())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "The photo with the given id is not found");
                        return notFound(message);
                    } catch (ForbiddenRequestException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", error.getMessage());
                        return forbidden(message);
                    } catch (Throwable serverError) {
                        return internalServerError();
                    }
                });
    }

    /**
     * Gets all photos of a given user using the userId as a reference.
     *
     * @param userId  the id of the user
     * @param request the http request
     * @return HTTP response which can be
     * - 200 - with a list of photos if successful
     * - 401 - if the user is not authorized
     * - 403 - if the user has not completed their profile
     * - 404 - if the user does not exist
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhotos(int userId, Http.Request request) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);
        // Check user exists
        if (User.find.byId(userId) == null) {
            JsonNode response = Json.newObject().put("error", "Not Found");
            return supplyAsync(() -> notFound(response));
        } else {
            return photoRepository.getPhotosById(userId)
                    .thenApplyAsync((photos) -> {
                        List<PersonalPhoto> userPhotos = photos.stream().filter(photo -> {
                            // Don't add primary photo to list of photos
                            if (photo.isPrimary()) {
                                return false;
                            }

                            // If user is admin, then display even private photos
                            if (userFromMiddleware.isAdmin()) {
                                return true;
                            }

                            // Don't add private photo's if user not the logged in user
                            return !(userFromMiddleware.getUserId() != userId && !photo.isPublic());
                        })
                                .collect(Collectors.toList());

                        JsonNode photosAsJSON = Json.toJson(userPhotos);
                        return ok(photosAsJSON);
                    });
        }
    }

    /**
     * Gets a specific photo
     *
     * @param photoId the id of the photo to retrieve
     * @param request HTTP request object
     * @return binary photo data with status 200 if found
     * unauthorized with 401 if not authorized
     * notFound with 404 if photo not found
     * forbidden 403 if trying to get a photo that you do not have permission to
     * 500 server error for any other server related error
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhoto(int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return photoRepository.getPhotoById(photoId)

                .thenApplyAsync(photo -> {
                    if (!photo.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    } else if (!user.isAdmin() && !photo.get().isPublic() && user.getUserId() != photo.get().getUser().getUserId()) {
                       return forbidden();
                    }
                    else {
                        String path = System.getProperty("user.dir") + "/storage/photos";
                        photo.get().getFilenameHash();
                        File photoToBeSent = new File(path, photo.get().getFilenameHash());
                        if (!photoToBeSent.exists()) {
                            // here for the last of sprint 4 where we can't seem to access photos
                            // but we can access the thumbnails
                            ObjectNode res = Json.newObject();
                            String messageKey = "message";
                            res.put(messageKey, "Did not find the photo..." + photoToBeSent);
                            return internalServerError(res);
                        }
                        return ok().sendFile(photoToBeSent);
                    }
                }).exceptionally(error -> {
                    try {
                        throw error.getCause();
                    } catch (NotFoundException notFoundException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "Please provide a valid request body according to the API spec");
                        return notFound(message);
                    } catch (Throwable throwable) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "Something went wrong while retrieving your image.");
                        return internalServerError(message);
                    }
                });
    }

    /**
     * Determine whether the user doing an upload can upload a photo for receiving user
     *
     * @param uploadingUserId the id of the user doing the upload
     * @param receivingUserId the if of the user for which the uploaded photo is
     * @return true (wrapped in a completion stage) if upload is allowed, false (wrapped in a
     * completion stage) otherwise
     */
    private CompletionStage<Boolean> canUserUploadPhoto(int uploadingUserId, int receivingUserId) {
        return userRepository.getUserById(uploadingUserId)
                .thenCombineAsync(userRepository.getUserById(receivingUserId), (optionalUploadingUser, optionalReceivingUser) -> {
                    // if either user does not exist, we can't really upload
                    if (!optionalUploadingUser.isPresent() || !optionalReceivingUser.isPresent()) {
                        return false;
                    }

                    User uploadingUser = optionalUploadingUser.get();
                    User receivingUser = optionalReceivingUser.get();

                    if (uploadingUser.isAdmin() || uploadingUser.isDefaultAdmin()) {
                        return true;
                    }

                    return uploadingUser.equals(receivingUser);
                }, httpExecutionContext.current());
    }

    /**
     * Upload a new photo for a user
     *
     * @param userId  the id of the user we are trying to add the photo for
     * @param request the HTTP request requesting the upload
     * @return the result for the request
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> uploadPhotoForUser(int userId, Http.Request request) {
        User userUploadingPhoto = request.attrs().get(ActionState.USER);
        ObjectNode response = Json.newObject();
        String messageKey = "message";

        return canUserUploadPhoto(userUploadingPhoto.getUserId(), userId)
                .thenComposeAsync(canUploadPhoto -> {
                    if (!canUploadPhoto) {
                        String message = String.format("User %d is not allowed to upload a photo for user %d", userUploadingPhoto.getUserId(), userId);
                        throw new CompletionException(new ForbiddenRequestException(message));
                    }

                    Optional<String> optionalContentType = request.contentType();

                    // check that a content type is specified
                    if (!optionalContentType.isPresent()) {
                        response.put(messageKey, "Please specify the Content Type as specified in the API spec");
                        return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
                    }

                    String contentType = optionalContentType.get();

                    // check that the content type is allowed
                    if (!contentType.equals("multipart/form-data")) {
                        response.put(messageKey, "Please specify the Content Type as specified in the API spec");
                        return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
                    }

                    // parse request body to multipart form data
                    Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
                    Http.MultipartFormData.FilePart photo = multipartFormData.getFile("image");
                    Map<String, String[]> textFields = multipartFormData.asFormUrlEncoded();

                    // check that the body includes fields as specified in the API spec
                    if (!(textFields.containsKey("isPrimary") || textFields.containsKey("isPublic"))) {
                        response.put(messageKey, "Please provide the request body as specified in the API spec");
                        return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
                    }

                    // check that the body includes the photo
                    if (photo == null) {
                        response.put(messageKey, "Missing photo in the request body");
                        return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
                    }

                    // check for the photo's content type
                    String photoContentType = photo.getContentType();
                    if (!(photoContentType.equals("image/png") || photoContentType.equals("image/jpeg"))) {
                        response.put(messageKey, "Your photo must be a .png or .jpg file");
                        return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
                    }

                    // initialise the text components of the request
                    String isPrimaryAsText = textFields.get("isPrimary")[0];
                    String isPublicAsText = textFields.get("isPublic")[0];
                    boolean isPrimary = isPrimaryAsText.equals("true");
                    boolean isPublic = isPublicAsText.equals("true");

                    // A photo cannot be primary and private as other users need to see the profile photo
                    if (isPrimary && !isPublic) {
                        response.put(messageKey, "A photo cannot be primary and private");
                        return supplyAsync(() -> forbidden(response), httpExecutionContext.current());
                    }

                    log.debug("Extracting photo from request");

                    // get the photo as a file from the request
                    Files.TemporaryFile temporaryPhotoFile = (Files.TemporaryFile) photo.getRef();

                    checkForAndCreatePhotosDirectory();

                    // start copying the file to file system
                    String path = System.getProperty("user.dir") + "/storage/photos";
                    Security security = new Security();
                    String token = security.generateToken();
                    String extension = photoContentType.equals("image/png") ? ".png" : ".jpg";
                    String filename = token + extension;
                    String thumbFilename = token + "_thumb" + extension;
                    File fileDestination = new File(path, filename);
                    File thumbFileDestination = new File(path, thumbFilename);

                    // if the file path already exists, generate another token
                    while (fileDestination.exists()) {
                        token = security.generateToken();
                        filename = token + extension;
                        thumbFilename = token + "_thumb" + extension;
                        fileDestination = new File(path, filename);
                        thumbFileDestination = new File(path, thumbFilename);
                    }

                    // save to filesystem
                    temporaryPhotoFile.moveFileTo(fileDestination);
                    log.info("Saved file to filesystem: {}", temporaryPhotoFile);

                    // resize and save a thumbnail.
                    try {
                        log.info("Saving thumbnail of photo {} as {}", fileDestination, thumbFileDestination);
                        saveThumbnail(fileDestination, thumbFileDestination, contentType);
                        log.info("Thumbnail {} created successfully", thumbFileDestination);
                    } catch (IOException e) {
                        log.error("Internal Server Error when generating a thumbnail", e);
                        return supplyAsync(Results::internalServerError, httpExecutionContext.current());
                    }
                    // save to filesystem
                    temporaryPhotoFile.moveFileTo(fileDestination);
                    // resize and save a thumbnail.
                    try {
                        saveThumbnail(fileDestination, thumbFileDestination, photoContentType.split("/")[1]);
                    } catch (IOException e) {
                        return supplyAsync(Results::internalServerError, httpExecutionContext.current());
                    }


                    // create photo model in database
                    final String usedFilename = filename;
                    String finalThumbFilename = thumbFilename;
                    return userRepository.getUserById(userId)
                            .thenComposeAsync(optionalReceivingUser -> {
                                if (!optionalReceivingUser.isPresent()) {
                                    response.put(messageKey, String.format("User %d does not exist", userId));
                                    return supplyAsync(() -> notFound(response));
                                }

                                User receivingUser = optionalReceivingUser.get();
                                PersonalPhoto personalPhoto = new PersonalPhoto(usedFilename, isPublic, receivingUser, isPrimary, finalThumbFilename);
                                return photoRepository.insert(personalPhoto)
                                        .thenApplyAsync((insertedPhoto) -> {
                                            if (isPrimary) {
                                                // If old profile photo exists, delete thumbnail and photo of old profile photo
                                                if (receivingUser.getProfilePhoto() != null) {
                                                    PersonalPhoto oldProfilePhoto = receivingUser.getProfilePhoto();
                                                    String profilePicturePath = System.getProperty("user.dir") + "/storage/photos";
                                                    File photoFile = new File(profilePicturePath, oldProfilePhoto.getFilenameHash());
                                                    File thumbnailFile = new File(profilePicturePath, oldProfilePhoto.getThumbnailName());

                                                    if (!photoFile.delete() || !thumbnailFile.delete()) {
                                                        String photoErrorMessage = "Error deleting old profile photo and thumbnail";
                                                        System.err.println(photoErrorMessage);
                                                        throw new CompletionException(new FileNotFoundException(photoErrorMessage));
                                                    }
                                                }

                                                receivingUser.setProfilePhoto(insertedPhoto);
                                                receivingUser.save();
                                            }


                                            return created(Json.toJson(insertedPhoto));
                                        });
                            });
                }, httpExecutionContext.current())
                .exceptionally(error -> {
                    try {
                        throw error.getCause();
                    } catch (ForbiddenRequestException forbiddenRequest) {
                        response.put(messageKey, forbiddenRequest.getMessage());
                        return forbidden(response);
                    } catch (Throwable throwable) {
                        System.err.println("Unexpected error when uploading a photo: " + Arrays.toString(throwable.getStackTrace()));
                        response.put(messageKey, "Something went wrong trying to upload the photo");
                        return internalServerError(response);
                    }
                });
    }

    /**
     * Checks if the photo directories are created and creates them if not.
     *
     * @return returns true only if both the storage and storage/photos are created, false otherwise.
     */
    private boolean checkForAndCreatePhotosDirectory() {
        String path = System.getProperty("user.dir") + "/storage";
        File file = new File(path);
        if (!file.exists() && file.mkdir()) {
            file = new File(path + "/photos");
            if (!file.exists()) {
                return file.mkdir();
            }
        }
        return false;
    }

    /**
     * Returns the thumbnail of a given photo.
     *
     * @param photoId the id of the photo of which the thumbnail will be returned.
     * @param request the Http request.
     * @return a Http response with one of the following:
     * - 200 - with the photo data in the body.
     * - 401 - when the user is not authenticated.
     * - 403 - when the user has not completed their profile.
     * - 404 - when the photo does not exist.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getThumbnail(int photoId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return photoRepository.getPhotoById(photoId)
                .thenApplyAsync(photo -> {
                    if (!photo.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    } else if (!user.isAdmin() && !photo.get().isPublic() && user.getUserId() != photo.get().getUser().getUserId()) {
                        return forbidden();
                    } else {
                        int índiceDePunto = photo.get().getFilenameHash().lastIndexOf('.');
                        String fileType = photo.get().getFilenameHash().substring(índiceDePunto);
                        String filename = photo.get().getFilenameHash().substring(0, índiceDePunto);
                        String path = System.getProperty("user.dir") + "/storage/photos";
                        filename += "_thumb" + fileType;
                        return ok().sendFile(new File(path, filename));
                    }
                }).exceptionally(error -> {
                    try {
                        throw error.getCause();
                    } catch (NotFoundException notFoundException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "Please provide a valid request body according to the API spec");
                        return notFound(message);
                    } catch (Throwable throwable) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "Something went wrong while retrieving your image.");
                        return internalServerError(message);
                    }
                });
    }

    /**
     * Saves a thumbnail of the given image file in the given destination.
     *
     * @param originalImage        the file containing the original image.
     * @param thumbFileDestination the file containing the destination path and filename.
     * @param photoContentType     the type of image.
     * @throws IOException thrown when the thumbnail cannot be written to disk.
     */
    private void saveThumbnail(File originalImage, File thumbFileDestination, String photoContentType) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(originalImage);
        ImageIO.write(bufferedImage, photoContentType, originalImage);

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int midWidth = bufferedImage.getWidth() / 2;
        int midHeight = bufferedImage.getHeight() / 2;
        Image img;
        if (midWidth > 150 && midHeight > 150) {
            if (midHeight > midWidth) {
                img = bufferedImage.getSubimage(0, midHeight - midWidth, width, width)
                        .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
            } else {
                img = bufferedImage.getSubimage(midWidth - midHeight, 0, height, height)
                        .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
            }
        } else if (midWidth > midHeight) {
            img = bufferedImage.getSubimage(midWidth - midHeight, 0, height, height)
                    .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
        } else {
            img = bufferedImage.getSubimage(0, midHeight - midWidth, width, width)
                    .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
        }
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        image.createGraphics().drawImage(img, 0, 0, null);
        ImageIO.write(image, photoContentType, thumbFileDestination);

    }
}
