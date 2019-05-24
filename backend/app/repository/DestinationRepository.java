package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.*;
import play.db.ebean.EbeanConfig;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DestinationRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency injection
     *
     * @param ebeanConfig      ebean config to use
     * @param executionContext Context to run completion stages on
     */
    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }


    /**
     * Gets a list of all destinations
     *
     * @return <b>List</b> of destinations
     */
    public CompletionStage<List<Destination>> getDestinations() {
        return supplyAsync(() -> {
            List<Destination> destinations = Destination.find.query().findList();
            return destinations;
        }, executionContext);
    }


    /**
     * Gets a destination by it's ID
     *
     * @param destinationId The ID of the destination to get
     * @return the destination object
     */
    public CompletionStage<Optional<Destination>> getDestinationById(int destinationId) {
        return supplyAsync(() -> {
            Optional<Destination> destination = Destination.find.query().
                    where().eq("destination_id", destinationId).findOneOrEmpty();
            return destination;
        }, executionContext);
    }


    /**
     * Inserts a destination into the database
     *
     * @param destination the destination to be inserted
     * @return the destination object
     */
    public CompletionStage<Destination> insert(Destination destination) {
        return supplyAsync(() -> {
            destination.save();
            return destination;
        }, executionContext);
    }

    /**
     * Updates a destination
     *
     * @param destination The destination to update
     * @return The destination that was updated
     */
    public CompletionStage<Destination> update(Destination destination) {
        return supplyAsync(() -> {
            destination.update();
            return destination;
        }, executionContext);
    }

    /**
     * Gets a list of countries
     *
     * @return The list of countries
     */
    public CompletionStage<List<Country>> getCountries() {
        return supplyAsync(() -> {
            List<Country> countries = Country.find.query().findList();
            return countries;
        }, executionContext);
    }

    /**
     * Gets a list of countries
     *
     * @return The list of countries Json
     */
    public CompletionStage<List<DestinationType>> getDestinationTypes() {
        return supplyAsync(() -> {
            List<DestinationType> destinationTypes = DestinationType.find.query().findList();
            return destinationTypes;
        }, executionContext);
    }

    /**
     * Gets a list of districts
     *
     * @param countryId The country id to get the districts from
     * @return The list of districts as Json
     */
    public CompletionStage<List<District>> getDistricts(int countryId) {
        return supplyAsync(() -> {
            List<District> districts = District.find.query().where()
                    .eq("country_country_id", countryId).findList();
            return districts;
        }, executionContext);
    }

    /**
     * Delete a destination given a destination by id
     *
     * @param destinationId the id of the destination we are trying to delete
     * @return the id of the destination that was deleted
     */
    public CompletionStage<Integer> deleteDestination(int destinationId) {
        return supplyAsync(() -> {
            Destination.find.deleteById(destinationId);
            return destinationId;
        }, executionContext);
    }

    /**
     * Checks for a duplicate destination when a destination is being added or updated.
     *
     * @param countryId The country Id of  the destination being added
     * @param destinationName The name of the destination being added
     * @param destinationTypeId The Id of the destination type being added
     * @param districtId The Id of the district being added
     * @return 1 if destination exists or 0 if no duplicates are found
     */
    public boolean CheckDestinations(int countryId, String destinationName, int destinationTypeId,
                                                      int districtId)   {
            List<Destination> destinations = Destination.find.query().where()
                    .eq("destination_country_country_id", countryId)
                    .eq("destination_name", destinationName)
                    .eq("destination_type_destination_type_id", destinationTypeId)
                    .eq("destination_district_district_id", districtId)
                    .findList();
            return !destinations.isEmpty();

    }
}
