package steps.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import models.User;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import steps.TestState;
import utils.FakeClient;
import utils.PlayResultToJson;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class LoginTestSteps {

    private JsonNode userData;
    private Result loginResponse;

    @Given("that I have signed up successfully with valid data:")
    public void thatIHaveSignedUpSuccessfullyWithValidData(DataTable dataTable) {
        TestState testState = TestState.getInstance();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);
        try {
            User user = testState.getFakeClient().signUpUser(userData);
            Assert.assertNotEquals(0, user.getUserId());
        } catch (IOException | FailedToSignUpException | ServerErrorException e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
        }
    }


    @When("I write correct login credentials in the Login form and I click the Login button")
    public void iWriteCorrectLoginCredentialsInTheLoginFormAndIClickTheLoginButton() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        // Gets the user credentials from the initial data
        String email = this.userData.get("email").asText();
        String password = this.userData.get("password").asText();

        // Constructing the request body
        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password);

        this.loginResponse = fakeClient.makeRequestWithNoToken(
                "POST", reqJsonBody, "/api/auth/users/login");
        Assert.assertTrue(!(this.loginResponse == null));
    }

    @Then("the response should have an authentication token")
    public void theResponseShouldHaveAnAuthenticationToken() throws IOException {
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(this.loginResponse);
        String authToken = authenticationResponseAsJson.get("token").asText();

        Assert.assertEquals(200, this.loginResponse.status());
        Assert.assertTrue(authToken.length() > 0);
    }

    @When("I write incorrect login credentials in the Login form and I click the Login button")
    public void iWriteIncorrectLoginCredentialsInTheLoginFormAndIClickTheLoginButton() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        // Gets the user credential from the initial data
        String email = this.userData.get("email").asText();
        String password = this.userData.get("password").asText();

        // Constructing the request body
        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password + "wrong-password");

        this.loginResponse = fakeClient.makeRequestWithNoToken(
                "POST", reqJsonBody, "/api/auth/users/login");
        Assert.assertNotNull(this.loginResponse);
    }

    @Then("the server should not log me in")
    public void theServerShouldNotLogMeIn() {
        Assert.assertEquals(401, this.loginResponse.status());
    }
}
