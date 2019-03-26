package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
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
import util.TripUtil;

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
    private final TripUtil tripUtil;

    @Inject
    public TripController(TripRepository tripRepository, HttpExecutionContext httpExecutionContext, TripUtil tripUtil) {
        this.tripRepository = tripRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.tripUtil = tripUtil;
    }

    /**
     * Endpoint to add a trip
     * @param request Used to retrieve trip JSON
     * @return A result object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addTrip(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        JsonNode jsonBody = request.body().asJson();

        String tripName = jsonBody.get("tripName").asText();
        JsonNode tripDestinationsJson = jsonBody.get("tripDestinations");
        List<TripDestination> tripDestinations;
        try {
            tripDestinations = tripUtil.getTripDestinationsFromJson(tripDestinationsJson);
        } catch (BadRequestException e) {
           return supplyAsync(() -> badRequest());
        }

        Trip trip = new Trip(tripDestinations, user, tripName);

        return tripRepository.saveTrip(trip)
                .thenApplyAsync((updatedTrip) -> {
                    JsonNode tripIdJson = Json.toJson(trip.getTripId());
                    return created(tripIdJson);
                }, httpExecutionContext.current());
    }

    @With(LoggedIn.class)
    public CompletionStage<Result> getTrip(int travellerId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int userId = user.getUserId();

        return tripRepository.getTripByIds(tripId, userId)
                .thenApplyAsync((optionalTrip) -> {
                    if (!optionalTrip.isPresent())  {
                        return notFound();
                    }
                    Trip trip = optionalTrip.get();
                    JsonNode tripJson = Json.toJson(trip);
                    return ok(tripJson);
                });
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
                thenComposeAsync((optionalTrip) -> {
                    if (!optionalTrip.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    Trip trip = optionalTrip.get();
                    return tripRepository.deleteTrip(trip);
                }, httpExecutionContext.current())
                .thenApplyAsync((trip) -> (Result) ok(), httpExecutionContext.current())
                // Exceptions / error checking
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundError) {
                        return notFound("Trip id was not found");
                    } catch (Throwable serverError) {
                        return internalServerError();
                    }
                });
        }



        
    /**
     * Endpoint to get update a trips destinations
     * @param request Request body to get json body from
     * @param tripId The trip ID to update
     * @param userId The id of the user that the trip belongs to
     * @return Returns the http response which can be
     *         - Ok - Trip was updated successfully
     *
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTrip(Http.Request request, int userId, int tripId) {
        return tripRepository.getTripByIds(tripId, userId)
                .thenComposeAsync((optionalTrip) -> {
                    if (!optionalTrip.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }
                    JsonNode jsonBody = request.body().asJson();
                    String tripName = jsonBody.get("tripName").asText();
                    JsonNode tripDestinationsJson = jsonBody.get("tripDestinations");

                    List<TripDestination> tripDestinations;
                    try {
                         tripDestinations = tripUtil.getTripDestinationsFromJson(tripDestinationsJson);
                    } catch (BadRequestException e) {
                        throw new CompletionException(new BadRequestException());
                    }

                    Trip trip = optionalTrip.get();

                    trip.setTripDestinations(tripDestinations);
                    trip.setTripName(tripName);

                    return tripRepository.update(trip);
                }, httpExecutionContext.current())
                .thenApplyAsync((Destination) -> (Result) ok(), httpExecutionContext.current())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundError) {
                        return notFound();
                    } catch (BadRequestException badRequestError) {
                        return badRequest();
                    } catch (Throwable serverError) {
                        return internalServerError();
                    }
                });
    }
}

