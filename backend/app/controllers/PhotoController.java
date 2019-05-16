package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;
import models.PersonalPhoto;
import models.User;
import play.libs.Files;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import repository.UserRepository;
import util.PhotoUtil;
import util.Security;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.*;


public class PhotoController {
    private final PhotoRepository photoRepository;
    private HttpExecutionContext httpExecutionContext;
    private UserRepository userRepository;
    private PhotoUtil photoUtil;

    @Inject
    public PhotoController(PhotoRepository photoRepository, UserRepository userRepository, PhotoUtil photoUtil, HttpExecutionContext httpExecutionContext) {
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
        if (!requestBody.has("isPublic")) {
            response.put("message", "Please specify the JSON body as specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        // If the JSON is empty
        if (requestBody.size() == 0) {
            response.put("message", "The request body is empty. Please specify the JSON body as " +
                    "specified in the API spec");
            return supplyAsync(() -> badRequest(response), httpExecutionContext.current());
        }

        boolean isPublic = requestBody.asBoolean();
        return photoRepository.getPhotoById(photoId)
                .thenApplyAsync(optionalPhoto -> {
                    // If the photo with the given photo id does not exists
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    // Checks that the user is either the admin or the owner of the photo to change permission groups
                    if (!user.isAdmin() && user.getUserId() != optionalPhoto.get().getUser().getUserId()) {
                        throw new CompletionException(new UnauthorizedException());
                    }

                    PersonalPhoto photo = optionalPhoto.get();
                    photo.setPermission(isPublic);
                    return photoRepository.updatePhoto(photo);
                }).thenApplyAsync(PersonalPhoto -> ok("Successfully updated permission groups"))
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundException) {
                        response.put("message", "Could not find a photo with the given user id and photo id");
                        return notFound(response);
                    } catch (UnauthorizedException unauthorizedException) {
                        response.put("message", "You are unauthorised to change the photo permission");
                        return forbidden(response);
                    } catch (Throwable serverException) {
                        response.put("message", "Endpoint under development");
                        return internalServerError(response);
                    }
                });
    }


    /**
     * This function is responsible for deleting the photo with the given ID
     *
     * @param photoFilename the hashed filename of the photo to be deleted
     * @param request       the Http request sent
     * @return a Play result
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deletePhoto(String photoFilename, Http.Request request) {
        return photoRepository.getPhotoByFilename(photoFilename)
                .thenComposeAsync((optionalPhoto) -> {
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    PersonalPhoto photo = optionalPhoto.get();
                    ObjectNode message = Json.newObject();
                    message.put("message", "Successfully deleted the given filename photo");
                    return this.photoRepository.deletePhoto(photo.getPhotoId());
                })
                .thenApplyAsync(photoId -> (Result) ok())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", "The photo with the given hashed filename is not found");
                        return notFound(message);
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
     * @return a completion stage and a status code 200.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhotos(int userId, Http.Request request) {
        return photoRepository.getPhotosById(userId)
                .thenApplyAsync((photos) -> {
                    JsonNode photosAsJSON = Json.toJson(photos);
                    System.out.println(photosAsJSON.asText());
                    return ok(photosAsJSON);
                });
    }

    /**
     * Gets a specific photo
     *
     * @param photoId the id of the photo to retrieve
     * @param request       HTTP request object
     * @return binary photo data with status 200 if found
     *         unauthorized with 401 if not authorized
     *         notFound with 404 if photo not found //TODO Add this to the API spec!
     *         forbidden 403 if trying to get a photo that you do not have permission to
     *         500 server error for any other server related error
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhoto(int photoId, Http.Request request) {

        User user = request.attrs().get(ActionState.USER);

        return photoRepository.getPhotoById(photoId).thenApplyAsync(optionalPhoto -> {
            if (!optionalPhoto.isPresent()) {
                return notFound();
            } else{

                if (!user.isAdmin() && !optionalPhoto.get().getIsPublic() && user.getUserId() != optionalPhoto.get().getUser().getUserId()) {
                    return forbidden();
                } else {
                    return ok().sendFile(new File("./app/photos/" + optionalPhoto.get().getFileNameHash()));
                }
            }
        });

    }

    /**
     * Upload a new photo for a user
     * @param userId the id of the user we are trying to add the photo for
     * @param request the HTTP request requesting the upload
     * @return the result for the request
     */
    public CompletionStage<Result> uploadPhotoForUser(int userId, Http.Request request) {
        Optional<String> optionalContentType = request.contentType();
        ObjectNode response = Json.newObject();
        String messageKey = "message";

        // TODO: check that we are authorised to upload the photo

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

        // get the photo as a file from the request
        Files.TemporaryFile temporaryPhotoFile = (Files.TemporaryFile) photo.getRef();

        // copy file to local file system
        String path = System.getProperty("user.dir") + "/storage/photos";
        Security security = new Security();
        String token = security.generateToken();
        String extension = photoContentType.equals("image/png") ? ".png" : ".jpg";
        String filename = token + extension;
        File destination = new File(path, filename);

        // if the file path already exists, generate another token
        while (destination.exists()) {
            token = security.generateToken();
            filename = token + extension;
            destination = new File(path, filename);
        }

        temporaryPhotoFile.moveFileTo(destination);

        response.put(messageKey, "Endpoint under development");
        return supplyAsync(() -> internalServerError(response), httpExecutionContext.current());
    }
}
