package steps.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import models.User;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Result;
import utils.TestState;
import utils.FakeClient;
import utils.PlayResultToJson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LoginTestSteps {

    private JsonNode userData;
    private Result loginResponse;
    private JsonNode loginData;

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

    @Given("the user has the following data to login with")
    public void iHaveTheFollowingDataToLoginWith(DataTable loginData) {
        List<Map<String, String>> list = loginData.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.loginData = Json.toJson(firstRow);
    }


    @When("the user tries to login")
    public void theUserTriesToLogin() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<User> users = User.find.all();

        Assert.assertEquals(users.size(), 1);

        Result loginResponse = fakeClient.makeRequestWithNoToken("POST", (ObjectNode) loginData, "/api/auth/users/login");
        this.loginResponse = loginResponse;
    }

    @Then("the response should have an authentication token")
    public void theResponseShouldHaveAnAuthenticationToken() throws IOException {
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(this.loginResponse);
        String authToken = authenticationResponseAsJson.get("token").asText();

        Assert.assertEquals(200, this.loginResponse.status());
        Assert.assertTrue(authToken.length() > 0);
    }

    @Then("the user should be logged in")
    public void theUserShouldBeLoggedIn() throws IOException  {
        JsonNode loginResponseBody = PlayResultToJson.convertResultToJson(loginResponse);
        Assert.assertEquals(200, loginResponse.status());
        Assert.assertNotEquals("", loginResponseBody.get("token"));
    }

    @Then("the user should not be logged in")
    public void theUserShouldNotBeLoggedIn() {
        Assert.assertNotEquals(200, loginResponse.status());
    }


    @Then("the server should not log me in")
    public void theServerShouldNotLogMeIn() {
        Assert.assertEquals(401, this.loginResponse.status());
    }
}
