package controllers;

import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import io.ebean.Finder;
import models.Destination;
import models.TravellerType;
import models.TreasureHunt;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;
import play.mvc.Http.Request;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.Json;
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
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

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
     * @param userRepository the user repository.
     * @param executionContext the context to execute the async functions.
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
     * - 404 - When the treasure hunt does not exist.
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

                    //TODO: Forbidden exception here.

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
                    if (jsonBody.has("ownerId")) {
                        int ownerId = jsonBody.get("ownerId").asInt();
                        try {
                            treasureHunt.setOwnerId(ownerId);
                        } catch (NotFoundException e) {
                            throw new CompletionException(e);
                        }
                    }
                    if (jsonBody.has("destinationId")) {
                        int destinationId = jsonBody.get("destinationId").asInt();
                        //TODO: destination here
                    }
                    if (jsonBody.has("riddle")) {
                        String riddle = jsonBody.get("riddle").asText();
                        if (riddle.length() == 0) {
                            throw new CompletionException(new BadRequestException("Riddle is required."));
                        }
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
                                throw new CompletionException(new BadRequestException("End date cannot be before start date."));
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
                    //TODO: check permission here.
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
                        Date startDate = new Date(jsonBody.get("startDate").asLong());
                        Date endDate = new Date(jsonBody.get("startDate").asLong());
                        int destinationId = jsonBody.get("destinationId").asInt();
                        TreasureHunt treasureHunt = new TreasureHunt(treasureHuntName, destinationId, riddle, startDate,
                                endDate);

                        User user = optUser.get();
                        treasureHunt.setOwnerId(user.getUserId());

                        return created();
                    } catch (NotFoundException e) {
                        ObjectNode message = Json.newObject().put("message", "Destination not found");
                        return notFound(message);
                    } catch (Exception e) {
                        return badRequest();
                    }
                }, executionContext);
    }
}
