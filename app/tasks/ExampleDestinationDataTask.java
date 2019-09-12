package tasks;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import models.Country;
import models.Destination;
import models.DestinationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.CountrySchedulerUtil;

/**
 * This class contains the code needed to populate destinations from an external API. Only runs when
 * the server is compiled.
 */
public class ExampleDestinationDataTask {

  private ActorSystem actorSystem;
  private ExecutionContext executionContext;
  private final CountrySchedulerUtil countrySchedulerUtil;
  final Logger log = LoggerFactory.getLogger(this.getClass());
  private final WSClient ws;
  private static int offset = 0;
  private static List<Country> countries;
  private static int countryIndex = 0;
  private static DestinationType cityDestinationType;

  @Inject
  public ExampleDestinationDataTask(
      ActorSystem actorSystem,
      ExecutionContext executionContext,
      CountrySchedulerUtil countrySchedulerUtil,
      WSClient ws) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
    this.countrySchedulerUtil = countrySchedulerUtil;
    this.ws = ws;
    initialise();
  }

  /**
   * Fetches all cities from GeoDB api
   *
   * @return all cities as a list of JsonNodes
   */
  private CompletionStage<List<JsonNode>> fetchCitiesFromCountry(Country country) {
    String countryUrl =
        "https://public.opendatasoft.com/api/records/1.0/search/?dataset=worldcitiespop&rows=10000&facet=country&refine.country="
            + country.getISOCode().toLowerCase() + "&start=" + offset;
    List<JsonNode> cities = new ArrayList<>();
    return ws.url(countryUrl)
        .get()
        .thenAcceptAsync(
            response -> {
              JsonNode resJson = response.asJson().get("records");

              if (resJson.size() < 10000) {
                offset = 0;
                countryIndex++;
              } else {
                offset += 10000;
              }

              for (JsonNode cityJson: resJson) {
                JsonNode cityDetails = cityJson.get("fields");
                ObjectNode city = Json.newObject();
                city.put("name", cityDetails.get("accentcity").asText());
                city.put("latitude", cityDetails.get("latitude").asText());
                city.put("longitude", cityDetails.get("longitude").asText());
                city.put("countryId", country.getCountryId());
                cities.add(city);
              }
            })
        .thenApplyAsync(nada -> cities);
  }

  /**
   * Gets all countries already in the db
   *
   * @return list of countries.
   */
  private CompletionStage<List<Country>> getCurrentCountries() {
    return supplyAsync(
        () -> {
          List<Country> currentCountries = Country.find.all();
          return currentCountries;
        });
  }

  /**
   * Saves a city destination to the database.
   *
   * @param cityDestination the city destination.
   * @return the destination once saved.
   */
  private CompletionStage<Destination> saveCityDestination(Destination cityDestination) {
    return supplyAsync(
        () -> {
          cityDestination.save();
          return cityDestination;
        });
  }

  /**
   * Checks if a destination exists in the database.
   *
   * @param destination the destination to check.
   * @return true if the destination exists.
   */
  private CompletionStage<Boolean> destinationExists(Destination destination) {
    return supplyAsync(
        () -> {
          Optional<Destination> optionalDestination =
              Destination.find
                  .query()
                  .where()
                  .eq(
                      "destination_country_country_id",
                      destination.getDestinationCountry().getCountryId())
                  .and()
                  .eq("destination_name", destination.getDestinationName())
                  .and()
                  .eq(
                      "destination_type_destination_type_id",
                      destination.getDestinationType().getDestinationTypeId())
                  .findOneOrEmpty();
          return optionalDestination.isPresent();
        });
  }

  /**
   * Gets the city destination type from the database.
   *
   * @return the city destination type.
   */
  private CompletionStage<Optional<DestinationType>> getCityDestinationType() {
    return supplyAsync(
        () ->
            DestinationType.find
                .query()
                .where()
                .eq("destination_type_name", "City")
                .findOneOrEmpty());
  }

  /**
   * Define the code to be run, and when it should be run NOTE - internet enabler must be turned on.
   * Runs at start up and then every 24 hours after that.
   */
  private void initialise() {
    this.actorSystem
        .scheduler()
        .schedule(
            Duration.create(1, TimeUnit.SECONDS), // initial delay
            Duration.create(1, TimeUnit.MINUTES), // interval
            () -> {
              // Initialise country and city type variables.
              if (countries == null) {
                getCurrentCountries()
                    .thenAcceptAsync(
                        allCountries -> {
                          countries = allCountries;
                          if (cityDestinationType == null) {
                            getCityDestinationType()
                                .thenAcceptAsync(
                                    optionalDestinationType ->
                                        optionalDestinationType.ifPresent(
                                            destinationType ->
                                                cityDestinationType = destinationType));
                          }
                        });
              } else {
                //
                supplyAsync(
                    () -> {
                      log.info("Beginning getting cities from open data soft api");
                      System.out.println("Beginning getting cities from open data soft api");
                      if (countryIndex < countries.size()) {
                        Country country = countries.get(countryIndex);
                        System.out.println(country.getCountryId());
                        fetchCitiesFromCountry(country)
                            .thenAcceptAsync(
                                cities -> {
                                  for (JsonNode city : cities) {
                                    String cityName = city.get("name").asText();
                                    Double cityLatitude = city.get("latitude").asDouble();
                                    Double cityLongitude = city.get("longitude").asDouble();
                                    Destination destination =
                                        new Destination(
                                            cityName,
                                            cityDestinationType,
                                            "",
                                            cityLatitude,
                                            cityLongitude,
                                            country,
                                            null,
                                            new ArrayList<>(),
                                            true);
                                    destinationExists(destination)
                                        .thenAcceptAsync(
                                            exists -> {
                                              if (!exists) {
                                                saveCityDestination(destination)
                                                    .thenAcceptAsync(
                                                        savedCity -> {
                                                          log.info(
                                                              String.format(
                                                                  "%s saved to the database.",
                                                                  savedCity.getDestinationName()));
                                                          System.out.println(
                                                              String.format(
                                                                  "%s saved to the database.",
                                                                  savedCity.getDestinationName()));
                                                        });
                                              } else {
                                                log.info(
                                                    String.format(
                                                        "%s already exists in the database.",
                                                        city.get("name").asText()));
                                              }
                                            });
                                  }
                                  log.info("Finished getting cities from open data soft api");
                                  System.out.println(
                                      "Finished getting cities from open data soft api");
                                });
                      }
                      return null;
                    });
              }
            },
            this.executionContext);
  }
}
