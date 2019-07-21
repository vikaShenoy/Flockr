package controllers;

import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import actions.ActionState;
import models.TreasureHunt;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Http.Request;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.DatabaseExecutionContext;
import play.mvc.With;
import repository.TreasureHuntRepository;
import repository.UserRepository;
import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletionException;
import java.util.Date;
import java.util.concurrent.CompletionStage;


/**
 * Contains all end points associated with treasure hunts.
 */
public class TreasureHuntController extends Controller {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private TreasureHuntRepository treasureHuntRepository;
    private final DatabaseExecutionContext executionContext;
    private UserRepository userRepository;

    /**
     * Dependency Injection.
     *
     * @param treasureHuntRepository the treasure hunt repository.
     * @param userRepository         the user repository.
     * @param executionContext       the context to execute the async functions.
     */
    @Inject
    public TreasureHuntController(TreasureHuntRepository treasureHuntRepository, UserRepository userRepository, DatabaseExecutionContext executionContext) {
        this.treasureHuntRepository = treasureHuntRepository;
        this.userRepository = userRepository;
        this.executionContext = executionContext;
    }

    /**
     * Method for the PUT /treasurehunts/:treasureHuntId endpoint.
     * Edits a treasure hunt if the user has permission and returns one of the following responses via HTTP:
     * - 200 - When treasure hunt is successfully modified.
     * - 400 - When the data given in the request is invalid.
     * - 401 - When the user is not authorized.
     * - 403 - When the user does not have permission.
     * - 404 - When the treasure hunt or user does not exist.
     *
     * @param request        the http request.
     * @param treasureHuntId the id of the treasure hunt.
     * @return the completion stage containing the result.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> editTreasureHunt(Request request, int treasureHuntId) {
        return treasureHuntRepository.getTreasureHuntById(treasureHuntId)
                .thenComposeAsync((optionalTreasureHunt) -> {
                    if (!optionalTreasureHunt.isPresent()) {
                        throw new CompletionException(new NotFoundException("Treasure Hunt not found."));
                    }
                    TreasureHunt treasureHunt = optionalTreasureHunt.get();

                    User userFromMiddleWare = request.attrs().get(ActionState.USER);
                    if (!userFromMiddleWare.isAdmin() && userFromMiddleWare.getUserId() != treasureHunt.getOwnerId()) {
                        throw new CompletionException(new ForbiddenRequestException("You do not have permission to perform this action."));
                    }

                    JsonNode jsonBody = request.body().asJson();
                    if (jsonBody.has("treasureHuntName")) {
                        String treasureHuntName = jsonBody.get("treasureHuntName").asText();
                        if (treasureHuntName.length() == 0) {
                            throw new CompletionException(new BadRequestException("Treasure hunt name is required."));
                        } else if (treasureHuntName.length() > 255) {
                            throw new CompletionException(new BadRequestException("Treasure hunt name must be less than 255 characters."));
                        }
                        treasureHunt.setTreasureHuntName(treasureHuntName);
                    }
                    if (jsonBody.has("treasureHuntDestinationId")) {
                        int destinationId = jsonBody.get("treasureHuntDestinationId").asInt();
                        try {
                            treasureHunt.setTreasureHuntDestinationId(destinationId);
                        } catch (NotFoundException e) {
                            throw new CompletionException(e);
                        }
                    }
                    if (jsonBody.has("riddle")) {
                        String riddle = jsonBody.get("riddle").asText();
                        if (riddle.length() == 0) {
                            throw new CompletionException(new BadRequestException("Riddle is required."));
                        }
                        treasureHunt.setRiddle(riddle);
                    }
                    if (jsonBody.has("endDate")) {
                        String endDateString = jsonBody.get("endDate").asText();
                        try {
                            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateString);
                            if (!jsonBody.has("startDate")) {
                                if (endDate.before(treasureHunt.getStartDate())) {
                                    throw new CompletionException(new BadRequestException("End date cannot be before start date."));
                                }
                            }
                            treasureHunt.setEndDate(endDate);
                        } catch (ParseException e) {
                            throw new CompletionException(new BadRequestException("End date must be of format yyyy-mm-dd"));
                        }
                    }
                    if (jsonBody.has("startDate")) {
                        String startDateString = jsonBody.get("startDate").asText();
                        try {
                            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
                            if (startDate.after(treasureHunt.getEndDate())) {
                                throw new CompletionException(new BadRequestException("Start date cannot be before end date."));
                            }
                            treasureHunt.setStartDate(startDate);
                        } catch (ParseException e) {
                            throw new CompletionException(new BadRequestException("Start date must be of format yyyy-mm-dd"));
                        }
                    }
                    return treasureHuntRepository.modifyTreasureHunt(treasureHunt);
                }, executionContext)
                .thenApplyAsync(result -> ok(Json.toJson(result)), executionContext)
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", notFoundException.getMessage());
                        return notFound(message);
                    } catch (BadRequestException badRequestException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", badRequestException.getMessage());
                        return badRequest(message);
                    } catch (ForbiddenRequestException forbiddenRequestException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", forbiddenRequestException.getMessage());
                        return forbidden(message);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        return internalServerError();
                    }
                });
    }

    /**
     * Method for the DELETE /treasurehunts/:treasureHuntId endpoint.
     * Edits a treasure hunt if the user has permission and returns one of the following responses via HTTP:
     * - 200 - When treasure hunt is successfully deleted.
     * - 401 - When the user is not authorized.
     * - 403 - When the user does not have permission.
     * - 404 - When the treasure hunt does not exist.
     *
     * @param request        the http request.
     * @param treasureHuntId the id of the treasure hunt.
     * @return the completion stage containing the result.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteTreasureHunt(Request request, int treasureHuntId) {
        return treasureHuntRepository.getTreasureHuntById(treasureHuntId)
                .thenComposeAsync(optionalTreasureHunt -> {
                    if (!optionalTreasureHunt.isPresent()) {
                        throw new CompletionException(new NotFoundException("Treasure hunt not found"));
                    }
                    TreasureHunt treasureHunt = optionalTreasureHunt.get();

                    User userFromMiddleWare = request.attrs().get(ActionState.USER);
                    if (!userFromMiddleWare.isAdmin() && userFromMiddleWare.getUserId() != treasureHunt.getOwnerId()) {
                        throw new CompletionException(new ForbiddenRequestException("You do not have permission to perform this action."));
                    }

                    return treasureHuntRepository.removeTreasureHunt(treasureHunt);
                })
                .thenApplyAsync(deleted -> {
                    if (deleted) return ok(Json.newObject()); // Strange error if json is removed...
                    else return internalServerError();
                })
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundException) {
                        return notFound(notFoundException.getMessage());
                    } catch (ForbiddenRequestException forbiddenRequestException) {
                        return forbidden(forbiddenRequestException.getMessage());
                    } catch (Throwable throwable) {
                        return internalServerError();
                    }
                });
    }

    /**
     * Endpoint to create a new treasure hunt for a destination which a user owns.
     * @param request HTTP post request containing treasure hunt parameters
     * @param userId user who creates (owns) the treasure hunt
     * @return
     *  - 201 for successful creation
     *  - 400 for bad request (parameters not provided etc.)
     *  - 401 if unauthorised
     *  - 404 if the user or destination can't be found.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addTreasureHunt(Http.Request request, int userId) {
        return userRepository.getUserById(userId)
                .thenApplyAsync((optUser) -> {
                    if (!optUser.isPresent()) {
                        ObjectNode message = Json.newObject().put("message", "User with id " + userId +
                                " was not found");
                        return notFound(message);
                    }

                    try {
                        JsonNode jsonBody = request.body().asJson();
                        String treasureHuntName = jsonBody.get("treasureHuntName").asText();
                        String riddle = jsonBody.get("riddle").asText();
                        String startDateString = jsonBody.get("startDate").asText();

                        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
                        String endDateString = jsonBody.get("startDate").asText();
                        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateString);
                        int destinationId = jsonBody.get("treasureHuntDestinationId").asInt();

                        TreasureHunt treasureHunt = new TreasureHunt(treasureHuntName, userId, destinationId, riddle, startDate,
                                endDate);
                        User user = optUser.get();
                        treasureHunt.setOwnerId(user.getUserId());
                        treasureHunt.save();
                        int treasureHuntId = treasureHunt.getTreasureHuntId();
                        return created(Json.newObject().put("treasureHuntId", treasureHuntId));
                    } catch (NotFoundException e) {
                        ObjectNode message = Json.newObject().put("message", "Destination not found");
                        return notFound(message);
                    } catch (NullPointerException e) {
                        return badRequest(Json.newObject().put("Message", "Insufficient data provided."));
                    } catch (Exception e) {
                        System.out.println(e);
                        return internalServerError();
                    }
                }, executionContext);
    }

    /**
     * Endpoint to get all treasure hunts for a user
     *
     * @param request
     * @param userId
     * @return Returns one of the following HTTP responses:
     * - 200 - Returns a list of treasure hunts
     * - 401 - Not authorised
     * - 403 - Forbidden
     * - 404 - User Not found
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTresureHuntsByUserId(Http.Request request, int userId) {
        return userRepository.getUserById(userId)
                .thenComposeAsync(optUser -> {
                    if (!optUser.isPresent()) {
                        throw new CompletionException(new NotFoundException("User not found"));
                    }
                    User userFromMiddleware = request.attrs().get(ActionState.USER);
                    if (!userFromMiddleware.isAdmin() && userId != userFromMiddleware.getUserId()) {
                        throw new CompletionException(new ForbiddenRequestException("Forbidden"));
                    }

                    return treasureHuntRepository.getTreasureHuntsByUserId(userId);
                })
                .thenApplyAsync(treasures -> {
                    JsonNode treasureJson = Json.toJson(treasures);
                    return ok(treasureJson);
                }, executionContext)
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (NotFoundException notFoundExc) {
                        ObjectNode message = Json.newObject();
                        message.put("message", notFoundExc.getMessage());
                        return notFound(message);
                    } catch (ForbiddenRequestException forbiddenException) {
                        ObjectNode message = Json.newObject();
                        message.put("message", forbiddenException.getMessage());
                        return forbidden(message);
                    } catch (Throwable throwable) {
                        return internalServerError();
                    }
                });
    }
}

