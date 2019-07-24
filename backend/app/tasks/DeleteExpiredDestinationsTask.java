package tasks;

import akka.actor.ActorSystem;
import models.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;


/**
 * Task to delete all destinations in the database that have been soft deleted if their
 * expiry has passed.
 */
public class DeleteExpiredDestinationsTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredDestinationsTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialise();
    }

    /**
     * Async function to get a list of all expired & deleted photos in the database.
     *
     * @return the list of expired deleted photos.
     */
    private CompletionStage<List<Destination>> getDeletedDestinations() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return Destination.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .ge("deleted_expiry", now).findList(); //TODO:: not sure if this part is right???
        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(5, TimeUnit.SECONDS), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> getDeletedDestinations()
                                .thenApplyAsync(destinations -> {
                                    log.info("-----------Cleaning up deleted destinations-------------");
                                    System.out.println("-----------Cleaning up deleted destinations-------------");
                                    for (Destination destination : destinations) {
                                        destination.deletePermanent();
                                    }
                                    log.info(String.format("%d Destinations deleted successfully", destinations.size()));
                                    System.out.printf("%d Destinations deleted successfully%n", destinations.size());
                                    return destinations;
                                }),
                        this.executionContext);
    }
}

