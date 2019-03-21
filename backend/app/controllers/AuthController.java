package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Http.Request;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import models.User;
import repository.AuthRepository;
import util.Security;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import util.Responses;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

/**
 * Controller handling authentication endpoints
 */
public class AuthController {
    private final AuthRepository authRepository;
    private final HttpExecutionContext httpExecutionContext;
    private final Security security;
    private final Responses responses;

    @Inject
    public AuthController(AuthRepository authRepository, HttpExecutionContext httpExecutionContext, Security security, Responses responses) {
        this.authRepository = authRepository;
        this.httpExecutionContext = httpExecutionContext;
        this.security = security;
        this.responses = responses;
    }

    /**
     * A function that checks if the given string contains all alphabet letters. If yes, it returns true.
     * Otherwise, return false.
     * @param name The name of the User
     * @return true or false depending on the content of the string
     */
    public boolean isAlpha(String name) {
        return name.matches("[a-zA-Z ]+");
    }

    /**
     * A function that checks if the given email is a valid email format. If yes, it returns true.
     * Otherwise, returns false.
     * @param email
     * @return
     */
    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Signs a user up with minimal details and sends auth session to client
     * @param request Incoming http request
     * @return The inserted user as JSON
     */
    public CompletionStage<Result> signup(Request request) {
        JsonNode jsonRequest = request.body().asJson();

        // check that there is a request body, if not return badRequest
        if (jsonRequest == null) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid request body according to the API spec");
                return badRequest(message);
            });
        }
        // Checks if the JSON contains a first name, last name, password and email
        if (!(jsonRequest.has("firstName"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a first name with the JSON key as firstName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("lastName"))) {  // Checks if the JSON contains a last name
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a last name with the JSON key as lastName");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("email"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid email address with the JSON key as email");
                return badRequest(message);
            });
        } else if (!(jsonRequest.has("password"))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a password with at least 6 characters with the JSON key as password");
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
                    message.put("message", "Please provide a valid middle name that contains only letters and has at least 2 characters");
                    return badRequest(message);
                });
            }
        }

        // Checks if the first name, last name, email and password given is valid
        if ((firstName.isEmpty()) || !(isAlpha(firstName)) || (firstName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid first name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((lastName.isEmpty()) || !(isAlpha(lastName)) || (lastName.length() < 2)) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid last name that contains only letters and has at least 2 characters");
                return badRequest(message);
            });
        } else if ((email.isEmpty()) || !(isValidEmailAddress(email))) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid email address");
                return badRequest(message);
            });
        } else if (password.isEmpty() || (password.length() < 6) ) {
            return supplyAsync(() -> {
                ObjectNode message = Json.newObject();
                message.put("message", "Please provide a valid password with at least 6 characters");
                return badRequest(message);
            });
        }

        User user = new User(firstName, middleName,lastName, email, hashedPassword, userToken);
        return authRepository.insert(user)
                .thenApplyAsync((insertedUser) -> ok(Json.toJson(insertedUser)), httpExecutionContext.current());
    }

}
