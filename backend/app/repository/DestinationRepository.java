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
     * Gets a list of all destinations that a user has created
     * @return List of destinations
     */
    public CompletionStage<List<Destination>> getDestinationsbyUserId(int userId) {
        return supplyAsync(() -> {
            List<Destination> destinations = Destination.find.query().where().eq("destination_owner", userId).findList();
            return destinations;
        }, executionContext);
    }


    /**
     * Get a destination photo associated with a destination given both ids
     *
     * @param destinationId the id of the destination
     * @param photoId the id of the photo
     * @return an optional destination photo if such link exists
     */
    public CompletionStage<Optional<DestinationPhoto>> getDestinationPhotoById(int destinationId, int photoId) {
        return supplyAsync(() -> {
            Optional<DestinationPhoto> destinationPhoto = DestinationPhoto.find.query()
                    .where().eq("destination_destination_id", destinationId)
                    .and().eq("personal_photo_photo_id", photoId).findOneOrEmpty();
            return destinationPhoto;
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
            destination.save();
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
     * Saves the destination photo into the database
     *
     * @param destinationPhoto the destination photo to be saved in the database
     * @return the photo object
     */
    public CompletionStage<DestinationPhoto> savePhoto(DestinationPhoto destinationPhoto) {
        return supplyAsync(() -> {
            destinationPhoto.save();
            return destinationPhoto;
        });
    }

    /**
     * Insert a destination photo into the database
     *
     * @param photo the destination photo to be inserted in the database
     * @return the photo object
     */
    public CompletionStage<DestinationPhoto> insertDestinationPhoto(DestinationPhoto photo) {
        return supplyAsync(() -> {
            photo.insert();
            return photo;
        }, executionContext);
    }

    public CompletionStage<DestinationProposal> createProposal(DestinationProposal proposal) {
       return supplyAsync(() -> {
           proposal.insert();
           System.out.println("size is: " + DestinationProposal.find.all().size());
           System.out.println(proposal.getDestinationProposalId());
           return proposal;
       }, executionContext);
    }

}
