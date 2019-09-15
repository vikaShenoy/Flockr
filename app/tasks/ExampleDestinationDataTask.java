package tasks;

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
import models.Country;
import models.Destination;
import models.DestinationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

/**
 * This class contains the code needed to populate destinations from an external API. Only runs when
 * populate feature flag is on. (See TasksController)
 */
public class ExampleDestinationDataTask {

  private ActorSystem actorSystem;
  private ExecutionContext executionContext;
  final Logger log = LoggerFactory.getLogger(this.getClass());
  private final WSClient ws;
  private static List<Country> countries;
  private static int countryIndex = 0;
  private static DestinationType cityDestinationType;

  @Inject
  public ExampleDestinationDataTask(
      ActorSystem actorSystem, ExecutionContext executionContext, WSClient ws) {
    this.actorSystem = actorSystem;
    this.executionContext = executionContext;
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
            + country.getISOCode().toLowerCase()
            + "&sort=population";
    List<JsonNode> cities = new ArrayList<>();
    WSRequest request = ws.url(countryUrl);
    CompletionStage<? extends WSResponse> responsePromise = request.get();
    return responsePromise
        .thenApplyAsync(
            response -> {
              JsonNode resJson = response.asJson().get("records");
              countryIndex++;

              for (JsonNode cityJson : resJson) {
                JsonNode cityDetails = cityJson.get("fields");
                ObjectNode city = Json.newObject();
                city.put("name", cityDetails.get("accentcity").asText());
                city.put("latitude", cityDetails.get("latitude").asText());
                city.put("longitude", cityDetails.get("longitude").asText());
                city.put("countryId", country.getCountryId());
                cities.add(city);
              }
              return cities;
            })
        .exceptionally(
            e -> {
              try {
                throw e.getCause();
              } catch (Throwable throwable) {
                throwable.printStackTrace();
                return cities;
              }
            });
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
                  .eq("destination_name", destination.getDestinationName())
                  .and()
                  .eq(
                      "destination_type_destination_type_id",
                      destination.getDestinationType().getDestinationTypeId())
                  .and()
                  .eq(
                      "destination_country_country_id",
                      destination.getDestinationCountry().getCountryId())
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
            Duration.create(15, TimeUnit.SECONDS), // interval
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
                if (countryIndex < countries.size()) {
                  Country country = countries.get(countryIndex);
                  log.info(
                      String.format(
                          "Beginning getting cities from open data soft api for %s",
                          country.getCountryName()));
                  System.out.println(
                      String.format(
                          "Beginning getting cities from open data soft api for %s",
                          country.getCountryName()));
                  fetchCitiesFromCountry(country)
                      .thenApplyAsync(
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
                            log.info(
                                String.format(
                                    "Finished getting cities from open data soft api for %s",
                                    country.getCountryName()));
                            System.out.println(
                                String.format(
                                    "Finished getting cities from open data soft api for %s",
                                    country.getCountryName()));
                            return null;
                          });
                }
              }
            },
            this.executionContext);
  }
}
