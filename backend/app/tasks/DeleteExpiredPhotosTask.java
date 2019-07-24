package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.PersonalPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
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
 * Task to delete all personal photos in the database that have been soft deleted if their
 * expiry has passed.
 */
public class DeleteExpiredPhotosTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredPhotosTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialise();
    }

    /**
     * Async function to get a list of all expired & deleted photos in the database.
     *
     * @return the list of expired deleted photos.
     */
    private CompletionStage<List<PersonalPhoto>> getDeletedPhotos() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return PersonalPhoto.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .ge("deleted_expiry", now).findList(); //TODO:: not sure if this part is right???
        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(2, TimeUnit.SECONDS), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> {
                            getDeletedPhotos()
                                .thenApplyAsync(personalPhotos -> {
                                    log.info("-----------Cleaning up deleted photos-------------");
                                    System.out.println("-----------Cleaning up deleted photos-------------");
                                    for (PersonalPhoto personalPhoto : personalPhotos) {
                                        File photoToDelete = new File(
                                                "./storage/photos/" + personalPhoto.getFilenameHash());
                                        File thumbnailToDelete = new File(
                                                "./storage/photos/" + personalPhoto.getThumbnailName());

                                        if (!photoToDelete.delete() || !thumbnailToDelete.delete()) {
                                            log.error("Could not delete photo or thumbnail for file " +
                                                    personalPhoto.getFilenameHash());
                                        } else {
                                            log.info("Successfully deleted the photo " +
                                                    personalPhoto.getFilenameHash());
                                        }
                                        personalPhoto.deletePermanent();
                                    }
                                    log.info(String.format("%d Photos deleted successfully", personalPhotos.size()));
                                    System.out.printf("%d Photos deleted successfully%n", personalPhotos.size());
                                    return personalPhotos;
                            });
                        },
                        this.executionContext);
    }
}

