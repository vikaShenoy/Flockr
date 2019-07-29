package tasks;

import akka.actor.ActorSystem;
import models.TreasureHunt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.sql.Timestamp;
import scala.concurrent.duration.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class DeleteExpiredTreasureHunts {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredTreasureHunts(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialise();
    }

    /**
     * Async function to get a list of all expired & deleted treasure hunts in the database.
     *
     * @return the list of expired deleted treasure hunts.
     */
    public CompletionStage<List<TreasureHunt>> getDeletedTreasureHunts() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return TreasureHunt.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .le("deleted_expiry", now).findList();

        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(10, TimeUnit.SECONDS),
                        Duration.create(24, TimeUnit.HOURS),
                        () -> getDeletedTreasureHunts()
                                    .thenApplyAsync(treasureHuntsList -> {
                                        log.info("-----------Cleaning up deleted treasure hunts-------------");
                                        System.out.println("-----------Cleaning up deleted treasure hunts-------------");
                                        for (TreasureHunt treasureHunt: treasureHuntsList) {
                                            treasureHunt.deletePermanent();
                                        }
                                        log.info(String.format(" %d Destination Proposals deleted successfully", treasureHuntsList.size()));
                                        System.out.printf(" %d Destination Proposals deleted successfully", treasureHuntsList.size());
                                        return treasureHuntsList;
                                    }),
                                    this.executionContext);
    }

}
