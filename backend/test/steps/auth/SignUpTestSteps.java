package steps.auth;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import models.User;
import org.junit.Assert;
import play.libs.Json;
import utils.TestState;
import utils.FakeClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;
import static play.test.Helpers.testServer;

public class SignUpTestSteps {

    private Exception signUpException;

    @Given("that the user has valid user data to sign up")
    public void thatIHaveValidUserDataToSignUp(DataTable dataTable) {
        TestState testState = TestState.getInstance();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        testState.setUserData(firstRow);
    }

    @Given("the user has incomplete user data to sign up")
    public void thatIHaveIncompleteUserDataToSignUp(DataTable dataTable) {
        TestState testState = TestState.getInstance();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        testState.setUserData(firstRow);
    }

    @When("the user signs up")
    public void iSignUpTheUser() throws FailedToSignUpException, ServerErrorException, IOException {
        TestState testState = TestState.getInstance();
        FakeClient fakeClient = testState.getFakeClient();
        try {
            User user = fakeClient.signUpUser(Json.toJson(testState.getUserData()));
            testState.addUser(user);
            Assert.assertNotNull(user);
            Assert.assertNotEquals(0, user.getUserId());
        } catch (Exception e) {
            signUpException = e;
        }
    }

    @Then("the user now exists in the system")
    public void theUserIsNowStoredInTheSystem() {
        User createdUser = TestState.getInstance().getUser(0);
        User user = User.find.byId(createdUser.getUserId());
        Assert.assertNotNull(user);
        Assert.assertEquals(createdUser.getUserId(), user.getUserId());
    }

    @Then("^I should receive an error message saying \"([^\"]*)\"$")
    public void iShouldReceiveAnErrorMessageSaying(String message) {
        Assert.assertEquals(message, signUpException.getMessage());
    }
}
