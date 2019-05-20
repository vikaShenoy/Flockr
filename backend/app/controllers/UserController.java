package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.Ebean;
import models.*;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;
import util.Security;

/**
 * Contains all endpoints associated with users.
 */
public class UserController extends Controller {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private HttpExecutionContext httpExecutionContext;
    private final Security security;

    @Inject
    public UserController(UserRepository userRepository, HttpExecutionContext httpExecutionContext, Security security) {
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
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
                .thenApplyAsync((optUser) -> {
                    if (!optUser.isPresent()) {
                        ObjectNode message = Json.newObject().put("message", "User with id " + userId + " was not found");
                        return notFound(message);
                    }
                    User user = optUser.get();
                    JsonNode userAsJson = Json.toJson(user);
                    return ok(userAsJson);
                }, httpExecutionContext.current());
    }

    /**
     * Updates a travellers details
     * @param userId Redundant ID
     * @param request Object to get the JSOn data
     * @return 200 status if update was successful, 500 otherwise
     */
    @With({LoggedIn.class})
    public CompletionStage<Result> updateTraveller(int userId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();
        User userFromMiddleware = request.attrs().get(ActionState.USER);

        if (!security.userHasPermission(userFromMiddleware, userId)) {
            return supplyAsync(Controller::forbidden);
        }

        return userRepository.getUserById(userId)
                .thenApplyAsync((user) -> {
                    if (!user.isPresent()) {
                        return notFound();
                    }

                    if (jsonBody.has("firstName")) {
                        user.get().setFirstName(jsonBody.get("firstName").asText());
                    }

                    if (jsonBody.has("middleName")) {
                        user.get().setMiddleName(jsonBody.get("middleName").asText());
                    }

                    if (jsonBody.has("lastName")) {
                        user.get().setLastName(jsonBody.get("lastName").asText());
                    }

                    if (jsonBody.has("email")) {
                        user.get().setEmail(jsonBody.get("email").asText());
                    }

                    if (jsonBody.has("dateOfBirth")) {
                        try {
                            String incomingDate = jsonBody.get("dateOfBirth").asText();
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(incomingDate);
                            System.out.println("Date stored in db for user is: " + date);
                            user.get().setDateOfBirth(date);
                        } catch (ParseException e) {
                            System.out.println(Arrays.toString(e.getStackTrace()));
                        }
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    if (jsonBody.has("nationalities")) {
                        JsonNode arrNode = jsonBody.get("nationalities");
                        ArrayList<Nationality> nationalities = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Nationality nationality = new Nationality(null);
                            nationality.setNationalityId(id.asInt());
                            nationalities.add(nationality);

                        }
                        user.get().setNationalities(nationalities);
                    }

                    if (jsonBody.has("passports")) {
                        JsonNode arrNode = jsonBody.get("passports");
                        ArrayList<Passport> passports = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Passport passport = new Passport(null);
                            passport.setPassportId(id.asInt());
                            passports.add(passport);

                        }
                        user.get().setPassports(passports);
                    }

                    if (jsonBody.has("travellerTypes")) {
                        JsonNode arrNode = jsonBody.get("travellerTypes");
                        ArrayList<TravellerType> travellerTypes = new ArrayList<>();
                        for (JsonNode id : arrNode) {
                            TravellerType travellerType = new TravellerType(null);
                            travellerType.setTravellerTypeId(id.asInt());
                            travellerTypes.add(travellerType);
                        }
                        user.get().setTravellerTypes(travellerTypes);
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    ObjectNode message = Json.newObject();
                    message.put("message", "Successfully updated the traveller's information");

                    userRepository.updateUser(user.get());
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

    /**		    /**
     * Retrieve user roles from request body and update the specified user so they
     * have these roles.
     * @param travellerId user to have roles updated
     * @param request HTTP request
     * @return a status code of 200 is ok, 400 if user or role doesn't exist
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> updateTravellerRole(int travellerId, Http.Request request) {

        // Check travellerID isn't a super admin already
        // Check the patch doesn't give someone a super admin role
        JsonNode jsonBody = request.body().asJson();
        JsonNode roleArray = jsonBody.withArray("roleTypes");
        List<String> roleTypes = new ArrayList<>();


        for (JsonNode roleJson : roleArray) {
            String roleTypeString = roleJson.asText();
            if (!RoleType.contains(roleTypeString)) {
                return supplyAsync(() -> badRequest());
            }
            roleTypes.add(roleTypeString);
        }

        return userRepository.getUserById(travellerId)
                .thenApplyAsync(optionalUser -> {
                    if (!optionalUser.isPresent()) {
                        return notFound();
                    }
                    List<Role> userRoles = userRepository.getRolesByRoleType(roleTypes);
                    User user = optionalUser.get();

                    // Prevent the default admin from having their permission removed
                    if (user.isDefaultAdmin()) {
                        boolean flag = false;
                        for (String roleString : roleTypes) {
                            if (roleString.equals(RoleType.SUPER_ADMIN.name())) {
                                flag = true;
                            }
                        }
                        if (!flag) { return forbidden(); }
                    } else {
                        // Prevents a non default admin from getting super-admin permission
                        for (String roleString : roleTypes) {
                            if (roleString.equals(RoleType.SUPER_ADMIN.name())) {
                                return forbidden();
                            }
                        }
                    }
                    user.setRoles(userRoles);
                    user.update();
                    return ok("Success");
                });
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

    /**		    /**
     * Delete a user given its id
     * @param userId the id of the user to be deleted
     * @param request the request passed by the controller
     * @return
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteUser(int userId, Http.Request request) {
        User userDoingDeletion = request.attrs().get(ActionState.USER);
        final ObjectNode message = Json.newObject();; // used in the response

        return userRepository.getUserById(userId)
                .thenApplyAsync((optionalUserBeingDeleted) -> {
                    if (!optionalUserBeingDeleted.isPresent()) {
                        // check that the user being deleted actually exists
                        message.put("message", "Did not find user with id " + userId);
                        return notFound(message);
                    }

                    User userBeingDeleted = optionalUserBeingDeleted.get();
                    if (userBeingDeleted.isDefaultAdmin()) {
                        // if user is the default admin, leave it alone
                        message.put("message", "No one can delete the default admin");
                        return unauthorized(message);
                    } else if (userBeingDeleted.isAdmin()) {
                        if (userDoingDeletion.isAdmin() || userDoingDeletion.isDefaultAdmin()) {
                            // only admins and the default admin can delete admins
                            message.put("message", "Deleted user with id: " + userBeingDeleted.getUserId());
                            CompletionStage<Result> completionStage = userRepository.deleteUserById(userId).thenApply((ignored) -> ok(message));
                            CompletableFuture<Result> completableFuture = completionStage.toCompletableFuture();
                            try {
                                return completableFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                System.err.println(String.format("Async execution interrupted when user %s was deleting user %s", userDoingDeletion, userBeingDeleted));
                                message.put("message", "Something went wrong deleting that user, try again");
                                return internalServerError(message);
                            }
                        } else {
                            message.put("message", "Only admins or the default admin can delete other admins");
                            return unauthorized(message);
                        }
                    } else {
                        if (userDoingDeletion.equals(userBeingDeleted) && !userBeingDeleted.isDefaultAdmin()) {
                            // a user can delete itself
                            message.put("message", "Deleted user with id: " + userBeingDeleted.getUserId());
                            CompletionStage<Result> completionStage = userRepository.deleteUserById(userId).thenApply((ignored) -> ok(message));
                            CompletableFuture<Result> completableFuture = completionStage.toCompletableFuture();
                            try {
                                return completableFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                System.err.println(String.format("Async execution interrupted when user %s was deleting user %s", userDoingDeletion, userBeingDeleted));
                                message.put("message", "Something went wrong deleting that user, try again");
                                return internalServerError(message);
                            }
                        } else if ((userDoingDeletion.isAdmin() && !userBeingDeleted.isDefaultAdmin()) || userDoingDeletion.isDefaultAdmin()) {
                            message.put("message", "Deleted user with id: " + userBeingDeleted.getUserId());
                            CompletionStage<Result> completionStage = userRepository.deleteUserById(userId).thenApply((ignored) -> ok(message));
                            CompletableFuture<Result> completableFuture = completionStage.toCompletableFuture();
                            try {
                                return completableFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                System.err.println(String.format("Async execution interrupted when user %s was deleting user %s", userDoingDeletion, userBeingDeleted));
                                message.put("message", "Something went wrong deleting that user, try again");
                                return internalServerError(message);
                            }
                        } else {
                            message.put("message", "Regular users can not delete other regular users");
                            return unauthorized(message);
                        }
                    }
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
     * @param request the http request
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

        // TODO: do not catch generic Exceptions, if this is a query method, not specifying all query parameters should not be logged as an error, it can be an info level log e.g. "no parameter nationality, omitting from search"

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
