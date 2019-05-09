package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class TestAuthenticationHelper {


    /**
     * A helper method for creating and authenticating a user for running tests.
     * @param dataTable a DataTable with the users details
     * @param application a play Application
     * @return String the authentication token returned from the database after login
     * @throws IOException when the server returns an error
     */
    public static String theFollowingUserExists(DataTable dataTable, Application application) throws IOException {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        String email = firstRow.get("email");
        String plainTextPassword = firstRow.get("password");


        // sign up a user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(201, signUpRes.status());

        // log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", email);
        logInReqBody.put("password", plainTextPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(signUpReqBody);
        Result logInRes = route(application, logInReq);

        Assert.assertEquals(200, logInRes.status());

        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);

        // make the token available for the rest of the class
        String authToken = logInResBody.get("token").asText();

        Assert.assertNotNull(authToken);
        return authToken;
    }

    /**
     * Method to log in with given user credentials and return an auth token
     * @param email String the email of the user
     * @param password String the password of the user
     * @param application play Application instance
     * @return String the auth token returned by the server
     * @throws IOException when the server returns an error
     */
    public static String login(String email, String password, Application application) throws IOException {

        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password);

        Http.RequestBuilder loginRequest = Helpers.fakeRequest()
                .method("POST")
                .bodyJson(reqJsonBody)
                .uri("/api/auth/users/login");
        Result loginResult = route(application, loginRequest);
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(loginResult);

        Assert.assertEquals(200, loginResult.status());

        String authToken = authenticationResponseAsJson.get("token").asText();

        Assert.assertNotNull(authToken);

        return authToken;
    }
}
