package tasks;

import akka.actor.ActorSystem;
import models.DestinationPhoto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class DeletedExpiredDestinationPhotos {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DeletedExpiredDestinationPhotos(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialise();
    }

    private CompletionStage<List<DestinationPhoto>> getDeletedDestinationPhotos() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return DestinationPhoto.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .le("deleted_expiry", now).findList();
        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(5, TimeUnit.SECONDS),
                        Duration.create(24, TimeUnit.HOURS),
                        () -> getDeletedDestinationPhotos()
                                .thenApplyAsync(destinationPhotos -> {
                                    log.info("-----------Cleaning up deleted destination photos-------------");
                                    System.out.println("-----------Cleaning up deleted destination photos-------------");
                                    for (DestinationPhoto destinationPhoto: destinationPhotos) {
                                        destinationPhoto.deletePermanent();
                                    }
                                    log.info(String.format("%d Destination Proposals deleted successfully", destinationPhotos.size()));
                                    System.out.println(String.format("%d Destination Proposals deleted successfully", destinationPhotos.size()));
                                    return destinationPhotos;
                                }),
                this.executionContext);
    }
}
