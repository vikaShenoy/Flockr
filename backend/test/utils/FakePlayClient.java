package utils;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
import models.Destination;
import models.User;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        } else {
            throw new ServerErrorException();
        }
    }

    @Override
    public Destination makeTestDestination(JsonNode destinationNode, String authToken, int userId) throws IOException, UnauthorizedException, ServerErrorException {
        ((ObjectNode) destinationNode).set("travellerTypeIds", Json.toJson(new ArrayList<>()));
        Result result = this.makeRequestWithToken("POST", (ObjectNode) destinationNode, "/api/users/" + userId + "/destinations", authToken);
        if (result.status() == 201) {
            JsonNode destinationAsJsonNode = PlayResultToJson.convertResultToJson(result);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.treeToValue(destinationAsJsonNode, Destination.class);
        } else if (result.status() == 401) {
            throw new UnauthorizedException("You are not authorized to perform this operation.");
        } else {
            System.out.println("The failed request was " + result.status());
            throw new ServerErrorException();
        }
    }

    @Override
    public Result makeMultipartFormRequestWithFileAndToken(String method, String endpoint, String token, File file, Map<String, String> otherFields) {
        // Determine the content type
        String contentType = getContentType(file);

        // Create the file part object
        Http.MultipartFormData.FilePart<akka.stream.javadsl.Source<akka.util.ByteString,?>> part =
                new Http.MultipartFormData.FilePart<>(
                        "image",
                        file.getName(),
                        contentType,
                        FileIO.fromPath(file.toPath()));

        // Construct the part list and add the file and text fields
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partList = new ArrayList<>();
        partList.add(part);
        for (String key: otherFields.keySet()) {
            partList.add(new Http.MultipartFormData.DataPart(key, otherFields.get(key)));
        }

        // Build the request
        Http.RequestBuilder request = Helpers.fakeRequest().uri(endpoint)
                .method(method)
                .header("Authorization", token)
                .bodyRaw(partList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        this.application.asScala().materializer());

        // Send the request and return the result
        return Helpers.route(this.application, request);
    }

    @Override
    public Result makeMultipartFormRequestWithFileNoToken(String method, String endpoint, File file, Map<String, String> otherFields) {
        // Determine the content type
        String contentType = getContentType(file);

        // Create the file part object
        Http.MultipartFormData.FilePart<akka.stream.javadsl.Source<akka.util.ByteString,?>> part =
                new Http.MultipartFormData.FilePart<>(
                "image",
                file.getName(),
                contentType,
                FileIO.fromPath(file.toPath()));

        // Construct the part list and add the file and text fields
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partList = new ArrayList<>();
        partList.add(part);
        for (String key: otherFields.keySet()) {
            partList.add(new Http.MultipartFormData.DataPart(key, otherFields.get(key)));
        }

        // Build the request
        Http.RequestBuilder request = Helpers.fakeRequest().uri(endpoint)
                .method(method)
                .bodyRaw(partList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        this.application.asScala().materializer());

        // Send the request and return the result
        return Helpers.route(this.application, request);
    }

    @Override
    public Result makeMultipartFormRequestWithToken(String method, String endpoint, String token, Map<String, String> otherFields) {
        // Construct the part list and add the file and text fields
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partList = new ArrayList<>();
        for (String key: otherFields.keySet()) {
            partList.add(new Http.MultipartFormData.DataPart(key, otherFields.get(key)));
        }

        // Build the request
        Http.RequestBuilder request = Helpers.fakeRequest().uri(endpoint)
                .method(method)
                .header("Authorization", token)
                .bodyRaw(partList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        this.application.asScala().materializer());

        // Send the request and return the result
        return Helpers.route(this.application, request);
    }

    @Override
    public Result makeMultipartFormRequestNoToken(String method, String endpoint, Map<String, String> otherFields) {
        // Construct the part list and add the file and text fields
        List<Http.MultipartFormData.Part<Source<ByteString, ?>>> partList = new ArrayList<>();
        for (String key: otherFields.keySet()) {
            partList.add(new Http.MultipartFormData.DataPart(key, otherFields.get(key)));
        }

        // Build the request
        Http.RequestBuilder request = Helpers.fakeRequest().uri(endpoint)
                .method(method)
                .bodyRaw(partList,
                        play.libs.Files.singletonTemporaryFileCreator(),
                        this.application.asScala().materializer());

        // Send the request and return the result
        return Helpers.route(this.application, request);
    }

    /**
     * Gets the content type from an image file.
     *
     * @param file the file object.
     * @return the content type.
     */
    private String getContentType(File file) {
        if (file.getName().contains(".jpg") || file.getName().contains(".jpeg")) {
            return "image/jpeg";
        } else if (file.getName().contains(".png")) {
            return "image/png";
        } else {
            return "";
        }
    }
}
