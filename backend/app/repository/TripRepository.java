package repository;

import models.Trip;

import javax.inject.Inject;
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
}
