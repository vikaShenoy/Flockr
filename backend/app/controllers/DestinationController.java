package controllers;

import actions.LoggedIn;
import akka.actor.FSM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.NotFoundException;
import models.Country;
import models.Destination;
import models.DestinationType;
import models.District;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.DestinationRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionException;
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
        int destinationType = jsonRequest.get("destinationTypeId").asInt();
        int district = jsonRequest.get("districtId").asInt();
        Double latitude = jsonRequest.get("latitude").asDouble();
        Double longitude = jsonRequest.get("longitude").asDouble();
        int country = jsonRequest.get("countryId").asInt();

        DestinationType destinationTypeAdd = new DestinationType(null);
        destinationTypeAdd.setDestinationTypeId(destinationType);
        District districtAdd = new District(null, null);
        districtAdd.setDistrictId(district);
        Country countryAdd = new Country(null);
        countryAdd.setCountryId(country);

        Destination destination = new Destination(destinationName,destinationTypeAdd,districtAdd,
                                                  latitude,longitude,countryAdd);

        return destinationRepository.insert(destination)
                .thenApplyAsync((insertedDestination) -> ok(Json.toJson(insertedDestination)), httpExecutionContext.current());

    }

    /**
     * Endpoint to get update a destinations details
     * @param request Request body to get json body from
     * @param destinationId The destination ID to update
     * @return Returns the http response which can be
     *         - Ok - User was updated successfully
     *
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateDestination(Http.Request request, int destinationId) {
       return destinationRepository.getDestinationById(destinationId)
       .thenComposeAsync((optionalDest) -> {
        if (!optionalDest.isPresent()) {
            throw new CompletionException(new NotFoundException());
        }
        JsonNode jsonBody = request.body().asJson();

        String destinationName =  jsonBody.get("destinationName").asText();
        int destinationTypeId = jsonBody.get("destinationTypeId").asInt();
        int countryId = jsonBody.get("countryId").asInt();
        int districtId = jsonBody.get("districtId").asInt();
        double latitude = jsonBody.get("latitude").asDouble();
        double longitude = jsonBody.get("longitude").asDouble();

        Destination destination = optionalDest.get();


        DestinationType destType = new DestinationType(null);
        destType.setDestinationTypeId(destinationTypeId);

        Country country = new Country(null);
        country.setCountryId(countryId);

        District district = new District(null, null);
        district.setDistrictId(districtId);

        destination.setDestinationName(destinationName);
        destination.setDestinationType(destType);
        destination.setDestinationCountry(country);
        destination.setDestinationDistrict(district);
        destination.setDestinationLat(latitude);
        destination.setDestinationLon(longitude);

        return destinationRepository.update(destination);
        }, httpExecutionContext.current())
       .thenApplyAsync((Destination) -> (Result) ok(), httpExecutionContext.current())
       .exceptionally(e -> {
            try {
                throw e.getCause();
            } catch (NotFoundException notFoundE) {
                return notFound();
            } catch (Throwable ee) {
                return internalServerError();
            }
       });
    }

    /**
     * Endpoint to get all countries
     * @return The countries as json
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getCountries() {
        return destinationRepository.getCountries()
                .thenApplyAsync(countries -> {
                    JsonNode countriesJson = Json.toJson(countries);
                    return ok(countriesJson);
                }, httpExecutionContext.current())
                .exceptionally(e -> internalServerError());
    }

    /**
     * Endpoint to get destination types
     * @return The destination types as json
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getDestinationTypes() {
        return destinationRepository.getDestinationTypes()
                .thenApplyAsync(destinationTypes -> {
                    JsonNode destinationTypesJson = Json.toJson(destinationTypes);
                    return ok(destinationTypesJson);
                }, httpExecutionContext.current())
                .exceptionally(e -> internalServerError());
    }

    @With(LoggedIn.class)
    public CompletionStage<Result> getDistricts(int countryId) {

        return destinationRepository.getDistricts(countryId)
                .thenApplyAsync(districts -> {
                    JsonNode districtsJson = Json.toJson(districts);
                    return ok(districtsJson);
                }, httpExecutionContext.current());
    }

}

