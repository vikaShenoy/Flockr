package repository;

import models.Trip;

import javax.inject.Inject;
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
     * @param trip The trip to save
     * @return The saved trip
     */
    public CompletionStage<Trip> saveTrip(Trip trip) {
        return supplyAsync(() -> {
            trip.save();
            return trip;
        }, executionContext);
    }

    /**
     * Updates a trip in the database.
     * @param trip The trip to update changes.
     * @return The trip that was updated.
     */
    public CompletionStage<Trip> update(Trip trip) {
        return supplyAsync(() -> {
            trip.update();
            return trip;
        }, executionContext);
    }

    /**
     * Get a trip by its tripId and userId.
     * @param tripId The id of the trip to find.
     * @param userId The user id of the trip to find.
     * @return the trip that matches given ids.
     */
    public CompletionStage<Optional<Trip>> getTripByIds(int tripId, int userId) {
        return supplyAsync(() -> {
            Optional<Trip> trip = Trip.find.query().
                    where().eq("trip_id", tripId)
                    .eq("user_user_id", userId)
                    .findOneOrEmpty();
            return trip;
        }, executionContext);
    }
}
