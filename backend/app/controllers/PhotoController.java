package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import models.PersonalPhoto;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import util.Security;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class PhotoController extends Controller {

    private final Security security;
    private final PhotoRepository photoRepository;

    @Inject
    public PhotoController(Security security, PhotoRepository photoRepository) {
        this.security = security;
        this.photoRepository = photoRepository;
    }

    @With(LoggedIn.class)
    public CompletionStage<Result> changePhotoPermissions(int userId, int photoId, Http.Request request) {

        JsonNode requestBody = request.body().asJson();

        boolean isPublic = requestBody.asBoolean();

        return photoRepository.getPhotoByIdAndUser(photoId, userId)
                .thenApplyAsync(optionalPhoto -> {
                    if (!optionalPhoto.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    PersonalPhoto personalPhoto = optionalPhoto.get();

                    personalPhoto.setPermission(isPublic);

                    return photoRepository.updatePhoto(personalPhoto);
                })
                .thenApplyAsync(personalPhoto -> (Result) ok())
                .exceptionally(e -> {
                      try {
                          throw e.getCause();
                      } catch (NotFoundException notFoundException) {
                          return notFound("Could not find photo from photo and user ID");
                      } catch (Throwable serverException) {
                          System.out.println(e);
                          return internalServerError();
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
     * @param photoFilename the filename of the photo to retrieve
     * @param request       HTTP request object
     * @return ok with 200 if photo found, notFound with 404 if photo not found //TODO: fgr27: add more response types
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getPhoto(String photoFilename, Http.Request request) {
        return supplyAsync(() -> {
            // TODO: need to check the database to see if the photo exists there too.
            File photo = new File("./photos/" + photoFilename);
            if (!photo.exists()) {
                return notFound();
            } else {
                return ok(photo);
            }
        });
    }
}
