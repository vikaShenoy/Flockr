package repository;

import models.PersonalPhoto;
import javax.inject.Inject;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
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
    return supplyAsync(
        () -> {
          Optional<PersonalPhoto> personalPhoto =
              PersonalPhoto.find
                  .query()
                  .where()
                  .eq("user_user_id", userId)
                  .eq("photo_id", photoId)
                  .findOneOrEmpty();

          return personalPhoto;
        },
        executionContext);
  }

  public CompletionStage<PersonalPhoto> updatePhoto(PersonalPhoto personalPhoto) {
    return supplyAsync(
        () -> {
          personalPhoto.update();
          return personalPhoto;
        },
        executionContext);
  }

  /**
   * Insert a photo into the database
   *
   * @param photo the personal photo to be inserted in the database
   * @return the photo object
   */
  public CompletionStage<PersonalPhoto> insert(PersonalPhoto photo) {
    return supplyAsync(
        () -> {
          photo.save();
          return photo;
        },
        executionContext);
  }

  /**
   * Gets the photo with the given photo Id
   *
   * @param photoId the id of the photo to be retrieved
   * @return the photo
   */
  public CompletionStage<Optional<PersonalPhoto>> getPhotoById(int photoId) {
    return supplyAsync(
        () -> {
          Optional<PersonalPhoto> photo =
              PersonalPhoto.find.query().where().eq("photo_id", photoId).findOneOrEmpty();
          return photo;
        },
        executionContext);
  }

  /**
   * Gets the photo with the given photo Id including soft deleted photos.
   *
   * @param photoId the id of the photo to be retrieved
   * @return the photo
   */
  public CompletionStage<Optional<PersonalPhoto>> getPhotoByIdWithSoftDelete(int photoId) {
    return supplyAsync(
        () -> {
          Optional<PersonalPhoto> photo =
              PersonalPhoto.find
                  .query()
                  .setIncludeSoftDeletes()
                  .where()
                  .eq("photo_id", photoId)
                  .findOneOrEmpty();
          return photo;
        },
        executionContext);
  }

  public CompletionStage<Optional<PersonalPhoto>> getPhotoByFilename(String filenameHash) {
    return supplyAsync(
        () -> {
          Optional<PersonalPhoto> photo =
              PersonalPhoto.find.query().where().eq("filename_hash", filenameHash).findOneOrEmpty();
          return photo;
        },
        executionContext);
  }

  /**
   * Gets a list of a users personal photos from the database
   *
   * @param userId the id of the user
   * @return a list of personal photos.
   */
  public CompletionStage<List<PersonalPhoto>> getPhotosById(int userId) {
    return supplyAsync(
        () ->
            PersonalPhoto.find
                .query()
                .where()
                .eq("user_user_id", userId)
                .and()
                .eq("is_cover", false)
                .findList(),
        executionContext);
  }

  /**
   * Delete a photo with the given hashed filename by finding the photo's id and deleting it with
   * the id found
   *
   * @param photo the photo to be deleted
   * @return the id of the photo that was deleted
   */
  public CompletionStage<Integer> deletePhoto(PersonalPhoto photo) {
    return supplyAsync(
        () -> {
          photo.setDeletedExpiry(Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
          photo.save();
          photo.delete(); // Soft delete
          return photo.getPhotoId();
        },
        executionContext);
  }

  /**
   * Retrieves a deleted cover photo.
   *
   * @param userId thi id of the user that owns the cover photo.
   * @param photoId the id of the deleted cover photo.
   * @return the optional cover photo.
   */
  public CompletionStage<Optional<PersonalPhoto>> retrieveDeletedCoverPhoto(
      int userId, int photoId) {
    return supplyAsync(
        () ->
            PersonalPhoto.find
                .query()
                .setIncludeSoftDeletes()
                .where()
                .eq("user_user_id", userId)
                .and()
                .eq("photo_id", photoId)
                .and()
                .eq("deleted", true)
                .and()
                .eq("is_cover", true)
                .findOneOrEmpty());
  }

  /**
   * Undoes a personal photo deletion.
   *
   * @param personalPhoto the photo to undo deletion of.
   * @return the photo after deletion is undone.
   */
  public CompletionStage<PersonalPhoto> undoPhotoDelete(PersonalPhoto personalPhoto) {
    return supplyAsync(
        () -> {
          personalPhoto.setDeleted(false);
          personalPhoto.setDeletedExpiry(null);
          personalPhoto.save();
          return personalPhoto;
        });
  }
}
