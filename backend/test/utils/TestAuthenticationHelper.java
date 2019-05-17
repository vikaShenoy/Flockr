package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import models.User;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class TestAuthenticationHelper {


    /**
     * A helper method for creating and authenticating a user for running tests.
     * The user object is placed in the TestState for retrieval by multiple test classes.
     *
     * @param dataTable a DataTable with the users details
     * @param application a play Application
     */
    public static void theFollowingUserExists(DataTable dataTable, Application application) {
        TestState testState = TestState.getInstance();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        String plainTextPassword = firstRow.get("password");

        // sign up a user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        try {
            User user = testState.getFakeClient().signUpUser(signUpReqBody);
            Assert.assertNotEquals(0, user.getUserId());

            user = testState.getFakeClient().loginMadeUpUser(user, plainTextPassword);
            Assert.assertNotEquals("", user.getToken());
            testState.addUser(user);
        } catch (IOException | FailedToSignUpException | ServerErrorException | FailedToLoginException e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Method to log in with given user credentials and return an auth token
     *
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
