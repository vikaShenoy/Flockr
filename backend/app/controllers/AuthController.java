package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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
     * Signs a user up with minimal details and sends auth session to client
     * @param request Incoming http request
     * @return The inserted user as JSON
     */
    public CompletionStage<Result> signup(Request request) {
        JsonNode jsonRequest = request.body().asJson();
        String firstName = jsonRequest.get("firstName").asText();
        String lastName = jsonRequest.get("lastName").asText();
        String email = jsonRequest.get("email").asText();
        String password = jsonRequest.get("password").asText();

        String hashedPassword = this.security.hashPassword(password);
        String userToken = this.security.generateToken();


        User user = new User(firstName, lastName, email, hashedPassword, userToken);

        return this.authRepository.insert(user)
        .thenApplyAsync((insertedUser) -> ok(Json.toJson(insertedUser)), httpExecutionContext.current());

    }
}
