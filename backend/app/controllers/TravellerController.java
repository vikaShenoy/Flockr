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
     * Adds a nationality to the user
     * @param travellerId the traveller ID
     * @param request Object to get the nationality to add.
     * @return
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
     * Delete a nationality for a logged in user given a nationality id in the request body
     * @param travellerId the traveller for which we want to delete the nationality
     * @param request the request passed by the routes file
     * @return a response with status code as specified in API spec
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

}
