package controllers;

import actions.ActionState;
import actions.Admin;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.ConflictingRequestException;
import exceptions.UnauthorizedException;
import models.User;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import repository.AuthRepository;
import repository.RoleRepository;
import repository.UserRepository;
import util.Responses;
import util.Security;
import javax.inject.Inject;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.*;
import static util.AuthUtil.isAlpha;
import static util.AuthUtil.isValidEmailAddress;

/**
 * Controller handling authentication endpoints
 */
public class AuthController {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final Security security;
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String MIDDLE_NAME_KEY = "middleName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String MESSAGE_KEY = "message";
    private static final String PASSWORD_KEY = "password";
    private static final String EMAIL_KEY = "email";

    @Inject
    public AuthController(AuthRepository authRepository, UserRepository userRepository,
                          HttpExecutionContext httpExecutionContext, Security security, Responses responses,
                          RoleRepository roleRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
    }

    /**
     * Signs a user up with minimal details and sends auth session to client
     * @param request Incoming http request
     * @return The inserted user as JSON, with status code 201.
     * Return status code 400 if signup request invalid.
     */
    public CompletionStage<Result> signup(Request request) {
        JsonNode jsonRequest = request.body().asJson();

        // check that there is a request body, if not return badRequest
        if (jsonRequest == null) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid request body according to the API spec");
                return badRequest(message);
            });
        }
        // Checks if the JSON contains a first name, last name, password and email
        if (!(jsonRequest.has(FIRST_NAME_KEY))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a first name with the JSON key as firstName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has(LAST_NAME_KEY))) {  // Checks if the JSON contains a last name
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a last name with the JSON key as lastName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has(EMAIL_KEY))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid email address with the JSON key as email");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has(PASSWORD_KEY))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a password with at least 6 characters with the JSON key as password");
                return badRequest(message);
            });
        }

        String middleName = jsonRequest.has(MIDDLE_NAME_KEY) ? jsonRequest.get(MIDDLE_NAME_KEY).asText() : "";
        String firstName = jsonRequest.get(FIRST_NAME_KEY).asText();
        String lastName = jsonRequest.get(LAST_NAME_KEY).asText();
        String email = jsonRequest.get(EMAIL_KEY).asText();
        String password = jsonRequest.get(PASSWORD_KEY).asText();
        String hashedPassword = this.security.hashPassword(password);
        String userToken = this.security.generateToken();

        // Middle name is optional and checks if the middle name is a valid name
        if ((jsonRequest.has(MIDDLE_NAME_KEY) && (!isAlpha(middleName)) || middleName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid middle name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        }

        // Checks if the first name, last name, email and password given is valid
        if ((firstName.isEmpty()) || !(isAlpha(firstName)) || (firstName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid first name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((lastName.isEmpty()) || !(isAlpha(lastName)) || (lastName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid last name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((email.isEmpty()) || !(isValidEmailAddress(email))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid email address");
                return badRequest(message);
            });
        } else if (password.isEmpty() || (password.length() < 6) ) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Please provide a valid password with at least 6 characters");
                return badRequest(message);
            });
        }

        // check that a user with that email does not already exist, if so, return request forbidden
        return authRepository.getUserByEmail(email).thenComposeAsync((optionalUser -> {
            if (optionalUser.isPresent()) {
                throw new CompletionException(new ConflictingRequestException("Sorry, that email is taken"));
            }

            User user = new User(firstName, middleName, lastName, email, hashedPassword, userToken);
            return authRepository.insert(user).thenApplyAsync(insertedUser -> created(Json.toJson(insertedUser)),
                httpExecutionContext.current());
        }), httpExecutionContext.current())
        .exceptionally(error -> {
            try {
                throw error.getCause();
            } catch (ConflictingRequestException e) {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, e.getMessage());
                return Results.status(Http.Status.CONFLICT, message);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "Something went wrong trying to sign up");
                return internalServerError(message);
            }
        });
    }

    /**
     * Logs a user in
     * @param request - Request to get JSon fields from
     * @return 200 status code with users data if valid credentials,
     * otherwise sends 401 if unauthorized.
     */
    public CompletionStage<Result> login(Request request) {
        JsonNode jsonBody = request.body().asJson();

        String email = jsonBody.get(EMAIL_KEY).asText();
        String password = jsonBody.get(PASSWORD_KEY).asText();
        String hashedPassword = this.security.hashPassword(password);

        return authRepository.getUserByCredentials(email, hashedPassword)
                .thenComposeAsync(optionalUser -> {
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
                .thenApplyAsync(user -> {
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
                        genericError.printStackTrace();
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
            .thenApplyAsync(u -> ok(), httpExecutionContext.current());

    }

    /**
     * Logs a user of a given id out by setting their auth token to null.
     * @param userId User ID to Logout
     * @param request incoming HTTP request.
     * @return
     * - 200 status code if ok.
     * - 404 status code if the user being logged out can't be found in db.
     */
    @With({LoggedIn.class, Admin.class})
    public CompletionStage<Result> logoutById( int userId, Request request) {
       return userRepository.getUserById(userId).thenApplyAsync(optionalUser -> {
            if (!optionalUser.isPresent()) {
                ObjectNode message = Json.newObject();
                message.put(MESSAGE_KEY, "User not found");
                return notFound(message);
            }

            User userToLogout = optionalUser.get();
            userToLogout.setToken(null);
            userToLogout.save();
            ObjectNode message = Json.newObject();
            message.put(MESSAGE_KEY, "User successfully logged out");
            return ok(message);
        });

    }

    /**
     * Checks the database for the given email, to see whether it's available.
     * @param email email to check the db for.
     * @return 400 if the email is empty. 409 if the email is taken. 200 if the email is available.
     */
    public CompletionStage<Result> checkEmailAvailable(String email) {
        if (email.isEmpty()) return supplyAsync(Results::badRequest);
        return authRepository.getUserByEmail(email)
            .thenApplyAsync(user -> {
                if (user.isPresent()) {
                    return status(409);
                }
                return ok();
            }, httpExecutionContext.current());
    }
}
