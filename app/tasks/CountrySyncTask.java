package tasks;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Country;
import models.Nationality;
import models.Passport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import util.CountrySchedulerUtil;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the code needed to sync the countries from the external countries API.
 * Only runs when the server is compiled.
 */
public class CountrySyncTask {

    private ActorSystem actorSystem;
    private ExecutionContext executionContext;
    private final CountrySchedulerUtil countrySchedulerUtil;
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final WSClient ws;

    /**
     * Please refer to Play documentation: https://www.playframework.com/documentation/2.7.x/ScheduledTasks
     * @param actorSystem the actor system.
     * @param executionContext the context to execute the async functions on.
     */
    @Inject
    public CountrySyncTask(ActorSystem actorSystem, ExecutionContext executionContext, WSClient ws, CountrySchedulerUtil countrySchedulerUtil) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        this.ws = ws;
        this.initialise();
        this.countrySchedulerUtil = countrySchedulerUtil;
    }

    /**
     * Fetches all countries from restcountries api
     * @return all countries as a map
     */
    private CompletionStage<Map<String, Country>> fetchCountryApi() {
        String countryUrl = "https://restcountries.eu/rest/v2/all?fields=name;alpha2Code";
        return ws.url(countryUrl).get() .thenApplyAsync(response -> {
                    JsonNode resJson = response.asJson();
                    Map<String, Country> countries = new HashMap<>();

                    for (JsonNode currentCountry : resJson) {
                        String countryName = currentCountry.get("name").asText();
                        String ISOCode = currentCountry.get("alpha2Code").asText();

                        countries.put(ISOCode, new Country(countryName, ISOCode, true));
                    }

                    return countries;
                });
    }


    /**
     * Gets all countries already in the db
     * @return current countries as a map
     */
    private Map<String, Country> getCurrentCountries() {
        Map<String, Country> currentCountriesMap = new HashMap<>();
        List<Country> currentCountries = Country.find.all();

        for (Country country : currentCountries) {
            currentCountriesMap.put(country.getISOCode(), country);
        }
        return currentCountriesMap;
    }

    /**
     * Gets all the passports that is already stored in the database and returns a map of all the current
     * passports
     * @return current passports in the database as a map
     */
    private Map<String, Passport> getCurrentPassports() {
        Map<String, Passport> currentPassportsMap = new HashMap<>();
        List<Passport> currentPassports = Passport.find.all();

        for (Passport passport : currentPassports) {
            currentPassportsMap.put(passport.getPassportCountry(), passport);
        }
        return currentPassportsMap;
    }

    /**
     * Gets all the nationalities that is already stored in the database and returns a map of all the
     * current nationalities
     * @return the current nationalities in the database as a map
     */
    private Map<String, Nationality> getCurrentNationalities() {
        Map<String, Nationality> currentNationalitiesMap = new HashMap<>();
        List<Nationality> currentNationalities = Nationality.find.all();

        for (Nationality nationality : currentNationalities) {
            currentNationalitiesMap.put(nationality.getNationalityName(), nationality);
        }
        return currentNationalitiesMap;
    }

    /**
     * Saves all countries that need to be inserted or updated
     * @param countries the list of countries to be saved.
     */
    private void saveCountries(List<Country> countries) {
        for (Country country: countries) {
            country.save();
        }
    }

    /**
     * Saves all the country passports that needs to be updated or inserted in the database
     * @param passports the passports that either needs to be inserted or updated
     */
    private void savePassports(List<Passport> passports) {
        for (Passport passport : passports) {
            passport.save();
        }
    }

    /**
     * Saves all the country nationality that needs to be updated or inserted in the
     * database
     * @param nationalities the nationalities that either needs to be inserted or updated
     */
    private void saveNationalities(List<Nationality> nationalities) {
        for (Nationality nationality : nationalities) {
            nationality.save();
        }
    }

    /**
     * Define the code to be run, and when it should be run
     * NOTE - internet enabler must be turned on.
     */
    private void initialise() {
        this.actorSystem
            .scheduler()
            .schedule(
                Duration.create(1, TimeUnit.HOURS), // initial delay
                Duration.create(1, TimeUnit.HOURS), // interval
                () -> {
                    log.info("Country Schedule started");
                    long startTime = System.currentTimeMillis();
                    fetchCountryApi()
                            .thenAcceptAsync(newCountries -> {
                                Map<String, Country> oldCountries = getCurrentCountries();
                                Map<String, Passport> oldPassports = getCurrentPassports();
                                Map<String, Nationality> oldNationalities = getCurrentNationalities();
                                List<Country> countriesToSave = countrySchedulerUtil.getCountriesToSave(oldCountries, newCountries);
                                List<Passport> passportsToSave = countrySchedulerUtil.getPassportsToSave(newCountries, oldPassports);
                                List<Nationality> nationalitiesToSave = countrySchedulerUtil.getNationalitiesToSave(newCountries, oldNationalities);
                                saveCountries(countriesToSave);
                                savePassports(passportsToSave);
                                saveNationalities(nationalitiesToSave);
                                long endTime = System.currentTimeMillis();
                                long duration = (endTime - startTime) / 1000;
                                log.info(String.format("Country schedule finished, took: %d seconds", duration));
                           });
                },
                this.executionContext
            );
    }
}
