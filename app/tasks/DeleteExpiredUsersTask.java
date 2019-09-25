package tasks;

import akka.actor.ActorSystem;
import models.PersonalPhoto;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Task to delete all Users in the database that have been soft deleted if their
 * expiry has passed.
 */
public class DeleteExpiredUsersTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredUsersTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialise();
    }

    /**
     * Async function to get a list of all expired & deleted users in the database.
     *
     * @return the list of expired deleted users.
     */
    private CompletionStage<List<User>> getDeletedUsers() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return User.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .le("deleted_expiry", now).findList();
        });
    }


    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create( 5, TimeUnit.SECONDS), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> getDeletedUsers()
                                .thenApplyAsync(users -> {
                                    log.info("-----------Cleaning up deleted users-------------");
                                    for (User user : users) {
                                        List<PersonalPhoto> photos = user.getPersonalPhotos();
                                        for (PersonalPhoto photo: photos) {
                                            String path = System.getProperty("user.dir") + "/storage/photos";
                                            String filename = photo.getFilenameHash();
                                            String thumbFilename = photo.getThumbnailName();
                                            log.info(String.format("Deleting photo %s for user %s %s", filename, user.getFirstName(), user.getLastName()));
                                            File photoFile = new File(path, filename);
                                            File thumbFile = new File(path, thumbFilename);

                                            if (!photoFile.delete() || !thumbFile.delete()) {
                                                log.error("Could not delete the photo file.");
                                            } else {
                                                log.info("Deletion successful");
                                            }
                                            photo.deletePermanent();
                                        }
                                        if (user.deletePermanent()) {
                                            log.info(String.format("User %s %s deleted successfully.", user.getFirstName(), user.getLastName()));
                                        } else {
                                            log.info(String.format("User %s %s was not deleted.", user.getFirstName(), user.getLastName()));
                                        }
                                    }
                                    log.info(String.format("%d users deleted successfully", users.size()));

                                    return users;
                                }),
                        this.executionContext);
    }
}
