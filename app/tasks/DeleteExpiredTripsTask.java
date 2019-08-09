package tasks;


import akka.actor.ActorSystem;
import models.TripComposite;
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
 * Task to delete any trips in the database that have surpassed their soft-deletion
 * expiry date
 */
public class DeleteExpiredTripsTask {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredTripsTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;

        this.initialise();
    }

    /**
     * Async function to get a list of all expired & deleted trips in the database.
     *
     * @return the list of expired deleted trips.
     */
    private CompletionStage<List<TripComposite>> getDeletedTrips() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return TripComposite.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .le("deleted_expiry", now).findList();
        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(5, TimeUnit.SECONDS), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> getDeletedTrips()
                                .thenApplyAsync(trips -> {
                                    log.info("-----------Cleaning up deleted trips-------------");
                                    System.out.println("-----------Cleaning up deleted trips-------------");
                                    for (TripComposite trip : trips) {
                                        trip.deletePermanent();
                                    }
                                    log.info(String.format("%d Trips deleted successfully", trips.size()));
                                    System.out.printf("%d Trips deleted successfully%n", trips.size());
                                    return trips;
                                }),
                        this.executionContext);
    }
}
