package steps.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
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
        Assert.assertTrue(this.userData.size() == 5);
    }

    @Given("that I have incomplete user data to sign up:")
    public void thatIHaveIncompleteUserDataToSignUp(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);
        Assert.assertTrue(this.userData.size() < 5);
    }

    @When("I make an {string} request to {string} with the data")
    public void iMakeARequestToWithTheData(String requestMethod, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(requestMethod)
                .uri(endpoint)
                .bodyJson(this.userData);
        this.result = route(application, request);
        Assert.assertTrue(!(this.result == null));
    }

    @Then("I should receive a {int} status code")
    public void iShouldReceiveAStatusCode(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }
}
