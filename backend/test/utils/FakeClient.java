package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
import models.Destination;
import models.User;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Specifies what a fake client for the application needs to do. <b>NOTE:</b> this will be useful for testing :)
 *
 * @author eba54
 * @author ash102
 */
public interface FakeClient {

    /**
     * Make a request (with a body) to the application, while not providing an authorisation token.
     *
     * @param method   the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param reqBody  the body of the request
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @return the result of the request
     */
    Result makeRequestWithNoToken(String method, ObjectNode reqBody, String endpoint);

    /**
     * Make a request (with no body) to the application, while not providing an authorisation token.
     *
     * @param method   the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @return the result of the request
     */
    Result makeRequestWithNoToken(String method, String endpoint);

    /**
     * Make a request (with a body) to the application, providing an authorisation token.
     *
     * @param method    the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param reqBody   the body of the request
     * @param endpoint  the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @param authToken the token for the logged in user
     * @return the result of the request
     */
    Result makeRequestWithToken(String method, ObjectNode reqBody, String endpoint, String authToken);

    /**
     * Make a request (with no body) to the application, providing an authorisation token.
     *
     * @param method    the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint  the backend endpoint that the request is going to, e.g. "/api/users/2", /"api/internal/resample"
     * @param authToken the token for the logged in user
     * @return the result of the request
     */
    Result makeRequestWithToken(String method, String endpoint, String authToken);

    /**
     * Log in a user that already exists in the database. <b>NOTE:</b> will add token to user and update it to database.
     *
     * @param user     the user that we want to login, assuming that the user has been saved into the database
     * @param password the password for the user
     * @return the logged in user (with the authToken available via its getter)
     * @throws FailedToLoginException when we couldn't log in successfully
     */
    User loginMadeUpUser(User user, String password) throws FailedToLoginException;

    /**
     * Sends a request to sign up a user with no middle name.
     *
     * @param firstName the first name of the user
     * @param lastName  the last name of the user.
     * @param email     the email of the user.
     * @param password  the password of the user.
     * @return the user object.
     * @throws IOException when the result cannot be parsed into a json.
     */
    User signUpUser(String firstName, String lastName, String email, String password) throws IOException, FailedToSignUpException, ServerErrorException;

    /**
     * Sends a request to sign up a user from a user json object.
     *
     * @param userJson the user as a Json Object.
     * @return the user object.
     * @throws IOException when the result cannot be parsed into a json.
     */
    User signUpUser(JsonNode userJson) throws IOException, FailedToSignUpException, ServerErrorException;

    /**
     * Creates a test destination to run tests with. Using the create destination endpoint.
     *
     * @param destinationNode the JsonNode containing the new destinations data.
     * @param authToken       the suthentication token to add to the request.
     * @return the test destination.
     */
    Destination makeTestDestination(JsonNode destinationNode, String authToken) throws IOException, UnauthorizedException, ServerErrorException;

    /**
     * Make a request (with a multipart/form body) to the application, providing an authorisation token.
     *
     * @param method      the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint    the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @param token       the authentication token to send in the header
     * @param file        the file object to send in the body
     * @param otherFields a map of other fields to send in the body
     * @return the result of the request
     */
    Result makeMultipartFormRequestWithFileAndToken(String method, String endpoint, String token, File file, Map<String, String> otherFields);

    /**
     * Make a request (with a multipart/form body) to the application, providing an authorisation token.
     *
     * @param method      the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint    the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @param file        the file object to send in the body
     * @param otherFields a map of other fields to send in the body
     * @return the result of the request
     */
    Result makeMultipartFormRequestWithFileNoToken(String method, String endpoint, File file, Map<String, String> otherFields);

    /**
     * Make a request (with a multipart/form body) to the application, providing an authorisation token.
     *
     * @param method      the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint    the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @param token       the authentication token to send in the header
     * @param otherFields a map of other fields to send in the body
     * @return the result of the request
     */
    Result makeMultipartFormRequestWithToken(String method, String endpoint, String token, Map<String, String> otherFields);

    /**
     * Make a request (with a multipart/form body) to the application, providing an authorisation token.
     *
     * @param method      the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint    the backend endpoint that the request is going to, e.g. "/api/users/2", "/api/internal/resample"
     * @param otherFields a map of other fields to send in the body
     * @return the result of the request
     */
    Result makeMultipartFormRequestNoToken(String method, String endpoint, Map<String, String> otherFields);

}
