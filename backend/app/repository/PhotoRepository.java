package repository;

import models.PersonalPhoto;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;


import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PhotoRepository {
    private final DatabaseExecutionContext executionContext;

    @Inject
    public PhotoRepository(DatabaseExecutionContext databaseExecutionContext) {
        this.executionContext = databaseExecutionContext;
    }

    public CompletionStage<Optional<PersonalPhoto>> getPhotoByIdAndUser(int photoId, int userId) {
        return supplyAsync(() -> {

            Optional<PersonalPhoto> personalPhoto = PersonalPhoto.find.query().where()
                        .eq("user_user_id", userId)
                        .eq("photo_id", photoId)
                        .findOneOrEmpty();

            return personalPhoto;
        }, executionContext);
    }

    public CompletionStage<PersonalPhoto> updatePhoto(PersonalPhoto personalPhoto) {
        return supplyAsync(() -> {
            personalPhoto.update();
            return personalPhoto;
        }, executionContext);
    }
}
