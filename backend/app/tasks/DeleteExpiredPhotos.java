package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.PersonalPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Task to delete all personal photos in the database that have been soft deleted if their
 * expiry has passed.
 */
public class DeleteExpiredPhotos {

    private final ActorSystem actorSystem;
    private final TasksCustomExecutionContext executionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredPhotos(ActorSystem actorSystem, TasksCustomExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialise();
    }

    private void initialise() {
        try {
            this.actorSystem
                    .scheduler()
                    .schedule(
                            Duration.create(10, TimeUnit.SECONDS), // initialDelay
                            Duration.create(24, TimeUnit.HOURS), // interval
                            () -> {
                                log.info("-----------Cleaning up deleted photos-------------");
                                System.out.println("-----------Cleaning up deleted photos-------------");
                                Timestamp now = Timestamp.from(Instant.now());
                                List<PersonalPhoto> personalPhotos = PersonalPhoto.find.query().setIncludeSoftDeletes()
                                        .where().eq("deleted", true).and()
                                        .ge("deleted_expiry", now).findList(); //TODO:: not sure if this part is right???
                                for (PersonalPhoto personalPhoto : personalPhotos) {
                                    File photoToDelete = new File(
                                            "./storage/photos/" + personalPhoto.getFilenameHash());
                                    File thumbnailToDelete = new File(
                                            "./storage/photos/" + personalPhoto.getThumbnailName());
                                    ObjectNode message = Json.newObject();

                                    if (!photoToDelete.delete() || !thumbnailToDelete.delete()) {
                                        log.error("Could not delete photo or thumbnail for file " +
                                                personalPhoto.getFilenameHash());
                                    } else {
                                        log.info("Successfully deleted the photo " +
                                                personalPhoto.getFilenameHash());
                                    }
                                    personalPhoto.deletePermanent();
                                }
                            },
                            this.executionContext);
        } catch (Exception ignore) {}
    }
}
