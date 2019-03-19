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
import repository.TravellerRepository;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
            user.setLastName(jsonBody.get("firstName").asText());
        }

        if (jsonBody.has("dateOfBirth")) {
            Timestamp timestamp = new Timestamp(jsonBody.get("dateOfBirth").asInt());
            user.setDateOfBirth(timestamp);
        }

        if (jsonBody.has("genderId")) {
            Gender gender = new Gender(null);
            gender.setGenderId(jsonBody.get("genderId").asInt());
            user.setGender(gender);
        }

        return supplyAsync(() -> {
            travellerRepository.updateUser(user);
            return ok();
        }, httpExecutionContext.current());
    }

    /**
     * Adds a passport to a user
     * @param travellerId Redundant ID
     * @param request Object to get the passportId to add
     * @return 200 if request was successful, 500 otherwise
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
     * Deletes a passport from the user
     * @param travellerId the traveller ID
     * @param passportId the passport ID
     * @return 200 OK if the request was successful, 500 if not
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
     * Adds a nationality to the user
     * @param travellerId the traveller ID
     * @param request Object to get the nationality to add.
     * @return <b>CompletionStage&ltResult&gt</b> the method to be run on completion
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
                    System.out.println(user.getNationalities());
                    return ok();
                }, httpExecutionContext.current());
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
     * Gets a list of all the passports and returns it with a 200 ok code to the HTTP client
     * @param request <b>Http.Request</b> the http request
     * @return <b>CompletionStage&ltResult&gt</b> the completion function to be called on completion
     */
    public CompletionStage<Result> getAllPassports(Http.Request request) {
        return travellerRepository.getAllPassports()
                .thenApplyAsync((passports) -> {
                    return ok(Json.toJson(passports));
                }, httpExecutionContext.current());
    }
    
}
