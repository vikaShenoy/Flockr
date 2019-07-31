package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import exceptions.UnauthorizedException;
import models.Destination;
import models.Trip;
import models.TripDestination;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;
import repository.DestinationRepository;
import repository.UserRepository;
import repository.TripRepository;
import util.Security;
import util.TripUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    final Logger log = LoggerFactory.getLogger(this.getClass());


    @Inject
    public TripController(TripRepository tripRepository, Security security, UserRepository userRepository,
                          HttpExecutionContext httpExecutionContext, TripUtil tripUtil,
                          DestinationRepository destinationRepository) {
        this.tripRepository = tripRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.tripUtil = tripUtil;
        this.security = security;
        this.userRepository = userRepository;
        this.destinationRepository = destinationRepository;
    }

    /**
     * Endpoint to add a trip.
     *
     * @param userId  id of the user to add a trip for.
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
            return supplyAsync(Results::badRequest);
        }

        return userRepository.getUserById(userId)
                .thenComposeAsync(optionalUser -> {
                    if (!optionalUser.isPresent()) {
                        throw new CompletionException(new BadRequestException("User does not exist"));
                    }

                    User user = optionalUser.get();

                    List<CompletionStage<Destination>> updateDestinations = checkAndUpdateOwners(userId,
                            tripDestinations);

                    return CompletableFuture.allOf(updateDestinations.toArray(new CompletableFuture[0]))
                            .thenComposeAsync(destinations -> {
                                Trip trip = new Trip(tripDestinations, user, tripName);

                                return tripRepository.saveTrip(trip);
                            });
                }, httpExecutionContext.current())
                .thenApplyAsync(updatedTrip -> {
                    JsonNode tripIdJson = Json.toJson(updatedTrip.getTripId());
                    return created(tripIdJson);
                });
    }

    /**
     * Endpoint to get a trip's information.
     *
     * @param userId  user who the trip belongs to.
     * @param tripId  id of the trip to retrieve.
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
                .thenApplyAsync(optionalTrip -> {
                    if (!optionalTrip.isPresent()) {
                        return notFound();
                    }
                    Trip trip = optionalTrip.get();
                    JsonNode tripJson = Json.toJson(trip);
                    return ok(tripJson);
                });
    }


    /**
     * Endpoint to delete a user's trip.
     *
     * @param userId  The user who's trip is deleted.
     * @param tripId  The trip to delete.
     * @param request HTTP req
     * @return A result object, with status code 200 if successful. 400 if the trip isn't found.
     * 500 for other errors.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(user, userId)) {
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
     * Undo the soft-delete for a trip.
     * @param userId user who owns the trip.
     * @param tripId id of the trip to un-soft delete.
     * @param request HTTP request object.
     * @return
     * - 200 with trip if successful
     * - 400 bad request if the trip hasn't been deleted
     * - 401 unauthorized if the user is unauthorized
     * - 403 forbidden if the user isn't allowed to undo the delete
     * - 404 not found if the trip can't be found in the db
     * - 500 internal server error for other errors
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> restoreTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository.getTripByIdsIncludingDeleted(tripId, userId).
                thenComposeAsync((optionalTrip) -> {
                    if (!optionalTrip.isPresent()) {
                        throw new CompletionException(new NotFoundException());
                    }

                    Trip trip = optionalTrip.get();
                    if (!user.isAdmin() && user.getUserId() != userId) {
                        throw new CompletionException(new ForbiddenRequestException(
                                "You do not have permission to undo this deletion."));
                    }
                    if (!trip.isDeleted()) {
                        throw new CompletionException(new BadRequestException("This trip has not been deleted."));
                    }

                    return tripRepository.restoreTrip(trip);
                })
                .thenApplyAsync(trip -> ok(Json.toJson(trip)))
                .exceptionally(error -> {
                    ObjectNode message = Json.newObject();
                    try {
                        throw error.getCause();
                    } catch (BadRequestException e) {
                        message.put("message", e.getMessage());
                        return badRequest(message);
                    } catch (UnauthorizedException e) {
                        message.put("message", e.getMessage());
                        return unauthorized(message);
                    } catch (ForbiddenRequestException e) {
                        message.put("message", e.getMessage());
                        return forbidden(message);
                    } catch (NotFoundException e) {
                        message.put("message", e.getMessage());
                        return notFound(message);
                    } catch (Throwable e) {
                        log.error("An unexpected error has occurred", e);
                        return internalServerError();
                    }
                });

    }


    /**
     * Endpoint to update a trips destinations.
     *
     * @param request Request body to get json body from
     * @param tripId  The trip ID to update
     * @param userId  The id of the user that the trip belongs to
     * @return Returns the http response which can be
     * - 200 - Trip was updated successfully
     * - 400 - there was an error with the request.
     * - 500 - there was an internal server error.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTrip(Http.Request request, int userId, int tripId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository.getTripByIds(tripId, userId)
                .thenComposeAsync(optionalTrip -> {
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

                    List<CompletionStage<Destination>> updateDestinations = checkAndUpdateOwners(userId,
                            tripDestinations);
                    return CompletableFuture.allOf(updateDestinations.toArray(new CompletableFuture[0]))
                            .thenComposeAsync(destinations -> {
                                Trip trip = optionalTrip.get();

                                trip.setTripDestinations(tripDestinations);
                                trip.setTripName(tripName);

                                return tripRepository.update(trip);
                            });
                }, httpExecutionContext.current())
                .thenApplyAsync(trip -> ok(Json.toJson(trip)), httpExecutionContext.current())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundError) {
                        return notFound();
                    } catch (BadRequestException badRequestError) {
                        return badRequest();
                    } catch (Throwable serverError) {
                        serverError.printStackTrace();
                        return internalServerError();
                    }
                });
    }

    /**
     * Creates a list of completable futures that:
     * Check the owners of each destination and updates them to null if they meet ALL of the following criteria:
     * - The destination is public
     * - The owner is not already null
     * - The user is not the owner
     *
     * @param userId           the id of the user that owns the trip.
     * @param tripDestinations the destinations of the trip.
     * @return List&lt CompletionStage&lt Destination &gt &gt the list of completion stages.
     */
    private List<CompletionStage<Destination>> checkAndUpdateOwners(int userId, List<TripDestination> tripDestinations) {
        List<CompletionStage<Destination>> updateDestinations = new ArrayList<>();
        for (TripDestination tripDestination : tripDestinations) {

            CompletionStage<Destination> updateDestination = destinationRepository.getDestinationById(
                    tripDestination.getDestination().getDestinationId())
                    .thenApplyAsync(destination -> {
                                if (destination.isPresent() &&  // The destination exists
                                        // The destination is public
                                        destination.get().getIsPublic() &&
                                        // The owner is not already null
                                        destination.get().getDestinationOwner() != null &&
                                        // The user doesn't own the destination
                                        !destination.get().getDestinationOwner().equals(userId)) {
                                    destination.get().setDestinationOwner(null);
                                    destinationRepository.update(destination.get());
                                }
                                return destination.get();
                            }
                    );
            updateDestinations.add(updateDestination);
        }
        return updateDestinations;
    }

    /**
     * Endpoint to get a users' trips.
     *
     * @param request - Request object to get the users ID
     * @param userId  - Irrelevant ID for consistency reasons
     * @return Returns the http response which can be
     * - 200 - Returns the list of trips
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTrips(Http.Request request, int userId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!userFromMiddleware.isAdmin() && userId != userFromMiddleware.getUserId()) {
            return supplyAsync(() -> ok(Json.toJson(new ArrayList<>())));
        }

        return tripRepository.getTripsByIds(userId)
                .thenApplyAsync((trips) -> {
                    JsonNode tripsJson = Json.toJson(trips);
                    return ok(tripsJson);
                }, httpExecutionContext.current());
    }


}

