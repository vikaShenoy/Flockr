package tasks;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;


/**
 * This class contains the code needed to sync the countries from the external countries API
 */
public class CountrySyncTask {

    private ActorSystem actorSystem;
    private ExecutionContext executionContext;
    final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Please refer to Play documentation: https://www.playframework.com/documentation/2.7.x/ScheduledTasks
     * @param actorSystem
     * @param executionContext
     */
    @Inject
    public CountrySyncTask(ActorSystem actorSystem, ExecutionContext executionContext) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.initialise();
    }

    /**
     * Define the code to be run, and when it should be run
     */
    private void initialise() {
        this.actorSystem
            .scheduler()
            .schedule(
                Duration.create(10, TimeUnit.SECONDS), // initial delay
                Duration.create(10, TimeUnit.SECONDS), // interval
                () -> {
                    System.out.println("Hello!");
                    log.info("Hello from scheduled task!");
                },
                this.executionContext
            );
    }
}
