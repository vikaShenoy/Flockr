package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import exceptions.ServerErrorException;
import models.Destination;
import models.Trip;
import models.TripDestination;
import models.User;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TripRepository;

import javax.inject.Inject;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.libs.Json.newObject;

/**
 * Contains all trip related endpoints
 */
public class TripController extends Controller {

    private final TripRepository tripRepository;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public TripController(TripRepository tripRepository, HttpExecutionContext httpExecutionContext) {
        this.tripRepository = tripRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Endpoint to add a trip
     * @param request Used to retrieve trip JSON
     * @return A result object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addTrip(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        JsonNode jsonBody = request.body().asJson();

        String tripName = jsonBody.get("tripName").asText();
        JsonNode tripDestinationsJson = jsonBody.get("tripDestinations");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD");
        List<TripDestination> tripDestinations = new ArrayList<>();
        for (JsonNode tripDestinationJson : tripDestinationsJson) {
            int destinationId = tripDestinationJson.get("destinationId").asInt();
            Date arrivalDate = new Date(tripDestinationJson.get("arrivalDate").asLong());
            int arrivalTime = tripDestinationJson.get("arrivalTime").asInt();
            Date departureDate = new Timestamp(tripDestinationJson.get("departureDate").asLong());
            int departureTime = tripDestinationJson.get("departureTime").asInt();

            Destination destination = new Destination(null, null, null, null, null, null);
            destination.setDestinationId(destinationId);

            TripDestination tripDestination = new TripDestination(destination, arrivalDate, arrivalTime, departureDate, departureTime);
            tripDestinations.add(tripDestination);
        }

        Trip trip = new Trip(tripDestinations, user);

        return tripRepository.saveTrip(trip)
                .thenApplyAsync((updatedTrip) -> {
                    JsonNode tripIdJson = Json.toJson(trip.getTripId());
                    return ok(tripIdJson);
                }, httpExecutionContext.current());
    }

    /**
     * Endpoint to delete a user's trip.
     * @param travellerId The user who's trip is deleted.
     * @param tripId The trip to delete.
     * @param request HTTP req
     * @return A result object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteTrip(int travellerId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int userId = user.getUserId();
        return tripRepository.getTripByIds(tripId, userId).
                thenApplyAsync((optionalTrip) -> {
                    if (!optionalTrip.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    Trip trip = optionalTrip.get();
                    // Delete and update database to remove the trip.
                    trip.delete();
                    return tripRepository.update(trip);
                }, httpExecutionContext.current()).
                thenApplyAsync((Trip) -> (Result) ok("Successfully deleted the given trip id."), httpExecutionContext.current())
                // Exceptions / error checking
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundE) {
                        ObjectNode message = Json.newObject();
                        message.put("Message", "The given trip id can't be found.");
                        return notFound(message);
                    } catch (Throwable ee) {
                        return internalServerError();
                    }
                });
        }
}

