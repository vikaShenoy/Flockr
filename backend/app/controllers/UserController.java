package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Passport;
import models.TravellerType;
import models.User;
import models.Nationality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.UserRepository;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.Date;
import java.text.SimpleDateFormat;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;

/**
 * Contains all endpoints associated with users.
 */
public class UserController extends Controller {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public UserController(UserRepository userRepository, HttpExecutionContext httpExecutionContext) {
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * Retrieves a user's details
     * @param userId the traveller Id of the traveller to retrieve
     * @param request request Object
     * @return HTTP response which can be
     *  - 200 - with user's details, if successful.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTraveller(int userId, Http.Request request) {

        return userRepository.getUserById(userId)
                .thenApplyAsync((user) -> {
                    if (!user.isPresent()) {
                        return notFound();
                    }

                    JsonNode userAsJson = Json.toJson(user);

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

    /**
     * Updates a user's details
      * @param userId Redundant ID
     * @param request Object to get the JSOn data
     * @return 200 status if update was successful, 500 otherwise
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTraveller(int userId, Http.Request request) {
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
                log.debug("Date stored in db for user is: " + date);
                user.setDateOfBirth(date);
            } catch (ParseException e) {
                log.error(e.getMessage());
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
            userRepository.updateUser(user);
            return ok(message);
        }, httpExecutionContext.current());
    }

    /**
     * A function that gets a list of all the passports and returns a 200 ok code to the HTTP client
     *
     * @param request Http.Request the HTTP request
     * @return a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getAllPassports(Http.Request request) {
        return userRepository.getAllPassports()
                .thenApplyAsync((passports) -> {
                    return ok(Json.toJson(passports));
                }, httpExecutionContext.current());
    }


    /**
     * Gets a list of all the nationalities and returns it with a 200 ok code to the HTTP client
     *
     * @param request <b>Http.Request</b> the http request.
     * @return The completion function to be called on completion.
     */
    public CompletionStage<Result> getNationalities(Http.Request request) {
        return userRepository.getAllNationalities()
                .thenApplyAsync((nationalities) -> {
                    return ok(Json.toJson(nationalities));
                }, httpExecutionContext.current());
    }


    /**
     * Get all users.
     * @return status code of 200 with all traveller details in json body.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellers() {
        return userRepository.getTravellers()
                .thenApplyAsync(travellers -> {
                    JsonNode travellersJson = Json.toJson(travellers);
                    return ok(travellersJson);
                }, httpExecutionContext.current());
    }


    /**
     * A function that adds a passport to a user based on the given user ID
     *
     * @param userId  the traveller ID
     * @param request Object to get the passportId to add
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addPassport(int userId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);


        int passportId = request.body().asJson().get("passportId").asInt();

        return userRepository.getPassportById(passportId)
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
     *
     * @param userId     the traveller ID
     * @param passportId the passport ID
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> removePassport(int userId, int passportId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        return userRepository.getPassportById(passportId)
                .thenApplyAsync((passport) -> {
                    if (!passport.isPresent()) {
                        return notFound();
                    }
                    List<Passport> passports = user.getPassports();
                    passports.remove(passport.get());
                    user.setPassports(passports);
                    user.save();
                    log.debug(user.getPassports().toString());
                    return ok();
                }, httpExecutionContext.current());
    }


    /**
     * A function that adds a nationality to the user based on the user ID given
     *
     * @param userId  the traveller ID
     * @param request Object to get the nationality to add.
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> addNationality(int userId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int nationalityId = request.body().asJson().get("nationalityId").asInt();

        return userRepository.getNationalityById(nationalityId)
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
     *
     * @param userId  the traveller for which we want to delete the nationality
     * @param request the request passed by the routes file
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteNationalityForUser(int userId, int nationalityId, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return userRepository.getNationalityById(nationalityId)
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
     *
     * @param request unused request object
     * @return ok with status 200 if types obtained, 401 if no token is provided
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellerTypes(Http.Request request) {
        return userRepository.getAllTravellerTypes()
                .thenApplyAsync((types) -> {
                    return ok(Json.toJson(types));
                }, httpExecutionContext.current());
    }


    /**
     * Allows the front-end to search for a traveller.
     *
     * @param request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> searchTravellers(Http.Request request) {
        int nationality;
        nationality = -1;
        int travellerType;
        travellerType = -1;
        long ageMin;
        ageMin = -1;
        long ageMax;
        ageMax = -1;
        String gender = "";
        try {
            String nationalityQuery = request.getQueryString("nationality");
            if (!nationalityQuery.isEmpty())
                nationality = Integer.parseInt(nationalityQuery);
        } catch (Exception e){ log.error("No Parameter nationality");}
        try {
            String ageMinQuery = request.getQueryString("ageMin");
            if (!ageMinQuery.isEmpty())
                ageMin = Long.parseLong(ageMinQuery);
        } catch (Exception e){ log.error("No Parameter ageMin");}
        try {
            String ageMaxQuery = request.getQueryString("ageMax");
            if (!ageMaxQuery.isEmpty())
                ageMax = Long.parseLong(ageMaxQuery);
        } catch (Exception e){ log.error("No Parameter ageMax");}
        try {
            String travellerTypeQuery = request.getQueryString("travellerType");
            if (!travellerTypeQuery.isEmpty())
                travellerType = Integer.parseInt(travellerTypeQuery);
        } catch (Exception e){ log.error("No Parameter travellerType");}
        try {
            gender = request.getQueryString("gender");
        } catch (Exception e){ log.error("No Parameter gender");}
        Date dateMin = new Date(ageMin);
        Date dateMax = new Date(ageMax);

        log.debug("nationality="+nationality + " agemin=" + ageMin +" agemax="+ ageMax + " gender=" + gender + " travellerType=" + travellerType);

        return userRepository.searchUser(nationality, gender, dateMin, dateMax, travellerType)  //Just for testing purposes
                .thenApplyAsync((user) -> {
                    JsonNode userAsJson = Json.toJson(user);
                    log.debug(userAsJson.asText());

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

}