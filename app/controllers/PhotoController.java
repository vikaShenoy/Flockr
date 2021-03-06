package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import exceptions.ServerErrorException;
import java.nio.file.StandardCopyOption;
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
import util.ExceptionUtil;
import util.Security;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Controller to handle photo related endpoints.
 */
public class PhotoController extends Controller {

  private static final String USER_DIR = "user.dir";
  private static final String IS_PUBLIC_KEY = "isPublic";
  private static final String MESSAGE_KEY = "message";
  private static final String IS_PRIMARY_KEY = "isPrimary";
  private static final String STORAGE_PHOTOS = "/storage/photos";
  private static final String PHOTO_NOT_FOUND_MESSAGE = "Photo not found";
  private static final String THUMB = "_thumb";
  private static final String USER_DOES_NOT_HAVE_PERMISSION_TO_PERFORM_THIS_REQUEST = "User does not have permission to perform this request";

  private final PhotoRepository photoRepository;
  private final UserRepository userRepository;
  private final HttpExecutionContext httpExecutionContext;
  private final ExceptionUtil exceptionUtil;

  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Inject
  public PhotoController(
      PhotoRepository photoRepository,
      UserRepository userRepository,
      HttpExecutionContext httpExecutionContext,
      ExceptionUtil exceptionUtil) {
    this.photoRepository = photoRepository;
    this.httpExecutionContext = httpExecutionContext;
    this.userRepository = userRepository;
    this.exceptionUtil = exceptionUtil;
  }

  /**
   * Gets the default cover photo.
   *
   * @return the response with the default cover photo. If not logged in responds with 401 -
   *     Unauthorized. If file is missing responds with an internal server error.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getDefaultCoverPhoto() {
    return supplyAsync(
        () -> {
          String path = System.getProperty(USER_DIR) + "/storage/defaults";

          File photoToBeSent = new File(path, "defaultCoverPhoto.jpg");

          if (!photoToBeSent.exists()) {
            return internalServerError();
          }
          return ok().sendFile(photoToBeSent);
        });
  }

  /**
   * This function is responsible for changing the permissions of a photo to either a private or a
   * public.
   *
   * @param photoId the id of the photo to be changed
   * @param request the Http request
   * @return a completion stage and a status code 200 if the request is successful, otherwise
   *     returns 500.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> updatePermission(int photoId, Http.Request request) {
    User user = request.attrs().get(ActionState.USER);
    JsonNode requestBody = request.body().asJson();
    ObjectNode response = Json.newObject();

    // If the request body does not contain is public key
    if (!requestBody.has(IS_PUBLIC_KEY) || !requestBody.has(IS_PRIMARY_KEY)) {
      response.put(MESSAGE_KEY, "Please specify the JSON body as specified in the API spec");
      return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
    }

    // If the JSON is empty
    if (requestBody.size() == 0) {
      response.put(
              MESSAGE_KEY,
          "The request body is empty. Please specify the JSON body as "
              + "specified in the API spec");
      return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
    }

    boolean isPublic = requestBody.get(IS_PUBLIC_KEY).asBoolean();
    boolean isPrimary = requestBody.get(IS_PRIMARY_KEY).asBoolean();
    return photoRepository
        .getPhotoById(photoId)
        .thenApplyAsync(
            optionalPhoto -> {
              // If the photo with the given photo id does not exists
              if (!optionalPhoto.isPresent()) {
                throw new CompletionException(
                    new NotFoundException("Could not find a photo with the given photo ID"));
              }

              // Checks that the user is either the admin or the owner of the photo to change
              // permission groups
              if (!user.isAdmin()
                  && user.getUserId() != optionalPhoto.get().getUser().getUserId()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have permission to change the photo permission of the photo"));
              }

              PersonalPhoto photo = optionalPhoto.get();
              photo.setPublic(isPublic);
              photo.setPrimary(isPrimary);
              return photoRepository.updatePhoto(photo);
            })
        .thenApplyAsync(personalPhoto -> ok("Successfully updated permission groups"))
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Endpoint controller method for undoing a personal photo deletion. return status codes are as
   * follows: - 200 - OK - successful undo. - 400 - Bad Request - The photo has not been deleted. -
   * 401 - Unauthorised - the user is not authorised. - 403 - Forbidden - The user does not have
   * permission to undo this deletion. - 404 - Not Found - The photo cannot be found.
   *
   * @param photoId the id of the photo.
   * @param request the http request.
   * @return the completion stage containing the result.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> undoDelete(int photoId, Http.Request request) {
    User user = request.attrs().get(ActionState.USER);
    return photoRepository
        .getPhotoByIdWithSoftDelete(photoId)
        .thenComposeAsync(
            optionalPhoto -> {
              if (!optionalPhoto.isPresent()) {
                throw new CompletionException(new NotFoundException(PHOTO_NOT_FOUND_MESSAGE));
              }
              PersonalPhoto photo = optionalPhoto.get();
              if (!user.isAdmin() && user.getUserId() != photo.getOwnerId()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have permission to undo this deletion"));
              }
              if (!photo.isDeleted()) {
                throw new CompletionException(
                    new BadRequestException("This photo has not been deleted"));
              }
              return photoRepository.undoPhotoDelete(photo);
            })
        .thenApplyAsync(photo -> ok(Json.toJson(photo)))
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * This function is responsible for deleting the photo with the given ID
   *
   * @param photoId the id of the photo to be deleted
   * @param request the Http request sent
   * @return a Play result
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> deletePhoto(int photoId, Http.Request request) {
    User user = request.attrs().get(ActionState.USER);
    return photoRepository
        .getPhotoById(photoId)
        .thenComposeAsync(
            optionalPhoto -> {
              if (!optionalPhoto.isPresent()) {
                throw new CompletionException(
                    new NotFoundException("The photo with the given id is not found"));
              }
              PersonalPhoto photo = optionalPhoto.get();
              if (user.getUserId() != photo.getUser().getUserId() && !user.isAdmin()) {
                throw new CompletionException(
                    new ForbiddenRequestException("You are not permitted to do that"));
              }
              return this.photoRepository.deletePhoto(photo);
            })
        .thenApplyAsync(photo -> (Result) ok())
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Gets all photos of a given user using the userId as a reference.
   *
   * @param userId the id of the user
   * @param request the http request
   * @return HTTP response which can be - 200 - with a list of photos if successful - 401 - if the
   *     user is not authorized - 403 - if the user has not completed their profile - 404 - if the
   *     user does not exist
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getPhotos(int userId, Http.Request request) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    // Check user exists
    if (User.find.byId(userId) == null) {
      JsonNode response = Json.newObject().put("error", "Not Found");
      return supplyAsync(() -> notFound(response));
    } else {
      return photoRepository
          .getPhotosById(userId)
          .thenApplyAsync(
              photos -> {
                List<PersonalPhoto> userPhotos =
                    photos.stream()
                        .filter(
                            photo -> {
                              // Don't add primary photo to list of photos
                              if (photo.isPrimary()) {
                                return false;
                              }

                              // If user is admin, then display even private photos
                              if (userFromMiddleware.isAdmin()) {
                                return true;
                              }

                              // Don't add private photo's if user not the logged in user
                              return !(userFromMiddleware.getUserId() != userId
                                  && !photo.isPublic());
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
   * @return binary photo data with status 200 if found unauthorized with 401 if not authorized
   *     notFound with 404 if photo not found forbidden 403 if trying to get a photo that you do not
   *     have permission to 500 server error for any other server related error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getPhoto(int photoId, Http.Request request) {
    User user = request.attrs().get(ActionState.USER);
    return photoRepository
        .getPhotoById(photoId)
        .thenApplyAsync(
            photo -> {
              if (!photo.isPresent()) {
                throw new CompletionException(
                    new NotFoundException(
                        "Please provide a valid request body according to the API spec"));
              } else if (!user.isAdmin()
                  && !photo.get().isPublic()
                  && user.getUserId() != photo.get().getUser().getUserId()) {
                return forbidden();
              } else {
                String path = System.getProperty(USER_DIR) + STORAGE_PHOTOS;
                File photoToBeSent = new File(path, photo.get().getFilenameHash());
                if (!photoToBeSent.exists()) {
                  // here for the last of sprint 4 where we can't seem to access photos
                  // but we can access the thumbnails
                  ObjectNode res = Json.newObject();
                  res.put(MESSAGE_KEY, "Did not find the photo..." + photoToBeSent);
                  return internalServerError(res);
                }
                return ok().sendFile(photoToBeSent);
              }
            })
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Determine whether the user doing an upload can upload a photo for receiving user
   *
   * @param uploadingUserId the id of the user doing the upload
   * @param receivingUserId the if of the user for which the uploaded photo is
   * @return true (wrapped in a completion stage) if upload is allowed, false (wrapped in a
   *     completion stage) otherwise
   */
  private CompletionStage<Boolean> canUserUploadPhoto(int uploadingUserId, int receivingUserId) {
    return userRepository
        .getUserById(uploadingUserId)
        .thenCombineAsync(
            userRepository.getUserById(receivingUserId),
            (optionalUploadingUser, optionalReceivingUser) -> {
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
            },
            httpExecutionContext.current());
  }

  /**
   * Upload a new photo for a user
   *
   * @param userId the id of the user we are trying to add the photo for
   * @param request the HTTP request requesting the upload
   * @return the result for the request
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> uploadPhotoForUser(int userId, Http.Request request) {
    User userUploadingPhoto = request.attrs().get(ActionState.USER);
    ObjectNode response = Json.newObject();
    String messageKey = MESSAGE_KEY;

    return canUserUploadPhoto(userUploadingPhoto.getUserId(), userId)
        .thenComposeAsync(
            canUploadPhoto -> {
              if (!canUploadPhoto) {
                String message =
                    String.format(
                        "User %d is not allowed to upload a photo for user %d",
                        userUploadingPhoto.getUserId(), userId);
                throw new CompletionException(new ForbiddenRequestException(message));
              }

              Optional<String> optionalContentType = request.contentType();

              // check that a content type is specified
              if (!optionalContentType.isPresent()) {
                response.put(
                    messageKey, "Please specify the Content Type as specified in the API spec");
                return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
              }

              String contentType = optionalContentType.get();

              // check that the content type is allowed
              if (!contentType.equals("multipart/form-data")) {
                response.put(
                    messageKey, "Please specify the Content Type as specified in the API spec");
                return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
              }

              // parse request body to multipart form data
              Http.MultipartFormData multipartFormData = request.body().asMultipartFormData();
              Http.MultipartFormData.FilePart photo = multipartFormData.getFile("image");
              Map<String, String[]> textFields = multipartFormData.asFormUrlEncoded();

              // check that the body includes fields as specified in the API spec
              if (!(textFields.containsKey(IS_PRIMARY_KEY) || textFields.containsKey(IS_PUBLIC_KEY))) {
                response.put(
                    messageKey, "Please provide the request body as specified in the API spec");
                return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
              }

              // check that the body includes the photo
              if (photo == null) {
                response.put(messageKey, "Missing photo in the request body");
                return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
              }

              // check for the photo's content type
              String photoContentType = photo.getContentType();
              if (!(photoContentType.equals("image/png")
                  || photoContentType.equals("image/jpeg"))) {
                response.put(messageKey, "Your photo must be a .png or .jpg file");
                return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
              }

              // initialise the text components of the request
              String isPrimaryAsText = textFields.get(IS_PRIMARY_KEY)[0];
              String isPublicAsText = textFields.get(IS_PUBLIC_KEY)[0];
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
              String path = System.getProperty(USER_DIR) + STORAGE_PHOTOS;
              String token = Security.generateToken();
              String extension = photoContentType.equals("image/png") ? ".png" : ".jpg";
              String filename = token + extension;
              String thumbFilename = token + THUMB + extension;
              File fileDestination = new File(path, filename);
              File thumbFileDestination = new File(path, thumbFilename);

              // if the file path already exists, generate another token
              while (fileDestination.exists()) {
                token = Security.generateToken();
                filename = token + extension;
                thumbFilename = token + THUMB + extension;
                fileDestination = new File(path, filename);
                thumbFileDestination = new File(path, thumbFilename);
              }

              // save to filesystem
              temporaryPhotoFile.moveFileTo(fileDestination);
              log.info("Saved file to filesystem: {}", temporaryPhotoFile);

              // resize and save a thumbnail.
              try {
                log.info(
                    "Saving thumbnail of photo {} as {}", fileDestination, thumbFileDestination);
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
                saveThumbnail(
                    fileDestination, thumbFileDestination, photoContentType.split("/")[1]);
              } catch (IOException e) {
                return supplyAsync(Results::internalServerError, httpExecutionContext.current());
              }

              // create photo model in database
              final String usedFilename = filename;
              String finalThumbFilename = thumbFilename;
              return userRepository
                  .getUserById(userId)
                  .thenComposeAsync(
                      optionalReceivingUser -> {
                        if (!optionalReceivingUser.isPresent()) {
                          response.put(messageKey, String.format("User %d does not exist", userId));
                          return supplyAsync(() -> notFound(response));
                        }

                        User receivingUser = optionalReceivingUser.get();
                        PersonalPhoto personalPhoto =
                            new PersonalPhoto(
                                usedFilename,
                                isPublic,
                                receivingUser,
                                isPrimary,
                                finalThumbFilename,
                                false);
                        return photoRepository
                            .insert(personalPhoto)
                            .thenApplyAsync(
                                insertedPhoto -> {
                                  if (isPrimary) {
                                    // If old profile photo exists, delete thumbnail and photo of
                                    // old profile photo
                                    if (receivingUser.getProfilePhoto() != null) {
                                      PersonalPhoto oldProfilePhoto =
                                          receivingUser.getProfilePhoto();

                                      // Soft delete profile photo row in db
                                      if (oldProfilePhoto != null) {
                                        oldProfilePhoto.delete();
                                      }
                                    }

                                    receivingUser.setProfilePhoto(insertedPhoto);
                                    receivingUser.save();
                                  }

                                  return created(Json.toJson(insertedPhoto));
                                });
                      });
            },
            httpExecutionContext.current())
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Checks if the photo directories are created and creates them if not.
   */
  public void checkForAndCreatePhotosDirectory() {
      String path = System.getProperty(USER_DIR) + STORAGE_PHOTOS;
      File file = new File(path);

      if (!file.exists()) {
        file.mkdir();
      }
  }

  /**
   * Returns the thumbnail of a given photo.
   *
   * @param photoId the id of the photo of which the thumbnail will be returned.
   * @param request the Http request.
   * @return a Http response with one of the following: - 200 - with the photo data in the body. -
   *     401 - when the user is not authenticated. - 403 - when the user has not completed their
   *     profile. - 404 - when the photo does not exist.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getThumbnail(int photoId, Http.Request request) {
    User user = request.attrs().get(ActionState.USER);
    return photoRepository
        .getPhotoById(photoId)
        .thenApplyAsync(
            photo -> {
              if (!photo.isPresent()) {
                throw new CompletionException(
                    new NotFoundException(
                        "Please provide a valid request body according to the API spec"));
              } else if (!user.isAdmin()
                  && !photo.get().isPublic()
                  && user.getUserId() != photo.get().getUser().getUserId()) {
                return forbidden();
              } else {
                int dotIndex = photo.get().getFilenameHash().lastIndexOf('.');
                String fileType = photo.get().getFilenameHash().substring(dotIndex);
                String filename = photo.get().getFilenameHash().substring(0, dotIndex);
                String path = System.getProperty(USER_DIR) + STORAGE_PHOTOS;
                filename += THUMB + fileType;
                return ok().sendFile(new File(path, filename));
              }
            })
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Undoes a photo deletion.
   * Http Status codes:
   *    200 - OK - the photo is retrieved successfully.
   *    401 - Unauthorized - the requesting user is not logged in.
   *    403 - Forbidden - the requesting user does not have permission.
   *    404 - Not Found - the photo or user is missing.
   *
   * @param userId the id of the user owning the photo.
   * @param photoId the id of the photo.
   * @param request the HTTP request.
   * @return the HTTP response.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> undoProfilePhoto(int userId, int photoId, Http.Request request) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);

    if (Security.userHasPermission(userFromMiddleware, userId)) {
      return supplyAsync(Controller::forbidden);
    }
    User user = User.find.byId(userId);

    if (user == null) {
      return supplyAsync(() -> notFound("Could not find "));
    }

    return photoRepository
        .getPhotoByIdWithSoftDelete(photoId)
        .thenComposeAsync(
            optionalPhoto -> {
              if (!optionalPhoto.isPresent()) {
                throw new CompletionException(new NotFoundException(PHOTO_NOT_FOUND_MESSAGE));
              }

              PersonalPhoto photo = optionalPhoto.get();
              if (!user.isAdmin() && user.getUserId() != photo.getOwnerId()) {
                throw new CompletionException(
                    new ForbiddenRequestException("You can't undo photo"));
              }
              if (!photo.isDeleted()) {
                throw new CompletionException(
                    new BadRequestException("This photo has not been deleted"));
              }
              return photoRepository.undoPhotoDelete(photo);
            })
        .thenApplyAsync(
            photo -> {
              PersonalPhoto personalPhoto = user.getProfilePhoto();
              user.setProfilePhoto(photo);
              user.save();
              // Only delete personal photo if it is already set
              if (personalPhoto != null) {
                personalPhoto.delete();
              }
              return (Result) ok();
            })
        .exceptionally(exceptionUtil::getResultFromError);
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
        img =
            bufferedImage
                .getSubimage(0, midHeight - midWidth, width, width)
                .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
      } else {
        img =
            bufferedImage
                .getSubimage(midWidth - midHeight, 0, height, height)
                .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
      }
    } else if (midWidth > midHeight) {
      img =
          bufferedImage
              .getSubimage(midWidth - midHeight, 0, height, height)
              .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
    } else {
      img =
          bufferedImage
              .getSubimage(0, midHeight - midWidth, width, width)
              .getScaledInstance(300, 300, BufferedImage.SCALE_SMOOTH);
    }
    BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
    image.createGraphics().drawImage(img, 0, 0, null);
    ImageIO.write(image, photoContentType, thumbFileDestination);
  }

  /**
   * Deletes a cover photo for a user.
   *
   * @param userId the id of the user.
   * @param request the http request.
   * @return the http response with one of the following status codes. - 200 - OK - successfully
   *     deleted. - 401 - Unauthorised - user not logged in. - 403 - Forbidden - user does not have
   *     permission. - 404 - Not Found - user not found or user has no cover photo. - 500 - Internal
   *     Server Error - an unexpected error occurred.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> deleteCoverPhoto(int userId, Http.Request request) {
    return userRepository
        .getUserById(userId)
        .thenComposeAsync(
            optionalUser -> {
              if (!optionalUser.isPresent() || optionalUser.get().getCoverPhoto() == null) {
                throw new CompletionException(
                    new NotFoundException("User or cover photo not found"));
              }
              User user = optionalUser.get();
              User userFromMiddleware = request.attrs().get(ActionState.USER);
              if (userFromMiddleware.getUserId() != userId && !userFromMiddleware.isAdmin()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                            USER_DOES_NOT_HAVE_PERMISSION_TO_PERFORM_THIS_REQUEST));
              }
              return photoRepository.deletePhoto(user.getCoverPhoto());
            })
        .thenApplyAsync(photo -> (Result) ok())
        .exceptionally(
                exceptionUtil::getResultFromError);
  }

  /**
   * Undoes the deletion of a cover photo for a user.
   *
   * @param userId the id of the user.
   * @param request the http request.
   * @return the http response with one of the following status codes. - 200 - OK - successfully
   *     deleted. - 401 - Unauthorised - user not logged in. - 403 - Forbidden - user does not have
   *     permission. - 404 - Not Found - user not found or user has no cover photo. - 500 - Internal
   *     Server Error - an unexpected error occurred.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> undoDeleteCoverPhoto(
      int userId, int photoId, Http.Request request) {
    return userRepository
        .getUserById(userId)
        .thenComposeAsync(
            optionalUser -> {
              if (!optionalUser.isPresent()) {
                throw new CompletionException(new NotFoundException("User not found"));
              }
              User user = optionalUser.get();

              return photoRepository
                  .retrieveDeletedCoverPhoto(userId, photoId)
                  .thenComposeAsync(
                      deletedCoverPhoto -> {
                        if (!deletedCoverPhoto.isPresent()) {
                          throw new CompletionException(
                              new NotFoundException("Cover photo not found"));
                        }
                        User userFromMiddleware = request.attrs().get(ActionState.USER);
                        if (userFromMiddleware.getUserId() != userId
                            && !userFromMiddleware.isAdmin()) {
                          throw new CompletionException(
                              new ForbiddenRequestException(
                                      USER_DOES_NOT_HAVE_PERMISSION_TO_PERFORM_THIS_REQUEST));
                        }

                        return photoRepository
                            .undoPhotoDelete(deletedCoverPhoto.get())
                            .thenComposeAsync(
                                coverPhoto -> {
                                  user.setCoverPhoto(coverPhoto);
                                  return userRepository.updateUser(user);
                                });
                      });
            })
        .thenApplyAsync(user -> ok(Json.toJson(user.getCoverPhoto())))
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Adds a cover photo to the given user.
   *
   * @param userId the isd of the user to add the cover photo to.
   * @param photoId the id of the photo to set as the cover photo.
   * @param request the http request.
   * @return the http response with one of the following codes. - 200 - OK - successfully added the
   *     cover photo. - 401 - Unauthorized - user not logged in. - 403 - Forbidden - User does not
   *     have permission. - 404 - Not Found - user or photo not found. - 500 - Internal Server Error
   *     - an unexpected error occurred.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> addCoverPhoto(int userId, int photoId, Http.Request request) {
    return userRepository
        .getUserById(userId)
        .thenComposeAsync(
            optionalUser -> {
              if (!optionalUser.isPresent()) {
                throw new CompletionException(new NotFoundException("User not found"));
              }
              User user = optionalUser.get();
              User userFromMiddleware = request.attrs().get(ActionState.USER);
              if (userFromMiddleware.getUserId() != userId && !userFromMiddleware.isAdmin()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                            USER_DOES_NOT_HAVE_PERMISSION_TO_PERFORM_THIS_REQUEST));
              }
              return photoRepository
                  .getPhotoByIdWithSoftDelete(photoId)
                  .thenComposeAsync(
                      optionalPhoto -> {
                        if (!optionalPhoto.isPresent()) {
                          throw new CompletionException(new NotFoundException(PHOTO_NOT_FOUND_MESSAGE));
                        }
                        PersonalPhoto photo = optionalPhoto.get();
                        if (photo.getOwnerId() != user.getUserId()) {
                          throw new CompletionException(
                              new ForbiddenRequestException(
                                      USER_DOES_NOT_HAVE_PERMISSION_TO_PERFORM_THIS_REQUEST));
                        }
                        if (photo.isDeleted()) { // Case where undoing an old cover photo change.
                          return photoRepository
                              .undoPhotoDelete(photo)
                              .thenComposeAsync(
                                  coverPhoto -> {
                                    photoRepository.deletePhoto(user.getCoverPhoto());
                                    user.setCoverPhoto(coverPhoto);
                                    return userRepository.updateUser(user);
                                  });
                        } else {

                          // start copying the file to file system
                          String[] filenameArray = photo.getFilenameHash().split("\\.");
                          String extension = "." + filenameArray[filenameArray.length - 1];

                          String path = System.getProperty(USER_DIR) + STORAGE_PHOTOS;

                          File currentPhoto = new File(path, photo.getFilenameHash());
                          if (!currentPhoto.exists()) {
                            throw new CompletionException(new NotFoundException("File Not Found"));
                          }

                          String token = Security.generateToken();
                          String filename = token + extension;
                          File fileDestination = new File(path, filename);

                          // if the file path already exists, generate another token
                          while (fileDestination.exists()) {
                            token = Security.generateToken();
                            filename = token + extension;
                            fileDestination = new File(path, filename);
                          }

                          try {
                            java.nio.file.Files.copy(
                                currentPhoto.toPath(),
                                fileDestination.toPath(),
                                StandardCopyOption.COPY_ATTRIBUTES);
                          } catch (IOException e) {
                            log.error("File Error", e);
                            throw new CompletionException(new ServerErrorException());
                          }

                          PersonalPhoto coverPhoto =
                              new PersonalPhoto(filename, true, user, false, null, true);
                          return photoRepository
                              .insert(coverPhoto)
                              .thenComposeAsync(
                                  savedCoverPhoto -> {
                                    if (user.getCoverPhoto() != null) {
                                      photoRepository.deletePhoto(user.getCoverPhoto());
                                    }
                                    user.setCoverPhoto(savedCoverPhoto);
                                    return userRepository.updateUser(user);
                                  });
                        }
                      });
            })
        .thenApplyAsync(user -> ok(Json.toJson(user.getCoverPhoto())))
        .exceptionally(exceptionUtil::getResultFromError);
  }
}
