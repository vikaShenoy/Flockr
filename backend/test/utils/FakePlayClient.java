package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
import models.Destination;
import models.User;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.rmi.ServerError;

import static play.test.Helpers.route;

/**
 * A fake client for the Play application.
 */
public class FakePlayClient implements FakeClient {

    /**
     * The play application under test.
     */
    private Application application;

    /**
     * Construct a fake client for our application.
     * @param application the Play application we want to make requests to
     */
    public FakePlayClient(Application application) {
        this.application = application;
    }

    @Override
    public Result makeRequestWithNoToken(String method, ObjectNode reqBody, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(method)
                .bodyJson(reqBody)
                .uri(endpoint);
        return route(this.application, request);
    }

    @Override
    public Result makeRequestWithNoToken(String method, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(method)
                .uri(endpoint);
        return route(this.application, request);
    }

    @Override
    public Result makeRequestWithToken(String method, ObjectNode reqBody, String endpoint, String authToken) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(method)
                .header("Authorization", authToken)
                .bodyJson(reqBody)
                .uri(endpoint);
        return route(this.application, request);
    }

    @Override
    public Result makeRequestWithToken(String method, String endpoint, String authToken) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(method)
                .header("Authorization", authToken)
                .uri(endpoint);
        return route(this.application, request);
    }

    @Override
    public User loginMadeUpUser(User user, String password) throws FailedToLoginException {
        // make request
        ObjectNode req = Json.newObject();
        req.put("email", user.getEmail());
        req.put("password", password);
        Result res = this.makeRequestWithNoToken("POST", req, "/api/auth/users/login");
        try {
            JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
        } catch(IOException e) {
            throw new FailedToLoginException("Could not convert result to JSON: " + e);
        }

        // check that we logged in successfully
        boolean wasLoggedInSuccessfully = res.status() == 200;
        if (!wasLoggedInSuccessfully) {
            throw new FailedToLoginException("Could not log in fake user: " + user);
        } else {
            try {
                JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
                String authToken = resAsJson.get("token").asText();
                user.setToken(authToken);
                user.update();
                return user;
            } catch (IOException e) {
                throw new FailedToLoginException("Failed to convert the Play result to JSON" + e.getMessage());
            }
        }
    }

    @Override
    public User signUpUser(String firstName, String lastName, String email, String password) throws IOException, FailedToSignUpException, ServerErrorException {
        ObjectNode userAsJson = Json.newObject();
        userAsJson.put("firstName", firstName);
        userAsJson.put("lastName", lastName);
        userAsJson.put("email", email);
        userAsJson.put("password", password);

        return this.signUpUser(userAsJson);
    }

    @Override
    public User signUpUser(JsonNode userJson) throws IOException, FailedToSignUpException, ServerErrorException {
        Result result = this.makeRequestWithNoToken("POST", (ObjectNode) userJson, "/api/auth/users/signup");
        if (result.status() == 400) {
            throw new FailedToSignUpException("Failed to sign up the user.");
        } else if (result.status() == 201) {
            JsonNode userAsJsonNode = PlayResultToJson.convertResultToJson(result);
            User user = new User(userAsJsonNode.get("firstName").asText(), "",
                    userAsJsonNode.get("lastName").asText(), userAsJsonNode.get("email").asText(),
                    userAsJsonNode.get("passwordHash").asText(), userAsJsonNode.get("token").asText());
            user.setUserId(userAsJsonNode.get("userId").asInt());
            return user;
            // TODO: fix everything to use default admin instead of super admin so the following will work.
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.treeToValue(userAsJsonNode, User.class);
        } else {
            throw new ServerErrorException();
        }
    }

    @Override
    public Destination makeTestDestination(ObjectNode destinationNode, String authToken) throws IOException, UnauthorizedException, ServerErrorException {
        Result result = this.makeRequestWithToken("POST", destinationNode, "/api/destinations", authToken);
        if (result.status() == 201) {
            JsonNode destinationAsJsonNode = PlayResultToJson.convertResultToJson(result);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.treeToValue(destinationAsJsonNode, Destination.class);
        } else if (result.status() == 401) {
            throw new UnauthorizedException("You are not authorized to perform this operation.");
        } else {
            throw new ServerErrorException();
        }
    }
}
