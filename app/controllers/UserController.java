package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.PhotoRepository;
import repository.UserRepository;
import util.Security;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Contains all endpoints associated with users.
 */
public class UserController extends Controller {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private HttpExecutionContext httpExecutionContext;
    private final Security security;
    private final PhotoRepository photoRepository;

    @Inject
    public UserController(UserRepository userRepository, HttpExecutionContext httpExecutionContext, Security security, PhotoRepository photoRepository) {
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
        this.photoRepository = photoRepository;
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
                .thenApplyAsync(user -> {
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
                            log.info(String.format("Date stored in db for user is: %s", date));
                            user.get().setDateOfBirth(date);
                        } catch (ParseException e) {
                            log.error("Error: ", e);
                        }
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    if (jsonBody.has("nationalities")) {
                        JsonNode arrNode = jsonBody.get("nationalities");
                        ArrayList<Nationality> nationalities = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Nationality nationality = Nationality.find.byId(id.asInt());
                            nationalities.add(nationality);
                        }
                        user.get().setNationalities(nationalities);
                    }

                    if (jsonBody.has("passports")) {
                        JsonNode arrNode = jsonBody.get("passports");
                        ArrayList<Passport> passports = new ArrayList<>();
                        for (JsonNode id : arrNode) {

                            Passport passport = Passport.find.byId(id.asInt());
                            passports.add(passport);

                        }
                        user.get().setPassports(passports);
                    }

                    if (jsonBody.has("travellerTypes")) {
                        JsonNode arrNode = jsonBody.get("travellerTypes");
                        ArrayList<TravellerType> travellerTypes = new ArrayList<>();
                        for (JsonNode id : arrNode) {
                            TravellerType travellerType = TravellerType.find.byId(id.asInt());
                            travellerTypes.add(travellerType);
                        }
                        user.get().setTravellerTypes(travellerTypes);
                    }

                    if (jsonBody.has("gender")) {
                        user.get().setGender(jsonBody.get("gender").asText());
                    }

                    ObjectNode message = Json.newObject();
                    return userRepository.updateUser(user.get());
                }, httpExecutionContext.current())
                .thenApplyAsync(updatedUser -> ok());
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
     * Get all users, including those who haven't filled in their complete profile.
     * @return status code of 200 with all traveller details in json body.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getAllTravellers() {
        return userRepository.getAllTravellers()
                .thenApplyAsync(travellers -> {
                    JsonNode travellersJson = Json.toJson(travellers);
                    return ok(travellersJson);
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
     * Retrieve user roles from request body and update the specified user so they
     * have these roles.
     * @param travellerId user to have roles updated
     * @param request HTTP request
     * @return a status code of 200 is ok, 400 if user or role doesn't exist
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> updateTravellerRole(int travellerId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();
        JsonNode roleArray = jsonBody.withArray("roles");
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
                    user.save();
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

    /**
     * Delete a user given its id
     * @param userId the id of the user to be deleted
     * @param request the request passed by the controller
     * @return HTTP result with codes:
     * - 500 - internal server errors
     * - 401 - user is not authorised
     * - 404 - user to be deleted can't be found
     * - 200 - successful delete
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> deleteUser(int userId, Http.Request request) {
        //TODO:: Issue #42 when deleting a user the profile photo file is never deleted.
        User userDoingDeletion = request.attrs().get(ActionState.USER);
        final ObjectNode message = Json.newObject();// used in the response

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
                            message.put(
                                    "message", "Deleted user with id: " + userBeingDeleted.getUserId());
                            CompletionStage<Result> completionStage = userRepository.deleteUserById(
                                    userId).thenApply((ignored) -> ok(message));
                            CompletableFuture<Result> completableFuture = completionStage.toCompletableFuture();
                            try {
                                return completableFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                System.err.println(String.format(
                                        "Async execution interrupted when user %s was deleting user %s",
                                        userDoingDeletion, userBeingDeleted));
                                message.put(
                                        "message", "Something went wrong deleting that user, try again");
                                return internalServerError(message);
                            }
                        } else {
                            message.put(
                                    "message", "Only admins or the default admin can delete other admins");
                            return unauthorized(message);
                        }
                    } else {
                        if (userDoingDeletion.equals(userBeingDeleted) && !userBeingDeleted.isDefaultAdmin()) {
                            // a user can delete itself
                            message.put("message", "Deleted user with id: " + userBeingDeleted.getUserId());
                            CompletionStage<Result> completionStage = userRepository.deleteUserById(
                                    userId).thenApply((ignored) -> ok(message));
                            CompletableFuture<Result> completableFuture = completionStage.toCompletableFuture();
                            try {
                                return completableFuture.get();
                            } catch (InterruptedException | ExecutionException e) {
                                System.err.println(String.format(
                                        "Async execution interrupted when user %s was deleting user %s",
                                        userDoingDeletion, userBeingDeleted));
                                message.put(
                                        "message", "Something went wrong deleting that user, try again");
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
     * Undoes a user deletion.
     * Response codes used.
     * - 200 - OK - Successful operation.
     * - 400 - Bad Request - When the user to undo has not been deleted.
     * - 401 - Unauthorised - User requesting is not authenticated.
     * - 403 - Forbidden - User requesting does not have permission.
     * - 404 - Not Found - Deleted user does not exist.
     *
     * @param userId the id of the deleted user to be undone.
     * @param request the http request.
     * @return the async method to make the changes.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> undoDeleteUser(int userId, Http.Request request) {
        User userFromMiddleWare = request.attrs().get(ActionState.USER);
        return userRepository.getUserByIdIncludingDeleted(userId)
                .thenComposeAsync(optionalDeletedUser -> {
                    if (!optionalDeletedUser.isPresent()) {
                        throw new CompletionException(new NotFoundException("This user does not exist."));
                    }
                    User deletedUser = optionalDeletedUser.get();
                    if (!userFromMiddleWare.isAdmin() && deletedUser.getUserId() != userFromMiddleWare.getUserId()) {
                        throw new CompletionException(new ForbiddenRequestException(
                                "You do not have permission to undo this user deletion."));
                    }
                    if (!deletedUser.isDeleted()) {
                        throw new CompletionException(new BadRequestException("This user has not been deleted."));
                    }
                    if (deletedUser.getProfilePhoto() != null) {
                        photoRepository.undoPhotoDelete(deletedUser.getProfilePhoto());
                    }
                    return userRepository.undoDeleteUser(deletedUser);
                })
                .thenApplyAsync(user -> ok(Json.toJson(user)))
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (BadRequestException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", e.getMessage());
                        return badRequest(message);
                    } catch (ForbiddenRequestException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", e.getMessage());
                        return forbidden(message);
                    } catch (NotFoundException error) {
                        ObjectNode message = Json.newObject();
                        message.put("message", e.getMessage());
                        return notFound(message);
                    } catch (Throwable throwable) {
                        log.error("Error while undoing user deletion", throwable);
                        return internalServerError();
                    }
                });
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
    public CompletionStage<Result> getTravellerTypes(Http.Request request) {
        return userRepository.getAllTravellerTypes()
                .thenApplyAsync((types) -> ok(Json.toJson(types)), httpExecutionContext.current());
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
        String name = null;
        int offset = 0;
        int limit = 20;

        try {
            String nationalityQuery = request.getQueryString("nationality");
            if (!nationalityQuery.isEmpty())
                nationality = Integer.parseInt(nationalityQuery);
        } catch (NullPointerException e){
            log.info("No Parameter nationality, excluding from search");
        }
        try {
            String ageMinQuery = request.getQueryString("ageMin");
            if (!ageMinQuery.isEmpty())
                ageMin = Long.parseLong(ageMinQuery);
        } catch (NullPointerException e){ log.info("No Parameter ageMin, excluding from search");}
        try {
            String ageMaxQuery = request.getQueryString("ageMax");
            if (!ageMaxQuery.isEmpty())
                ageMax = Long.parseLong(ageMaxQuery);
        } catch (NullPointerException e){ log.info("No Parameter ageMax, excluding from search");}
        try {
            String travellerTypeQuery = request.getQueryString("travellerType");
            if (!travellerTypeQuery.isEmpty())
                travellerType = Integer.parseInt(travellerTypeQuery);
        } catch (NullPointerException e){ log.error("No Parameter travellerType, excluding from search");}
        try {
            gender = request.getQueryString("gender");
        } catch (Exception e){ log.error("No Parameter gender");}

        try {
          name = request.getQueryString("name");
        } catch (NullPointerException e) {
          log.info("No parameter name, excluding from search");
        }

        try {
           String offsetQuery = request.getQueryString("offset");
           offset = Integer.parseInt(offsetQuery);
        } catch (NumberFormatException e) {
          log.info("No parameter offset");
        }

        try {
          String limitQuery = request.getQueryString("limit");
          System.out.println(limitQuery);
          limit = Integer.parseInt(limitQuery);
        } catch (NumberFormatException e) {
          log.info("No parameter limit");
        }

        Date dateMin = new Date(ageMin);
        Date dateMax = new Date(ageMax);
        log.debug("nationality="+nationality + " agemin=" + ageMin +" agemax="+ ageMax + " gender=" + gender + " travellerType=" + travellerType);

        return userRepository.searchUser(nationality, gender, dateMin, dateMax, travellerType, name, offset, limit)  //Just for testing purposes
                .thenApplyAsync((user) -> {
                    JsonNode userAsJson = Json.toJson(user);
                    log.debug(userAsJson.asText());

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }
}
