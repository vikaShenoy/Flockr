package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
import models.Passport;
import models.TravellerType;
import models.User;
import models.Nationality;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TravellerRepository;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.Date;
import java.text.SimpleDateFormat;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;

/**
 * Contains all endpoints associated with travellers
 */
public class TravellerController extends Controller {
    private final TravellerRepository travellerRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public TravellerController(TravellerRepository travellerRepository, HttpExecutionContext httpExecutionContext) {
        this.travellerRepository = travellerRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Retrieves a travellers details
     * @param travellerId the traveller Id of the traveller to retrieve
     * @param request request Object
     * @return traveller details as a Json object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTraveller(int travellerId, Http.Request request) {

        return travellerRepository.getUserById(travellerId)
                .thenApplyAsync((user) -> {
                    if (!user.isPresent()) {
                        return notFound();
                    }

                    JsonNode userAsJson = Json.toJson(user);

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

    /**
     * Updates a travellers details
      * @param travellerId Redundant ID
     * @param request Object to get the JSOn data
     * @return 200 status if update was successful, 500 otherwise
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTraveller(int travellerId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();


        User user = request.attrs().get(ActionState.USER);

        if (jsonBody.has("firstName")) {
            user.setFirstName(jsonBody.get("firstName").asText());
        }

        if (jsonBody.has("middleName")) {
            user.setMiddleName(jsonBody.get("middleName").asText());
        }

        if (jsonBody.has("lastName")) {
            user.setLastName(jsonBody.get("lastName").asText());
        }

        if (jsonBody.has("dateOfBirth")) {
            try {
                String incomingDate = jsonBody.get("dateOfBirth").asText();
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(incomingDate);
                System.out.println("Date stored in db for user is: " + date);
                user.setDateOfBirth(date);
            } catch (ParseException e) {
                System.out.println(e);
            }
        }

        if (jsonBody.has("gender")) {
            user.setGender(jsonBody.get("gender").asText());
        }

        if (jsonBody.has("nationalities")) {
            JsonNode arrNode = jsonBody.get("nationalities");
            ArrayList<Nationality> nationalities = new ArrayList<>();
            for (JsonNode id : arrNode) {

                Nationality nationality = new Nationality(null);
                nationality.setNationalityId(id.asInt());
                nationalities.add(nationality);

            }
            user.setNationalities(nationalities);
        }

        if (jsonBody.has("passports")) {
            JsonNode arrNode = jsonBody.get("passports");
            ArrayList<Passport> passports = new ArrayList<>();
            for (JsonNode id : arrNode) {

                Passport passport = new Passport(null);
                passport.setPassportId(id.asInt());
                passports.add(passport);

            }
            user.setPassports(passports);
        }

       if (jsonBody.has("travellerTypes")) {
            JsonNode arrNode = jsonBody.get("travellerTypes");
            ArrayList<TravellerType> travellerTypes = new ArrayList<>();
            for (JsonNode id : arrNode) {
                TravellerType travellerType = new TravellerType(null);
                travellerType.setTravellerTypeId(id.asInt());
                travellerTypes.add(travellerType);
            }
            user.setTravellerTypes(travellerTypes);
        }

        if (jsonBody.has("gender")) {
            user.setGender(jsonBody.get("gender").asText());
        }

        ObjectNode message = Json.newObject();
        message.put("message", "Successfully updated the traveller's information");

        return supplyAsync(() -> {
            travellerRepository.updateUser(user);
            return ok(message);
        }, httpExecutionContext.current());
    }

    /**
     * A function that gets a list of all the passports and returns a 200 ok code to the HTTP client
     * @param request Http.Request the HTTP request
     * @return a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getAllPassports(Http.Request request) {
        return travellerRepository.getAllPassports()
                .thenApplyAsync((passports) -> {
                    return ok(Json.toJson(passports));
                }, httpExecutionContext.current());
    }


    /**
     * Gets a list of all the nationalities and returns it with a 200 ok code to the HTTP client
     * @param request <b>Http.Request</b> the http request
     * @return <b>CompletionStage&ltResult&gt</b> the completion function to be called on completion
     */
    public CompletionStage<Result> getNationalities(Http.Request request) {
        return travellerRepository.getAllNationalities()
                .thenApplyAsync((nationalities) -> {
                    return ok(Json.toJson(nationalities));
                }, httpExecutionContext.current());
    }


    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellers() {
        return travellerRepository.getTravellers()
                .thenApplyAsync(travellers -> {
                    JsonNode travellersJson = Json.toJson(travellers);
                    return ok(travellersJson);
                }, httpExecutionContext.current());
    }


    /**
     * A function that adds a passport to a user based on the given user ID
     * @param travellerId the traveller ID
     * @param request Object to get the passportId to add
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addPassport(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);


        int passportId = request.body().asJson().get("passportId").asInt();

        return travellerRepository.getPassportById(passportId)
                .thenApplyAsync((passport) -> {
                    if (!passport.isPresent()) {
                        return notFound();
                    }
                    List<Passport> passports = user.getPassports();
                    passports.add(passport.get());
                    user.setPassports(passports);
                    user.save();
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * A function that deletes a passport from a user based on the given user ID
     * @param travellerId the traveller ID
     * @param passportId the passport ID
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> removePassport(int travellerId, int passportId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        return travellerRepository.getPassportById(passportId)
                .thenApplyAsync((passport) -> {
                    if (!passport.isPresent()) {
                        return notFound();
                    }
                    List<Passport> passports = user.getPassports();
                    passports.remove(passport.get());
                    user.setPassports(passports);
                    user.save();
                    System.out.println(user.getPassports());
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * A function that adds a nationality to the user based on the user ID given
     * @param travellerId the traveller ID
     * @param request Object to get the nationality to add.
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addNationality(int travellerId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int nationalityId = request.body().asJson().get("nationalityId").asInt();

        return travellerRepository.getNationalityById(nationalityId)
                .thenApplyAsync((nationality) -> {
                    if (!nationality.isPresent()) {
                        return notFound();
                    }
                    List<Nationality> nationalities = user.getNationalities();
                    nationalities.add(nationality.get());
                    user.setNationalities(nationalities);
                    user.save();
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * Deletes a nationality for a logged in user given a nationality id in the request body
     * @param travellerId the traveller for which we want to delete the nationality
     * @param request the request passed by the routes file
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteNationalityForUser(int travellerId, int nationalityId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return travellerRepository.getNationalityById(nationalityId)
                .thenApplyAsync((optionalNationality) -> {
                    if (!optionalNationality.isPresent()) {
                        return notFound("Could not find nationality " + nationalityId);
                    }
                    // now that we know that the nationality definitely exists
                    // extract the Nationality from the Optional<Nationality> object
                    Nationality nationality = optionalNationality.get();
                    List<Nationality> userNationalities = user.getNationalities();

                    // return not found if the user did not have that nationality already
                    if (!userNationalities.contains(nationality)) {
                        return notFound("User does not have nationality " + nationalityId);
                    }

                    userNationalities.remove(nationality);
                    user.setNationalities(userNationalities);
                    user.save();
                    return ok("Successfully deleted nationality");
                }, httpExecutionContext.current());
    }




    /**
     * Get a list of all valid traveller types
     * @param request unused request object
     * @return ok with status 200 if types obtained, 401 if no token is provided
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellerTypes(Http.Request request) {
        return travellerRepository.getAllTravellerTypes()
                .thenApplyAsync((types) -> {
                    return ok(Json.toJson(types));
                }, httpExecutionContext.current());
    }
}
