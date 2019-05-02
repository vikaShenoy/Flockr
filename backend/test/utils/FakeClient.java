package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import models.User;
import play.mvc.Result;

/**
 * Specifies what a fake client for the application needs to do. <b>NOTE:</b> this will be useful for testing :)
 * @author eba54
 */
public interface FakeClient {

    /**
     * Make a request (with a body) to the application, while not providing an authorisation token.
     * @param method the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param reqBody the body of the request
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "api/internal/resample"
     */
    Result makeRequestWithNoToken(String method, ObjectNode reqBody, String endpoint);

    /**
     * Make a request (with no body) to the application, while not providing an authorisation token.
     * @param method the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "api/internal/resample"
     */
    Result makeRequestWithNoToken(String method, String endpoint);

    /**
     * Make a request (with a body) to the application, providing an authorisation token.
     * @param method the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param reqBody the body of the request
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "api/internal/resample"
     * @param authToken the token for the logged in user
     */
    Result makeRequestWithToken(String method, ObjectNode reqBody, String endpoint, String authToken);

    /**
     * Make a request (with no body) to the application, providing an authorisation token.
     * @param method the HTTP request method e.g. "POST", "GET", "DELETE", etc
     * @param endpoint the backend endpoint that the request is going to, e.g. "/api/users/2", "api/internal/resample"
     * @param authToken the token for the logged in user
     */
    Result makeRequestWithToken(String method, String endpoint, String authToken);


    /**
     * Log in a user that already exists in the database. <b>NOTE:</b> will add token to user and update it to database.
     * @param user the user that we want to login, assuming that the user has been saved into the database
     * @param password the password for the user
     * @return the logged in user (with the authToken available via its getter)
     * @throws FailedToLoginException when we couldn't log in successfully
     */
    User loginMadeUpUser(User user, String password) throws FailedToLoginException;
}
