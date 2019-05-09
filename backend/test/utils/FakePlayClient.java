package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import models.User;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;

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
                user.save();
                return user;
            } catch (IOException e) {
                throw new FailedToLoginException("Failed to convert the Play result to JSON" + e.getMessage());
            }
        }
    }
}
