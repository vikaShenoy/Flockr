package tasks;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * This class contains the code needed to sync the countries from the external countries API.
 * Only runs when the server is compiled.
 */
public class CountrySyncTask {

    private ActorSystem actorSystem;
    private ExecutionContext executionContext;
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final WSClient ws;

    /**
     * Please refer to Play documentation: https://www.playframework.com/documentation/2.7.x/ScheduledTasks
     * @param actorSystem
     * @param executionContext
     */
    @Inject
    public CountrySyncTask(ActorSystem actorSystem, ExecutionContext executionContext, WSClient ws) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.ws = ws;
        this.initialise();
    }

    private void fetchCountryApi() {
        String countryUrl = "https://restcountries.eu/rest/v2/all";
        WSRequest request = ws.url(countryUrl);
        System.out.println("Request called");

        // TODO - Next step: figure out how to process WSResponse object, return a list of countries.

    }

    public void checkCountryValidity() {
    }

    /**
     * Define the code to be run, and when it should be run
     * NOTE - internet enabler must be turned on.
     */
    private void initialise() {
        this.actorSystem
            .scheduler()
            .schedule(
                Duration.create(10, TimeUnit.SECONDS), // initial delay
                Duration.create(10, TimeUnit.SECONDS), // interval
                () -> {
                    System.out.println("Scheduled task executed.");
                    log.info("Hello from scheduled task!");
                    fetchCountryApi();
                    //List<Country> countries = fetchCountryApi();
                },
                this.executionContext
            );
    }
}
