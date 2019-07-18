package controllers;

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
import play.libs.concurrent.HttpExecutionContext;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TreasureHuntRepository;
import repository.UserRepository;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Contains all end points associated with treasure hunts.
 */
public class TreasureHuntController extends Controller {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private TreasureHuntRepository treasureHuntRepository;
    private UserRepository userRepository;
    private HttpExecutionContext httpExecutionContext;

    /**
     * Dependency Injection.
     *
     * @param treasureHuntRepository
     */
    @Inject
    public TreasureHuntController(TreasureHuntRepository treasureHuntRepository, UserRepository userRepository, HttpExecutionContext httpExecutionContext) {
        this.treasureHuntRepository = treasureHuntRepository;
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
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
                }, httpExecutionContext.current());
    }
}
