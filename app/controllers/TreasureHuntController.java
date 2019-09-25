package controllers;

import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import actions.ActionState;
import exceptions.UnauthorizedException;
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
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.Date;
import java.util.concurrent.CompletionStage;
import util.ExceptionUtil;

import static util.TreasureHuntUtil.validateTreasureHunts;

/** Controller to handle all end points associated with treasure hunts. */
public class TreasureHuntController extends Controller {

  private static final String TREASURE_HUNT_NAME_KEY = "treasureHuntName";
  private static final String START_DATE_KEY = "startDate";
  private static final String MESSAGE_KEY = "message";
  private static final String RIDDLE_KEY = "riddle";
  private static final String END_DATE_KEY = "endDate";
  private static final String TREASURE_HUNT_DESTINATION_ID_KEY = "treasureHuntDestinationId";
  private static final String START_TIME_KEY = "startTime";
  private static final String END_TIME_KEY = "endTime";


  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final TreasureHuntRepository treasureHuntRepository;
  private final ExceptionUtil exceptionUtil;
  private final DatabaseExecutionContext executionContext;
  private final UserRepository userRepository;

  /**
   * Dependency Injection.
   *
   * @param treasureHuntRepository the treasure hunt repository.
   * @param userRepository the user repository.
   * @param executionContext the context to execute the async functions.
   */
  @Inject
  public TreasureHuntController(
      TreasureHuntRepository treasureHuntRepository,
      UserRepository userRepository,
      DatabaseExecutionContext executionContext,
      ExceptionUtil exceptionUtil) {
    this.treasureHuntRepository = treasureHuntRepository;
    this.userRepository = userRepository;
    this.executionContext = executionContext;
    this.exceptionUtil = exceptionUtil;
  }

  /**
   * Method for the PUT /treasurehunts/:treasureHuntId endpoint. Edits a treasure hunt if the user
   * has permission and returns one of the following responses via HTTP: - 200 - When treasure hunt
   * is successfully modified. - 400 - When the data given in the request is invalid. - 401 - When
   * the user is not authorized. - 403 - When the user does not have permission. - 404 - When the
   * treasure hunt or user does not exist.
   *
   * @param request the http request.
   * @param treasureHuntId the id of the treasure hunt.
   * @return the completion stage containing the result.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> editTreasureHunt(Request request, int treasureHuntId) {
    return treasureHuntRepository
        .getTreasureHuntById(treasureHuntId)
        .thenComposeAsync(
            optionalTreasureHunt -> {
              if (!optionalTreasureHunt.isPresent()) {
                throw new CompletionException(new NotFoundException("Treasure Hunt not found."));
              }
              TreasureHunt treasureHunt = optionalTreasureHunt.get();

              User userFromMiddleWare = request.attrs().get(ActionState.USER);
              if (!userFromMiddleWare.isAdmin()
                  && userFromMiddleWare.getUserId() != treasureHunt.getOwnerId()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have permission to perform this action."));
              }

              JsonNode jsonBody = request.body().asJson();
              if (jsonBody.has(TREASURE_HUNT_NAME_KEY)) {
                String treasureHuntName = jsonBody.get(TREASURE_HUNT_NAME_KEY).asText();
                if (treasureHuntName.length() == 0) {
                  throw new CompletionException(
                      new BadRequestException("Treasure hunt name is required."));
                } else if (treasureHuntName.length() > 255) {
                  throw new CompletionException(
                      new BadRequestException(
                          "Treasure hunt name must be less than 255 characters."));
                }
                treasureHunt.setTreasureHuntName(treasureHuntName);
              }
              if (jsonBody.has(TREASURE_HUNT_DESTINATION_ID_KEY)) {
                int destinationId = jsonBody.get(TREASURE_HUNT_DESTINATION_ID_KEY).asInt();
                try {
                  treasureHunt.setTreasureHuntDestinationId(destinationId);
                } catch (NotFoundException e) {
                  throw new CompletionException(e);
                }
              }
              if (jsonBody.has(RIDDLE_KEY)) {
                String riddle = jsonBody.get(RIDDLE_KEY).asText();
                if (riddle.length() == 0) {
                  throw new CompletionException(new BadRequestException("Riddle is required."));
                }
                treasureHunt.setRiddle(riddle);
              }
              if (jsonBody.has(END_DATE_KEY)) {
                String endDateString = jsonBody.get(END_DATE_KEY).asText();
                try {
                  Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDateString);
                  if (!jsonBody.has(START_DATE_KEY) && endDate.before(treasureHunt.getStartDate())) {
                    throw new CompletionException(new BadRequestException("End date cannot be before start date."));
                  }
                  treasureHunt.setEndDate(endDate);
                } catch (ParseException e) {
                  throw new CompletionException(
                      new BadRequestException("End date must be of format yyyy-mm-dd HH:mm:ss"));
                }
              }
              if (jsonBody.has(START_DATE_KEY)) {
                String startDateString = jsonBody.get(START_DATE_KEY).asText();
                try {
                  Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
                  if (startDate.after(treasureHunt.getEndDate())) {
                    throw new CompletionException(
                        new BadRequestException("Start date cannot be after end date."));
                  }
                  treasureHunt.setStartDate(startDate);
                } catch (ParseException e) {
                  throw new CompletionException(
                      new BadRequestException("Start date must be of format yyyy-mm-dd"));
                }
              }
              return treasureHuntRepository.modifyTreasureHunt(treasureHunt);
            },
            executionContext)
        .thenApplyAsync(result -> ok(Json.toJson(result)), executionContext)
        .exceptionally(exceptionUtil::getResultFromError);
  }


  /**
   * Method for the DELETE /treasurehunts/:treasureHuntId endpoint. Edits a treasure hunt if the
   * user has permission and returns one of the following responses via HTTP: - 200 - When treasure
   * hunt is successfully deleted. - 401 - When the user is not authorized. - 403 - When the user
   * does not have permission. - 404 - When the treasure hunt does not exist.
   *
   * @param request the http request.
   * @param treasureHuntId the id of the treasure hunt.
   * @return the completion stage containing the result.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> deleteTreasureHunt(Request request, int treasureHuntId) {
    return treasureHuntRepository
        .getTreasureHuntById(treasureHuntId)
        .thenComposeAsync(
            optionalTreasureHunt -> {
              if (!optionalTreasureHunt.isPresent()) {
                throw new CompletionException(new NotFoundException("Treasure hunt not found"));
              }
              TreasureHunt treasureHunt = optionalTreasureHunt.get();

              User userFromMiddleWare = request.attrs().get(ActionState.USER);
              if (!userFromMiddleWare.isAdmin()
                  && userFromMiddleWare.getUserId() != treasureHunt.getOwnerId()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have permission to perform this action."));
              }

              return treasureHuntRepository.removeTreasureHunt(treasureHunt);
            })
        .thenApplyAsync(
            deleted -> {
              if (deleted) return ok(Json.newObject()); // Strange error if json is removed...
              else return internalServerError();
            })
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Endpoint to create a new treasure hunt for a destination which a user owns.
   *
   * @param request HTTP post request containing treasure hunt parameters
   * @param userId user who creates (owns) the treasure hunt
   * @return - 201 for successful creation - 400 for bad request (parameters not provided etc.) -
   *     401 if unauthorised - 404 if the user or destination can't be found.
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> addTreasureHunt(Http.Request request, int userId) {
    return userRepository
        .getUserById(userId)
        .thenApplyAsync(
            optionalUser -> {
              if (!optionalUser.isPresent()) {
                throw new CompletionException(new NotFoundException(
                    String.format("User with id %d was not found", userId)));
              }
              try {
                JsonNode jsonBody = request.body().asJson();
                String treasureHuntName = jsonBody.get(TREASURE_HUNT_NAME_KEY).asText();
                String riddle = jsonBody.get(RIDDLE_KEY).asText();
                String startDateString = jsonBody.get(START_DATE_KEY).asText();

                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
                String endDateString = jsonBody.get(END_DATE_KEY).asText();
                Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDateString);
                int destinationId = jsonBody.get(TREASURE_HUNT_DESTINATION_ID_KEY).asInt();

                TreasureHunt treasureHunt =
                    new TreasureHunt(
                        treasureHuntName, userId, destinationId, riddle, startDate, endDate);
                User user = optionalUser.get();
                treasureHunt.setOwnerId(user.getUserId());
                treasureHunt.save();
                int treasureHuntId = treasureHunt.getTreasureHuntId();
                return created(Json.newObject().put("treasureHuntId", treasureHuntId));
              } catch (NullPointerException e) {
                throw new CompletionException(new BadRequestException("Insufficient data provided."));
              } catch (Exception e) {
                throw new CompletionException(e);
              }
            }, executionContext)
        .exceptionally(exceptionUtil::getResultFromError);
  }


  /**
   * Endpoint to get all treasure hunts for a user.
   *
   * @param request HTTP request object.
   * @param userId id of the user to get treasure hunts for.
   * @return Returns one of the following HTTP responses: - 200 - Returns a list of treasure hunts -
   *     401 - Not authorised - 403 - Forbidden - 404 - User Not found
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getTreasureHuntsByUserId(Http.Request request, int userId) {
    return userRepository
        .getUserById(userId)
        .thenComposeAsync(
            optUser -> {
              if (!optUser.isPresent()) {
                throw new CompletionException(new NotFoundException("User not found"));
              }
              User userFromMiddleware = request.attrs().get(ActionState.USER);
              if (!userFromMiddleware.isAdmin() && userId != userFromMiddleware.getUserId()) {
                throw new CompletionException(new ForbiddenRequestException("Forbidden"));
              }

              return treasureHuntRepository.getTreasureHuntsByUserId(userId);
            })
        .thenApplyAsync(
            treasures -> {
              JsonNode treasureJson = Json.toJson(treasures);
              return ok(treasureJson);
            },
            executionContext)
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Endpoint to retrieve all treasure hunts in the system.
   *
   * @param request Http request object
   * @return - 200 - success, body contains all treasure hunts - 401 - unauthorized - 500 - internal
   *     server error
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> getAllTreasureHunts(Http.Request request) {
    return treasureHuntRepository
        .getTreasureHunts()
        .thenApplyAsync(
            treasureHunts -> {
              List<TreasureHunt> validTreasures = validateTreasureHunts(treasureHunts);
              JsonNode treasureJson = Json.toJson(validTreasures);
              return ok(treasureJson);
            },
            executionContext)
        .exceptionally(exceptionUtil::getResultFromError);
  }

  /**
   * Undo the deletion of a treasure hunt. The following are the status codes: - 200 - OK -
   * successful undo. - 400 - Bad Request - The treasure hunt has not been deleted. - 401 -
   * Unauthorised - the user is not authorised. - 403 - Forbidden - The user does not have
   * permission to undo this deletion. - 404 - Not Found - The treasure hunt cannot be found.
   *
   * @param treasureHuntId the Id of the treasure hunt to undo deletion for
   * @param request the http request
   * @return the completion stage containing the result
   */
  @With(LoggedIn.class)
  public CompletionStage<Result> undoDeleteTreasureHunt(int treasureHuntId, Http.Request request) {
    User userFromMiddleware = request.attrs().get(ActionState.USER);
    return treasureHuntRepository
        .getTreasureHuntByIdWithSoftDelete(treasureHuntId)
        .thenComposeAsync(
            optionalTreasureHunt -> {
              if (!optionalTreasureHunt.isPresent()) {
                throw new CompletionException(
                    new NotFoundException("The treasure hunt you are undoing does not exist"));
              }
              TreasureHunt treasureHunt = optionalTreasureHunt.get();

              if (!userFromMiddleware.isAdmin()
                  && treasureHunt.getOwnerId() != userFromMiddleware.getUserId()) {
                throw new CompletionException(
                    new ForbiddenRequestException(
                        "You do not have the permission to undo the deletion."));
              }

              if (!treasureHunt.isDeleted()) {
                throw new CompletionException(
                    new BadRequestException("This treasure hunt has not been deleted"));
              }
              return treasureHuntRepository.undoTreasureHuntDelete(treasureHunt);
            })
        .thenApplyAsync(treasureHunt -> ok(Json.toJson(treasureHunt)))
        .exceptionally(exceptionUtil::getResultFromError);
  }
}
