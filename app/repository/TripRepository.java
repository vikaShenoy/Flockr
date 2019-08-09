package repository;

import models.TripComposite;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Contains all trip related db interactions
 */
public class TripRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public TripRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Saves a trip
     *
     * @param trip The trip to save
     * @return The saved trip
     */
    public CompletionStage<TripComposite> saveTrip(TripComposite trip) {
        return supplyAsync(() -> {
            trip.save();
            return trip;
        }, executionContext);
    }

    /**
     * Updates a trip in the database.
     *
     * @param trip The trip to update changes.
     * @return The trip that was updated.
     */
    public CompletionStage<TripComposite> update(TripComposite trip) {
        return supplyAsync(() -> {
            trip.update();
            return trip;
        }, executionContext);
    }

    /**
     * Delete a trip.
     *
     * @param trip The trip to delete.
     */
    public CompletionStage<TripComposite> deleteTrip(TripComposite trip) {
        return supplyAsync(() -> {
            trip.setDeletedExpiry(Timestamp.from(Instant.now().plus(Duration.ofHours(1))));
            trip.delete(); // Soft delete
            return trip;
        }, executionContext);
    }

    /**
     * Restore a deleted trip
     * @param trip the trip to be restored
     * @return
     */
    public CompletionStage<TripComposite> restoreTrip(TripComposite trip) {
        return supplyAsync(() -> {
            trip.setDeleted(false);
            trip.setDeletedExpiry(null);
            trip.save();
            return trip;
        });
    }

    /**
     * Get a trip by its tripId and userId.
     *
     * @param tripId The id of the trip to find.
     * @param userId The user id of the trip to find.
     * @return the trip that matches given ids.
     */
    public CompletionStage<Optional<TripComposite>> getTripByIds(int tripId, int userId) {
        return supplyAsync(() -> {
            Optional<TripComposite> trip = TripComposite.find.query()
                    .fetch("users")
                    .where().eq("trip_id", tripId)
                    .in("users.userId", userId)
                    .findOneOrEmpty();
            return trip;
        }, executionContext);
    }

    /**
     * Get a trip, including if it has been soft deleted, by its tripId and userId
     * @param tripId The id of the trip to get
     * @param userId The user id of the owner of the trip
     * @return the trip that matches the given ids
     */
    public CompletionStage<Optional<TripComposite>> getTripByIdsIncludingDeleted(int tripId, int userId) {
        return supplyAsync(() -> {
            Optional<TripComposite> trip = TripComposite.find.query().setIncludeSoftDeletes()
                    .fetch("users")
                    .where().eq("trip_id", tripId)
                    .in("users.userId", userId)
                    .findOneOrEmpty();
            return trip;
        }, executionContext);
    }

    /**
     * Get trips by the users userId
     *
     * @param travellerId The user id of the trips
     * @return The users trips
     */
    public CompletionStage<List<TripComposite>> getTripsByIds(int travellerId) {
        return supplyAsync(() -> {
            List<TripComposite> trip = TripComposite.find.query()
                    .fetch("users")
                    .where()
                    .in("users.userId", travellerId)
                    .findList();
            return trip;
        }, executionContext);
    }
}
