package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import models.Gender;
import models.Passport;
import models.User;
import models.Nationality;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.DestinationRepository;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;


/**
 * Contains all endpoints associated with destinations
 */
public class DestinationController  extends Controller{
    private final DestinationRepository destinationRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public DestinationController(DestinationRepository destinationRepository, HttpExecutionContext httpExecutionContext) {
        this.destinationRepository = destinationRepository;
        this.httpExecutionContext = httpExecutionContext;
    }


    /**
     * A function that gets a list of all the destinations and returns it with a 200 ok code to the HTTP client
     * @param request Http.Request the http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getDestinations(Http.Request request) {
        return destinationRepository.getDestinations()
                .thenApplyAsync((destinations) -> {
                    return ok(Json.toJson(destinations));
                }, httpExecutionContext.current());
    }

    /**
     * A function that retrieves a destination details based on the destination ID given
     * @param destinationId the destination Id of the destination to retrieve
     * @param request request Object
     * @return destination details as a Json object
     */

    public CompletionStage<Result> getDestination(int destinationId, Http.Request request) {

        return destinationRepository.getDestinationById(destinationId)
                .thenApplyAsync((destination) -> {
                    if (!destination.isPresent()) {
                        return notFound();
                    }

                    JsonNode userAsJson = Json.toJson(destination);

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

}
