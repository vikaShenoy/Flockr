package repository;

import models.PersonalPhoto;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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

    /**
     * Insert a photo into the database
     *
     * @param photo the personal photo to be inserted in the database
     * @return the photo object
     */
    public CompletionStage<PersonalPhoto> insert(PersonalPhoto photo) {
        return supplyAsync(() -> {
            photo.save();
            return photo;
        }, executionContext);
    }

    /**
     * Gets the photo with the given photo Id
     *
     * @param photoId the id of the photo to be retrieved
     * @return the photo
     */
    public CompletionStage<Optional<PersonalPhoto>> getPhotoById(int photoId) {
        return supplyAsync(() -> {
            Optional<PersonalPhoto> photo = PersonalPhoto.find.query().
                    where().eq("photo_id", photoId).findOneOrEmpty();
            return photo;
        }, executionContext);
    }

    public CompletionStage<Optional<PersonalPhoto>> getPhotoByFilename(String filenameHash) {
        return supplyAsync(() -> {
            Optional<PersonalPhoto> photo = PersonalPhoto.find.query()
                    .where().eq("filename_hash", filenameHash).findOneOrEmpty();
            return photo;
        }, executionContext);
    }

    /**
     * Gets a list of a users personal photos from the database
     *
     * @param userId the id of the user
     * @return a list of personal photos.
     */
    public CompletionStage<List<PersonalPhoto>> getPhotosById(int userId) {
        return supplyAsync(() -> PersonalPhoto.find.query().where()
                .eq("user_user_id", userId).findList(), executionContext);
    }

    /**
     * Delete a photo with the given hashed filename by finding the photo's id and deleting
     * it with the id found
     *
     * @param photoId the id of the photo to be deleted
     * @return the id of the photo that was deleted
     */
    public CompletionStage<Integer> deletePhoto(int photoId) {
        return supplyAsync(() -> {
            PersonalPhoto.find.deleteById(photoId);
            return photoId;
        }, executionContext);
    }
}
