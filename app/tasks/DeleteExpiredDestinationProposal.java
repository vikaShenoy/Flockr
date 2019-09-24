package tasks;

import akka.actor.ActorSystem;
import models.DestinationProposal;
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
 * The class that handles the task to delete all the destination proposals in the database
 * that have been soft deleted if their expiry has passed.
 */
public class DeleteExpiredDestinationProposal {

    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeleteExpiredDestinationProposal(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialise();
    }

    /**
     * An asynchronous function to get a lit of all the expired and deleted destination
     * proposals in the database
     *
     * @return the list of the expired deleted destination proposals
     */
    private CompletionStage<List<DestinationProposal>> getDeletedDestinationProposals() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return DestinationProposal.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .le("deleted_expiry", now).findList();
        });
    }

    /**
     * Initialises the Scheduler
     */
    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(5, TimeUnit.SECONDS), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> getDeletedDestinationProposals()
                                    .thenApplyAsync(destinationProposals -> {
                                        log.info("-----------Cleaning up deleted destination proposals-------------");
                                        System.out.println("-----------Cleaning up deleted destination proposals-------------");
                                        for (DestinationProposal destinationProposal : destinationProposals) {
                                            destinationProposal.deletePermanent();
                                        }
                                        log.info(String.format("%d Destination Proposals deleted successfully", destinationProposals.size()));
                                        System.out.println(String.format("%d Destination Proposals deleted successfully", destinationProposals.size()));
                                        return destinationProposals;
                                    }),
                        this.executionContext);
    }
}
