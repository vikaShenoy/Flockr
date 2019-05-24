package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
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
import repository.DestinationRepository;
import repository.UserRepository;
import repository.TripRepository;
import util.Security;
import util.TripUtil;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;


/**
 * Controller for trip related endpoints.
 */
public class TripController extends Controller {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final DestinationRepository destinationRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final TripUtil tripUtil;
    private final Security security;

    @Inject
    public TripController(TripRepository tripRepository, Security security, UserRepository userRepository, HttpExecutionContext httpExecutionContext, TripUtil tripUtil, DestinationRepository destinationRepository) {
        this.tripRepository = tripRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.tripUtil = tripUtil;
        this.security = security;
        this.userRepository = userRepository;
        this.destinationRepository = destinationRepository;
    }

    /**
     * Endpoint to add a trip.
     * @param userId id of the user to add a trip for.
     * @param request Used to retrieve trip JSON.
     * @return 200 status code if successful. 400 if bad request error.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addTrip(int userId, Http.Request request) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        JsonNode jsonBody = request.body().asJson();

        String tripName = jsonBody.get("tripName").asText();
        JsonNode tripDestinationsJson = jsonBody.get("tripDestinations");
        List<TripDestination> tripDestinations;
        try {
            tripDestinations = tripUtil.getTripDestinationsFromJson(tripDestinationsJson);

        } catch (BadRequestException e) {
           return supplyAsync(() -> badRequest());
        }

        return userRepository.getUserById(userId)
                .thenComposeAsync(optionalUser -> {
                    if (!optionalUser.isPresent()) {
                        throw new CompletionException(new BadRequestException("User does not exist"));
                    }

                    User user = optionalUser.get();

                    for (TripDestination tripDestination : tripDestinations) {
                        Destination destination = tripDestination.getDestination();
                        if (destination.getIsPublic() && destination.getDestinationOwner() != null) {
                            //TODO Check that this code actually workss
                            destination.setDestinationOwner(null);
                            destinationRepository.update(destination);
                        }
                    }

                    Trip trip = new Trip(tripDestinations, user, tripName);

                    return tripRepository.saveTrip(trip);
                    }, httpExecutionContext.current())
               .thenApplyAsync((updatedTrip) -> {
                JsonNode tripIdJson = Json.toJson(updatedTrip.getTripId());
                return created(tripIdJson);
               });
    }

    /**
     * Endpoint to get a trip's information.
     * @param userId user who the trip belongs to.
     * @param tripId id of the trip to retrieve.
     * @param request incoming http request.
     * @return 200 status code with the trip json if successful, 404 if the trip cannot be found.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

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
     * @param userId The user who's trip is deleted.
     * @param tripId The trip to delete.
     * @param request HTTP req
     * @return A result object, with status code 200 if successful. 400 if the trip isn't found.
     * 500 for other errors.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

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
     * Endpoint to update a trips destinations.
     * @param request Request body to get json body from
     * @param tripId The trip ID to update
     * @param userId The id of the user that the trip belongs to
     * @return Returns the http response which can be
     *         - Ok - Trip was updated successfully
     *         - 400 - there was an error with the request.
     *         - 500 - there was an internal server error.
     *
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTrip(Http.Request request, int userId, int tripId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

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

                    for (TripDestination tripDestination : tripDestinations) {
                        Destination destination = tripDestination.getDestination();
                        if (destination.getIsPublic() && destination.getDestinationOwner() != null) {
                            //TODO Check that this code actually works
                            destination.setDestinationOwner(null);
                            destinationRepository.update(destination);
                        }
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

    /**
     * Endpoint to get a users trips
     * @param request - Request object to get the users ID
     * @param userId - Irrelevant ID for consistency reasons
     * @return Returns the http response which can be
     *         - 200 - Returns the list of trips
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTrips(Http.Request request, int userId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository.getTripsByIds(userId)
                .thenApplyAsync((trips) -> {
                    JsonNode tripsJson = Json.toJson(trips);
                    return ok(tripsJson);
                }, httpExecutionContext.current());
    }


}

