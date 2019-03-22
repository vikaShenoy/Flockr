package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Country;
import models.Destination;
import models.DestinationType;
import models.District;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repository.DestinationRepository;

import javax.inject.Inject;
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

                    JsonNode destAsJson = Json.toJson(destination);

                    return ok(destAsJson);

                }, httpExecutionContext.current());

    }


    /**
     * Function to add destinations to the database
     * @param request the HTTP post request.
     * @return a completion stage with the new json object or a bad request error/
     */

    public CompletionStage<Result> addDestination(Http.Request request) {
        JsonNode jsonRequest = request.body().asJson();

        //Use the request Checker from the  AuthController to check the JSON is not empty
        if (AuthController.checkRequest(jsonRequest)) return supplyAsync(() -> {
            ObjectNode message = Json.newObject();
            message.put("message", "Please provide a valid request body according to the API spec");
            return badRequest(message);
        });
        String destinationName = jsonRequest.get("destinationName").asText();
        int destinationType = jsonRequest.get("destinationType").asInt();
        int district = jsonRequest.get("district").asInt();
        Double latitude = jsonRequest.get("latitude").asDouble();
        Double longitude = jsonRequest.get("longitude").asDouble();
        int country = jsonRequest.get("country").asInt();

        DestinationType destinationTypeAdd = new DestinationType(null);
        destinationTypeAdd.setDestinationTypeId(destinationType);
        District districtAdd = new District(null);
        districtAdd.setDistrictId(district);
        Country countryAdd = new Country(null);
        countryAdd.setCountryId(country);

        Destination destination = new Destination(destinationName,destinationTypeAdd,districtAdd,
                                                  latitude,longitude,countryAdd);

        return destinationRepository.insert(destination)
                .thenApplyAsync((insertedDestination) -> ok(Json.toJson(insertedDestination)), httpExecutionContext.current());

    }



}

