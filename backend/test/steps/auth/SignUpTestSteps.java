package steps.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class SignUpTestSteps {
    @Inject
    private Application application;

    private JsonNode userData;
    private Result result;
    private Result signUpResponse;

    /**
     * Set up the backend server
     */
    @Before
    public void setUp() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);

        Helpers.start(application);
    }

    /**
     * Stop the backend server
     */
    @After
    public void tearDown() {
        Helpers.stop(application);
    }

    @Given("that I have valid user data to sign up:")
    public void thatIHaveValidUserDataToSignUp(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);
        Assert.assertEquals(5, this.userData.size());
    }

    @Given("that I have incomplete user data to sign up:")
    public void thatIHaveIncompleteUserDataToSignUp(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);
        Assert.assertTrue(this.userData.size() < 5);
    }

    @When("I click the Sign Up button")
    public void iClickTheSignUpButton() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(this.userData);
        this.result = route(application, request);
        Assert.assertTrue(!(this.result == null));
    }

    @Then("^I should receive a (\\d+) status code indicating that the User is successfully created$")
    public void iShouldReceiveAStatusCodeIndicatingThatTheUserIsSuccessfullyCreated(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Then("^I should receive a (\\d+) status code indicating that the User filled the form with invalid data$")
    public void iShouldReceiveAStatusCodeIndicatingThatTheUserFilledTheFormWithInvalidData(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }
}
