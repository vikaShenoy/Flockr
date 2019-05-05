package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import exceptions.NotFoundException;
import models.PersonalPhoto;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import util.Security;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
}
