package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.UnauthorizedException;
import play.libs.Json;
import play.mvc.Http.Request;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import models.User;
import play.mvc.With;
import repository.AuthRepository;
import repository.UserRepository;
import util.Security;

import javax.inject.Inject;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import util.Responses;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.*;
import static util.AuthUtil.isAlpha;
import static util.AuthUtil.isValidEmailAddress;

/**
 * Controller handling authentication endpoints.
 */
public class AuthController {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final Security security;
    private final Responses responses;

    @Inject
    public AuthController(AuthRepository authRepository, UserRepository userRepository, HttpExecutionContext httpExecutionContext, Security security, Responses responses) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
        this.responses = responses;
    }

    /**
     * Signs a user up with minimal details and sends auth session to client
     * @param request Incoming http request
     * @return The inserted user as JSON, with status code 201.
     * Return status code 400 if signup request invalid.
     */
    public CompletionStage<Result> signup(Request request) {
        JsonNode jsonRequest = request.body().asJson();

        String messageKey = "message";

        // check that there is a request body, if not return badRequest
        if (jsonRequest == null) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid request body according to the API spec");
                return badRequest(message);
            });
        }
        // Checks if the JSON contains a first name, last name, password and email
        if (!(jsonRequest.has("firstName"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a first name with the JSON key as firstName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("lastName"))) {  // Checks if the JSON contains a last name
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a last name with the JSON key as lastName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("email"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid email address with the JSON key as email");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("password"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a password with at least 6 characters with the JSON key as password");
                return badRequest(message);
            });
        }

        String middleName = "";
        String firstName = jsonRequest.get("firstName").asText();
        String lastName = jsonRequest.get("lastName").asText();
        String email = jsonRequest.get("email").asText();
        String password = jsonRequest.get("password").asText();
        String hashedPassword = this.security.hashPassword(password);
        String userToken = this.security.generateToken();

        // Middle name is optional and checks if the middle name is a valid name
        if (jsonRequest.has("middleName")) {
            middleName = jsonRequest.get("middleName").asText();
            if (!(isAlpha(middleName)) || (middleName.length() < 2)) {
                return supplyAsync(() -> {
                    ObjectNode message = Json.newObject();
                    message.put(messageKey, "Please provide a valid middle name that contains only letters and has at least 2 characters");
                    return badRequest(message);
                });
            }
        }

        // Checks if the first name, last name, email and password given is valid
        if ((firstName.isEmpty()) || !(isAlpha(firstName)) || (firstName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid first name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((lastName.isEmpty()) || !(isAlpha(lastName)) || (lastName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid last name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((email.isEmpty()) || !(isValidEmailAddress(email))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid email address");
                return badRequest(message);
            });
        } else if (password.isEmpty() || (password.length() < 6) ) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(messageKey, "Please provide a valid password with at least 6 characters");
                return badRequest(message);
            });
        }

        User user = new User(firstName, middleName,lastName, email, hashedPassword, userToken);
        return authRepository.insert(user)
                .thenApplyAsync((insertedUser) -> created(Json.toJson(insertedUser)), httpExecutionContext.current());
    }

    /**
     * Logs a user in
     * @param request - Request to get JSon fields from
     * @return 200 status code with users data if valid credentials,
     * otherwise sends 401 if unauthorized.
     */
    public CompletionStage<Result> login(Request request) {
        JsonNode jsonBody = request.body().asJson();

        String email = jsonBody.get("email").asText();
        String password = jsonBody.get("password").asText();
        String hashedPassword = this.security.hashPassword(password);

        return authRepository.getUserByCredentials(email, hashedPassword)
                .thenComposeAsync((optionalUser) -> {
                    if (!optionalUser.isPresent()) {
                        throw new CompletionException(new UnauthorizedException());
                    }

                    User user = optionalUser.get();

                    if (!security.comparePasswordAndHash(password, user.getPasswordHash())) {
                        throw new CompletionException(new UnauthorizedException());
                    }

                    String token = this.security.generateToken();
                    user.setToken(token);

                    return userRepository.updateUser(user);
                }, httpExecutionContext.current())
                .thenApplyAsync((user) -> {
                            JsonNode userJson = Json.toJson(user);
                            return ok(userJson);
                        }
                        , httpExecutionContext.current())
                .exceptionally(e -> {
                    try {
                        throw e.getCause();
                    } catch (UnauthorizedException noAuthError) {
                        return unauthorized();
                    } catch (Throwable genericError) {
                        return internalServerError();
                    }
                });
    }

    /**
     * Logs a user out by setting their auth token to null.
     * @param request incoming HTTP request.
     * @return 200 status code.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> logout(Request request) {
        User user = request.attrs().get(ActionState.USER);
        user.setToken(null);

        return userRepository.updateUser(user)
                .thenApplyAsync((u) -> {
                    return ok();
                }, httpExecutionContext.current());

    }

    /**
     * Checks the database for the given email, to see whether it's available.
     * @param email email to check the db for.
     * @return 400 if the email is empty. 409 if the email is taken. 200 if the email is available.
     */
    public CompletionStage<Result> checkEmailAvailable(String email) {
        if (email.isEmpty()) {
            return supplyAsync(() -> badRequest());
        }
        return authRepository.getUserByEmail(email)
                .thenApplyAsync((user) -> {
                    if (user.isPresent()) {
                        return status(409);
                    }
                    return ok();
                }, httpExecutionContext.current());
    }
}
