package controllers;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import actions.ActionState;
import actions.LoggedIn;
import models.*;
import modules.websocket.ConnectedUsers;
import modules.websocket.TripNotifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.text.PathProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.DestinationRepository;
import repository.TripRepository;
import repository.UserRepository;
import util.ExceptionUtil;
import util.Security;
import util.TripUtil;
import java.util.stream.Collectors;

/**
 * Controller for trip related endpoints.
 */
public class TripController extends Controller {

    private static final String TRIP_OWNER = "TRIP_OWNER";
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final TripUtil tripUtil;
    private final DestinationRepository destinationRepository;
    private final TripNotifier tripNotifier;
    private final ExceptionUtil exceptionUtil;

    @Inject
    public TripController(
        TripRepository tripRepository,
        UserRepository userRepository,
        HttpExecutionContext httpExecutionContext,
        TripUtil tripUtil,
        DestinationRepository destinationRepository,
        TripNotifier tripNotifier,
        ExceptionUtil exceptionUtil) {
        this.tripRepository = tripRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.tripUtil = tripUtil;
        this.userRepository = userRepository;
        this.destinationRepository = destinationRepository;
        this.tripNotifier = tripNotifier;
        this.exceptionUtil = exceptionUtil;
    }

    /**
     * Endpoint to add a trip.
     *
     * @param userId  id of the user to add a trip for.
     * @param request Used to retrieve trip JSON.
     * @return A completion stage with an HTTP result which can be
     * - 200 - Trip was successfully created
     * - 403 - User does not have permission to add trip
     * - 400 - Problem with request body
     * - 404 - User or trip could not be found
     * - 500 - Any other internal server error
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addTrip(int userId, Http.Request request) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (Security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }
        JsonNode jsonBody = request.body().asJson();
        String tripName = jsonBody.get("name").asText();
        JsonNode tripNodesJson = jsonBody.get("tripNodes");
        JsonNode userIdsJson = jsonBody.get("userIds");

    return userRepository
        .getUserById(userId)
        .thenComposeAsync(
            optionalUser -> {
              if (!optionalUser.isPresent()) {
                throw new CompletionException(new BadRequestException("User does not exist"));
              }

              User user = optionalUser.get();

              List<TripNode> tripNodes;
              List<User> users;

              try {
                List<TripComposite> trips = tripRepository.getTripsByOwnUserId(userId);
                tripNodes = tripUtil.getTripNodesFromJson(tripNodesJson, trips);
                users = tripUtil.getUsersFromJson(userIdsJson, user);
              } catch (BadRequestException e) {
                return CompletableFuture.completedFuture(badRequest(e.getMessage()));
              } catch (ForbiddenRequestException e) {
                return CompletableFuture.completedFuture(forbidden(e.getMessage()));
              } catch (NotFoundException e) {
                return CompletableFuture.completedFuture(notFound(e.getMessage()));
              }

              List<CompletionStage<Destination>> updateDestinations =
                  checkAndUpdateOwners(userId, tripNodes);

              return CompletableFuture.allOf(updateDestinations.toArray(new CompletableFuture[0]))
                  .thenComposeAsync(
                      destinations -> {
                        TripComposite trip = new TripComposite(tripNodes, users, tripName);
                        List<UserRole> userRoles = new ArrayList<>();
                        for (User roledUser : users) {
                          for (JsonNode userIdJson : userIdsJson) {
                            if (userIdJson.get("userId").asInt() == roledUser.getUserId()) {
                              Role role =
                                  userRepository.getSingleRoleByType(
                                      userIdJson.get("role").asText());
                              UserRole userRole = new UserRole(roledUser, role);
                              userRole.save();
                              userRoles.add(userRole);
                            }
                          }
                        }

                        Role role = userRepository.getSingleRoleByType(TRIP_OWNER);
                        User owner = users.get(users.size() - 1);
                        UserRole userRole = new UserRole(owner, role);
                        userRole.save();
                        userRoles.add(userRole);
                        trip.setUserRoles(userRoles);
                        trip.save();

                        return tripRepository.saveTrip(trip);
                      })
                  .thenApplyAsync(updatedTrip -> created(Json.toJson(updatedTrip)))
                  .exceptionally(exceptionUtil::getResultFromError);
            },
            httpExecutionContext.current());
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

        if (Security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository
                .getTripByIds(tripId, userId)
                .thenApplyAsync(
                        optionalTrip -> {
                            if (!optionalTrip.isPresent()) {
                                return notFound();
                            }
                            TripComposite trip = optionalTrip.get();
                            JsonNode tripJson = Json.toJson(trip);
                            List<User> connectedUsersInTrip = new ArrayList<>();
                            List<User> usersInTrip = trip.getUsers();
                            ConnectedUsers connectedUsers = ConnectedUsers.getInstance();


                            for (User currentUser : usersInTrip) {
                                if (connectedUsers.isUserConnected(currentUser)) {
                                    connectedUsersInTrip.add(currentUser);
                                }
                            }
                            ((ObjectNode) tripJson).set("connectedUsers", Json.toJson(connectedUsersInTrip));
                            return ok(tripJson);
                        });
    }

    /**
     * Endpoint to delete a user's trip.
     *
     * @param userId  The user who's trip is deleted.
     * @param tripId  The trip to delete.
     * @param request HTTP req
     * @return A result object, with status code 200 if successful. 400 if the trip isn't found. 500
     * for other errors.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (Security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository
                .getTripByIds(tripId, userId)
                .thenComposeAsync(
                        optionalTrip -> {
                            if (!optionalTrip.isPresent()) {
                                throw new CompletionException(new NotFoundException());
                            }

                            TripComposite trip = optionalTrip.get();

                            String permissionLevel = getTripUserPermissionLevel(trip, user);
                            if (permissionLevel.equals("TRIP_MEMBER")) {
                                throw new CompletionException(
                                        new ForbiddenRequestException(
                                                "You do not have permission to delete this trip."));
                            }

                            return tripRepository.deleteTrip(trip);
                        },
                        httpExecutionContext.current())
                .thenApplyAsync(trip -> (Result) ok(), httpExecutionContext.current())
                // Exceptions / error checking
                .exceptionally(exceptionUtil::getResultFromError);
    }

    /**
     * Undo the soft-delete for a trip.
     *
     * @param userId  user who owns the trip.
     * @param tripId  id of the trip to un-soft delete.
     * @param request HTTP request object.
     * @return - 200 with trip if successful - 400 bad request if the trip hasn't been deleted - 401
     * unauthorized if the user is unauthorized - 403 forbidden if the user isn't allowed to undo
     * the delete - 404 not found if the trip can't be found in the db - 500 internal server error
     * for other errors
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> restoreTrip(int userId, int tripId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        if (Security.userHasPermission(user, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository
                .getTripByIdsIncludingDeleted(tripId, userId)
                .thenComposeAsync(
                        optionalTrip -> {
                            if (!optionalTrip.isPresent()) {
                                throw new CompletionException(new NotFoundException());
                            }

                            TripNode trip = optionalTrip.get();
                            if (!user.isAdmin() && user.getUserId() != userId) {
                                throw new CompletionException(
                                        new ForbiddenRequestException(
                                                "You do not have permission to undo this deletion."));
                            }
                            if (!trip.isDeleted()) {
                                throw new CompletionException(
                                        new BadRequestException("This trip has not been deleted."));
                            }

                            return tripRepository.restoreTrip(trip);
                        })
                .thenApplyAsync(trip -> ok(Json.toJson(trip)))
                .exceptionally(exceptionUtil::getResultFromError);
    }

    /**
     * Endpoint to update a trips destinations.
     *
     * @param request Request body to get json body from
     * @param tripId  The trip ID to update
     * @param userId  The id of the user that the trip belongs to
     * @return Returns the http response which can be
     * - 200 - Trip was updated successfully
     * - 401 - User was not authenticated
     * - 403 - User does not have permission to create the trip
     * - 404 - User or trip could not be found
     * - 400 - there was an error with the request.
     * - 500 - there was an internal server error.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTrip(Http.Request request, int userId, int tripId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);
        if (Security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository
                .getTripByIds(tripId, userId)
                .thenComposeAsync(
                        optionalTrip -> {
                            if (!optionalTrip.isPresent()) {
                                throw new CompletionException(new NotFoundException());
                            }
                            JsonNode jsonBody = request.body().asJson();
                            String tripName = jsonBody.get("name").asText();
                            JsonNode tripNodesJson = jsonBody.get("tripNodes");
                            JsonNode userIdsJson = jsonBody.get("userIds");

                            List<TripNode> tripNodes;
                            List<User> users;
                            List<Integer> userIds = new ArrayList<>();

                            for (JsonNode currentUserJson : userIdsJson) {
                              userIds.add(currentUserJson.get("userId").asInt());
                            }

                            try {
                                List<User> allUsers = userRepository.getUsersWithIds(userIds);
                                List<TripComposite> trips = tripRepository.getTripsByOwnUserId(userId);
                                tripNodes = tripUtil.getTripNodesFromJson(tripNodesJson, trips);
                                users = tripUtil.getUsersFromJsonEdit(userIdsJson, allUsers);

                            } catch (BadRequestException e) {
                                throw new CompletionException(new BadRequestException());
                            } catch (ForbiddenRequestException e) {
                                return CompletableFuture.completedFuture(forbidden(e.getMessage()));
                            } catch (NotFoundException e) {
                                System.out.println("It is not found");
                                return CompletableFuture.completedFuture(notFound(e.getMessage()));
                            }

                            List<CompletionStage<Destination>> updateDestinations =
                                    checkAndUpdateOwners(userId, tripNodes);

                            return CompletableFuture.allOf(updateDestinations.toArray(new CompletableFuture[0]))
                                    .thenComposeAsync(
                                            destinations -> {
                                                TripComposite trip = optionalTrip.get();

                                                // CHECK IF USER IS AN ADMIN
                                                String userPermissionLevel = getTripUserPermissionLevel(
                                                        trip, userFromMiddleware);

                                                // Trip members can't edit anything.
                                                if (userPermissionLevel.equals("TRIP_MEMBER")) {
                                                    throw new CompletionException(
                                                            new ForbiddenRequestException(
                                                                    "You do not have permission to edit this trip."));
                                                }

                                                // Delete old destination leaf nodes.
                                                tripRepository.deleteListOfTrips(trip.getTripNodes());
                                                trip.setTripNodes(tripNodes);
                                                trip.setName(tripName);

                                                // Set users/permissions. Only trip owners have permission.
                                                List<UserRole> tripUserRoles = new ArrayList<>();
                                                if (userPermissionLevel.equals(TRIP_OWNER)) {
                                                    if (tripNodesJson != null) {
                                                        trip.setUsers(users);
                                                    }

                                                    long roleStartTime = System.nanoTime();
                                                    for (User user : users) {
                                                        for (JsonNode userJson : userIdsJson) {
                                                            if (userJson.get("userId").asInt() == user.getUserId()) {
                                                                Role role = userRepository.getSingleRoleByType(
                                                                        userJson.get("role").asText());
                                                                UserRole userRole = new UserRole(user, role);
                                                                userRole.save();
                                                                tripUserRoles.add(userRole);
                                                            }
                                                        }
                                                    }

                                                    trip.setUserRoles(tripUserRoles);
                                                }

                                                return tripRepository.update(trip);
                                            })
                                    .thenApplyAsync(optionalUpdatedTrip -> {
                                        tripNotifier.notifyTripUpdate(userFromMiddleware, optionalUpdatedTrip);
                                        return ok();
                                    });
                        },
                        httpExecutionContext.current())
                .exceptionally(exceptionUtil::getResultFromError);
    }

    /**
     * Finds the highest trip permission level a user has.
     * NOTE - if the user is a system admin they automatically get the highest permission level.
     * @param trip the trip the permission level is being found for.
     * @param user user to find permission level for.
     * @return a string of the user's highest permission level.
     */
    private String getTripUserPermissionLevel(TripComposite trip, User user) {
        List<String> tripUserRoles = trip.getUserRoles().stream().
                filter(tripUserRole -> tripUserRole.getUser().getUserId() == user.getUserId()).
                map(userRole -> userRole.getRole().getRoleType()).collect(
                Collectors.toList());
        if (tripUserRoles.contains(TRIP_OWNER) || user.isAdmin()) {
            return TRIP_OWNER;
        } else if (tripUserRoles.contains("TRIP_MANAGER")) {
            return "TRIP_MANAGER";
        }
        return "TRIP_MEMBER";
    }

    /**
     * Creates a list of completable futures that: Check the owners of each destination and updates
     * them to null if they meet ALL of the following criteria: - The destination is public - The
     * owner is not already null - The user is not the owner
     *
     * @param userId           the id of the user that owns the trip.
     * @param tripDestinations the destinations of the trip.
     * @return List&lt CompletionStage&lt Destination &gt &gt the list of completion stages.
     */
    private List<CompletionStage<Destination>> checkAndUpdateOwners(int userId, List<TripNode> tripDestinations) {
        List<CompletionStage<Destination>> updateDestinations = new ArrayList<>();
        for (TripNode tripDestination : tripDestinations) {
            if (tripDestination.getNodeType().equals("TripDestinationLeaf")) {
                CompletionStage<Destination> updateDestination =
                        destinationRepository.getDestinationById(
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
                                            } else if (destination.isPresent()) {
                                            return destination.get();
                                            }
                                            return null;
                                        }
                                );
                updateDestinations.add(updateDestination);

            }
        }
        return updateDestinations;
    }

    /**
     * Endpoint to get a users' trips.
     *
     * @param request - Request object to get the users ID
     * @param userId  - Irrelevant ID for consistency reasons
     * @return Returns the http response which can be - 200 - Returns the list of trips
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTrips(Http.Request request, int userId) {
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!userFromMiddleware.isAdmin() && userId != userFromMiddleware.getUserId()) {
            return supplyAsync(() -> ok(Json.toJson(new ArrayList<>())));
        }

        return tripRepository
                .getTripsByUserId(userId)
                .thenApplyAsync(
                        trips -> {
                            JsonNode tripsJson = Json.toJson(trips);
                            return ok(tripsJson);
                        },
                        httpExecutionContext.current());
    }

    /**
     * Retrieves a list of a user's trips that have no parent (High Level)
     *
     * @param userId  of the owner of the trips
     * @param request the http request.
     * @return the result to return to the client.
     * Status Codes:
     *    200 - OK - get trips successful.
     *    401 - Unauthorized - user not logged in.
     *    403 - Forbidden - user does not have permission.
     *    500 - Internal Server Error - Unexpected Error.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getHighLevelTrips(int userId, Http.Request request) {
        User middlewareUser = request.attrs().get(ActionState.USER);

        if (userId != middlewareUser.getUserId() && !middlewareUser.isAdmin()) {
            return supplyAsync(Controller::forbidden);
        }

        return tripRepository
                .getHighLevelTripsByUserId(userId)
                .thenApplyAsync(
                        trips -> {
                            List<TripComposite> tripComposites = new ArrayList<>();
                            for (TripComposite tripComposite : trips) {
                                if (tripComposite.getParents().isEmpty()) {
                                    tripComposites.add(tripComposite);
                                } else {
                                    List<TripNode> parents = tripComposite.getParents();
                                    List<Integer> userIds = new ArrayList<>();
                                    for (TripNode parent : parents) {
                                        List<User> users = parent.getUsers();
                                        for (User user : users) {
                                            userIds.add(user.getUserId());
                                        }
                                    }
                                    if (!userIds.contains(userId)) {
                                        tripComposites.add(tripComposite);
                                    }
                                }
                            }

                            PathProperties pathProperties = PathProperties.parse("tripNodeId, name");
                            String tripsJson = Ebean.json().toJson(tripComposites, pathProperties);
                            return ok(Json.parse(tripsJson));
                        },
                        httpExecutionContext.current());
    }
}
