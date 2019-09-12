package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.Country;
import models.Destination;
import models.DestinationPhoto;
import models.DestinationProposal;
import models.DestinationType;
import play.db.ebean.EbeanConfig;

public class DestinationRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private String destinationNamePropertyName = "destinationName";

    /**
     * Dependency injection
     *
     * @param ebeanConfig ebean config to use
     * @param executionContext Context to run completion stages on
     */
    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Gets a list of all destinations
     *
     * @return <b>List</b> of destinations
     */
    public CompletionStage<List<Destination>> getDestinations() {
        return supplyAsync(
            () -> {
                List<Destination> destinations = Destination.find.query().findList();
                return destinations;
            },
            executionContext);
    }

    /**
     * Get all destinations with an offset, sorted by destination name.
     * @param offset the offset for the results
     * @return destinations in the specified offset
     */
    public CompletionStage<List<Destination>> getDestinations(int offset) {
        int maxRows = 30;
        return supplyAsync(() -> Destination.find.query().where()
            .orderBy("destinationName")
            .setFirstRow(offset)
            .setMaxRows(maxRows)
            .findList()
        , executionContext);
    }

    /**
     * Get the destinations that match a certain criterion and offset, sorted by
     * destination name.
     * @param searchCriterion the criterion by which we are filtering destinations
     * @param offset the offset for results
     * @return the list of destinations matching the query
     */
    public CompletionStage<List<Destination>> getDestinations(String searchCriterion, int offset) {
        int maxRows = 30;
        return supplyAsync(() -> Destination.find.query().where()
            .ilike(destinationNamePropertyName, searchCriterion + "%")
            .orderBy(destinationNamePropertyName)
            .setFirstRow(offset)
            .setMaxRows(maxRows)
            .findList()
        , executionContext);
    }

    /**
     * Gets a destination by it's ID
     *
     * @param destinationId The ID of the destination to get
     * @return the destination object
     */
    public CompletionStage<Optional<Destination>> getDestinationById(int destinationId) {
        return supplyAsync(
            () -> {
                Optional<Destination> destination =
                    Destination.find.query().where()
                        .eq("destination_id", destinationId)
                        .findOneOrEmpty();
                return destination;
            },
            executionContext);
    }

    /**
     * Gets a destination by it's ID including soft deleted destinations.
     *
     * @param destinationId The ID of the destination to get
     * @return the destination object
     */
    public CompletionStage<Optional<Destination>> getDestinationByIdIncludingSoftDelete(
        int destinationId) {
        return supplyAsync(
            () -> {
                Optional<Destination> destination =
                    Destination.find
                        .query()
                        .setIncludeSoftDeletes()
                        .where()
                        .eq("destination_id", destinationId)
                        .findOneOrEmpty();
                return destination;
            },
            executionContext);
    }

    /**
     * Undoes a destination deletion.
     *
     * @param destination the destination to be undeleted.
     * @return the destination after being restored.
     */
    public CompletionStage<Destination> undoDeletion(Destination destination) {
        return supplyAsync(
            () -> {
                destination.setDeleted(false);
                destination.setDeletedExpiry(null);
                destination.save();
                return destination;
        });
    }

    /**
     * Gets a list of all destinations that a user has created
     *
     * @return List of destinations
     */
    public CompletionStage<List<Destination>> getDestinationsbyUserId(int userId) {
        return supplyAsync(
            () -> {
                List<Destination> destinations =
                    Destination.find.query().where()
                        .eq("destination_owner", userId).findList();
                return destinations;
            },
            executionContext);
    }

    /**
     * Get a destination photo associated with a destination given both ids
     *
     * @param destinationId the id of the destination
     * @param photoId the id of the photo
     * @return an optional destination photo if such link exists
     */
    public CompletionStage<Optional<DestinationPhoto>> getDestinationPhotoById(
        int destinationId, int photoId) {
        return supplyAsync(
            () -> {
                Optional<DestinationPhoto> destinationPhoto =
                    DestinationPhoto.find
                        .query()
                        .where()
                        .eq("destination_destination_id", destinationId)
                        .and()
                        .eq("destination_photo_id", photoId)
                        .findOneOrEmpty();
                return destinationPhoto;
            },
            executionContext);
    }

    /**
     * Inserts a destination into the database
     *
     * @param destination the destination to be inserted
     * @return the destination object
     */
    public CompletionStage<Destination> insert(Destination destination) {
        return supplyAsync(
            () -> {
                destination.save();
                return destination;
            },
            executionContext);
    }

    /**
     * Updates a destination
     *
     * @param destination The destination to update
     * @return The destination that was updated
     */
    public CompletionStage<Destination> update(Destination destination) {
        return supplyAsync(
            () -> {
                destination.save();
                return destination;
            },
            executionContext);
    }

    /**
     * Gets a list of countries
     *
     * @return The list of countries
     */
    public CompletionStage<List<Country>> getCountries() {
        return supplyAsync(
            () -> {
                List<Country> countries = Country.find.query().orderBy().asc("country_name")
                    .findList();
                return countries;
            },
            executionContext);
    }

    /**
     * Gets a list of countries
     *
     * @return The list of countries Json
     */
    public CompletionStage<List<DestinationType>> getDestinationTypes() {
        return supplyAsync(
            () -> {
                List<DestinationType> destinationTypes = DestinationType.find.query().findList();
                return destinationTypes;
            },
            executionContext);
    }

    /**
     * Delete a destination given a destination by id
     *
     * @param destinationId the id of the destination we are trying to delete
     * @return the id of the destination that was deleted
     */
    public CompletionStage<Integer> deleteDestination(int destinationId) {
        return supplyAsync(
            () -> {
                Destination destination = Destination.find.byId(destinationId);
                // Set the expiry time for an hour from now.
                destination
                    .setDeletedExpiry(Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
                destination.save();

                destination.delete(); // Soft delete.
                return destinationId;
            },
            executionContext);
    }

    /**
     * Saves the destination photo into the database
     *
     * @param destinationPhoto the destination photo to be saved in the database
     * @return the photo object
     */
    public CompletionStage<DestinationPhoto> savePhoto(DestinationPhoto destinationPhoto) {
        return supplyAsync(
            () -> {
                destinationPhoto.save();
                return destinationPhoto;
        });
    }

    public CompletionStage<Optional<DestinationPhoto>> getPhotoByIdWithSoftDelete(
        int destinationId, int photoId) {
        return supplyAsync(
            () -> {
                Optional<DestinationPhoto> photo =
                    DestinationPhoto.find
                        .query()
                        .setIncludeSoftDeletes()
                        .where()
                        .eq("destination_destination_id", destinationId)
                        .eq("destination_photo_id", photoId)
                        .findOneOrEmpty();
                return photo;
            },
            executionContext);
    }

    /**
     * Insert a destination photo into the database
     *
     * @param photo the destination photo to be inserted in the database
     * @return the photo object
     */
    public CompletionStage<DestinationPhoto> insertDestinationPhoto(DestinationPhoto photo) {
        return supplyAsync(
            () -> {
                photo.insert();
                return photo;
            },
            executionContext);
    }

    /**
     * Soft delete a destination photo from the database
     *
     * @param destinationPhoto the DestinationPhoto to delete
     * @return the ID of the destination photo that was deleted
     */
    public CompletionStage<Integer> deleteDestinationPhoto(DestinationPhoto destinationPhoto) {
        return supplyAsync(
            () -> {
                destinationPhoto.setDeletedExpiry(
                    Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
                destinationPhoto.save();
                destinationPhoto.delete();
                return destinationPhoto.destinationPhotoId;
            },
            executionContext);
    }

    /**
     * Un-soft-delete a destination photo from the database
     *
     * @param destinationPhoto the DestinationPhoto to un-delete
     * @return the ID of the destination photo that was un-deleted
     */
    public CompletionStage<DestinationPhoto> undoDeleteDestinationPhoto(
        DestinationPhoto destinationPhoto) {
        return supplyAsync(
            () -> {
                destinationPhoto.setDeletedExpiry(null);
                destinationPhoto.setDeleted(false);
                destinationPhoto.save();
                return destinationPhoto;
            },
            executionContext);
    }

    /**
     * Creates a destination proposal
     *
     * @param proposal The proposal to create
     * @return The created proposal
     */
    public CompletionStage<DestinationProposal> createProposal(DestinationProposal proposal) {
        return supplyAsync(
            () -> {
                proposal.insert();
                return proposal;
            },
            executionContext);
    }

    /**
     * Finds a destination proposal by the given ID
     *
     * @param destinationProposalId the ID of the destination proposal to be search
     * @return The destinationProposal that corresponds to the ID
     */
    public CompletionStage<Optional<DestinationProposal>> getDestinationProposalById(
        int destinationProposalId) {
        return supplyAsync(
            () ->
                DestinationProposal.find
                    .query()
                    .fetch("travellerTypes")
                    .fetch("destination")
                    .where()
                    .eq("destinationProposalId", destinationProposalId)
                    .findOneOrEmpty(),
            executionContext);
    }

    /**
     * Deletes a destination proposal by finding the destination proposal's ID and deleting it with
     * the ID found
     *
     * @param destinationProposal the destination proposal to be deleted
     * @return the ID of the destination proposal that was deleted
     */
    public CompletionStage<Integer> deleteDestinationProposal(
        DestinationProposal destinationProposal) {
        return supplyAsync(
            () -> {
                destinationProposal.setDeletedExpiry(
                    Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
                destinationProposal.save();
                destinationProposal.delete(); // Soft delete
                return destinationProposal.getDestinationProposalId();
            },
            executionContext);
    }

    /**
     * Get a page of destination proposals
     *
     * @return the destination proposals
     */
    public CompletionStage<List<DestinationProposal>> getDestinationProposals(int page) {

        int pageSize = 5;
        int offset = (page - 1) * pageSize;

        return supplyAsync(() -> DestinationProposal.find.query()
                .setFirstRow(offset)
                .setMaxRows(pageSize)
                .findList());
    }

    /**
     * Gets the destination proposal with the given proposal ID including soft deleted proposals
     *
     * @param destinationProposalId the ID of the destination proposal to be searched
     * @return the proposal
     */
    public CompletionStage<Optional<DestinationProposal>> getDestinationProposalByIdWithSoftDelete(
        int destinationProposalId) {
        return supplyAsync(
            () -> {
                Optional<DestinationProposal> proposal =
                    DestinationProposal.find
                        .query()
                        .setIncludeSoftDeletes()
                        .where()
                        .eq("destination_proposal_id", destinationProposalId)
                        .findOneOrEmpty();
                return proposal;
            },
            executionContext);
    }

    /**
     * Undoes the deletion of the destination proposal
     *
     * @param destinationProposal the destination proposal that the deletion is undone
     * @return the destination proposal after the deletion is undone
     */
    public CompletionStage<DestinationProposal> undoDestinationProposalDelete(
        DestinationProposal destinationProposal) {
        return supplyAsync(
            () -> {
                destinationProposal.setDeleted(false);
                destinationProposal.setDeletedExpiry(null);
                destinationProposal.save();
                return destinationProposal;
        });
    }

    /**
     * Updates a destination proposal
     *
     * @param destinationProposal The destination proposal to update
     * @return The destination proposal that was updated
     */
    public CompletionStage<DestinationProposal> updateDestinationProposal(
        DestinationProposal destinationProposal) {
        return supplyAsync(
            () -> {
                destinationProposal.save();
                return destinationProposal;
            },
            executionContext);
    }
}
