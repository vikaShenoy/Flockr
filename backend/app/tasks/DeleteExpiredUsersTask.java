package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.PersonalPhoto;
import models.User;
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
 * Task to delete all users that are deleted and have expired.
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
     * Async function to get a list of all expired & deleted photos in the database.
     *
     * @return the list of expired deleted photos.
     */
    private CompletionStage<List<User>> getDeletedUsers() {
        return supplyAsync(() -> {
            Timestamp now = Timestamp.from(Instant.now());
            return User.find.query().setIncludeSoftDeletes()
                    .where().eq("deleted", true).and()
                    .ge("deleted_expiry", now).findList(); //TODO:: not sure if this part is right???
        });
    }

    private void initialise() {
        this.actorSystem
                .scheduler()
                .schedule(
                        Duration.create(2, TimeUnit.MINUTES), // initialDelay
                        Duration.create(24, TimeUnit.HOURS), // interval
                        () -> {
                            log.info("-----------Cleaning up deleted photos-------------");
                            System.out.println("-----------Cleaning up deleted photos-------------");
                            getDeletedUsers()
                                    .thenApplyAsync(users -> {
                                        for (User user : users) {
                                            user.deletePermanent();
                                        }
                                        return users;
                                    });
                        },
                        this.executionContext);
    }
}
