package steps;

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

import javax.inject.Inject;

import static play.test.Helpers.route;

public class DestinationTestingSteps {
    @Inject
    private Application application;
    private JsonNode userData;
    private Result result;

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

    @After
    public void tearDown() {
        Helpers.stop(application);
    }

    @Given("that I have destination data to create with:")
    public void thatIHaveDestinationDataToCreateWith(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);

        Http.RequestBuilder resample = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        Result result = route(application, resample);
    }

    @Given("that I have a destination created")
    public void thatIHaveADestinationCreated() {
        Http.RequestBuilder resample = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
    }

    @When("I make a {string} request to {string} with the data")
    public void iMakeARequestToWithTheData(String requestMethod, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(requestMethod)
                .uri(endpoint)
                .bodyJson(this.userData);
        this.result = route(application, request);
    }

    @Then("I should receive an {int} status code")
    public void iShouldReceieveAnStatusCode(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Then("I should receive a {int} status code when checking for the destination")
    public void iShouldReceiveAStatusCodeWhenCheckingForTheDestination(Integer expectedStatusCode) {
        Http.RequestBuilder checkDeletion = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/1");
        Result result = route(application, checkDeletion);
        Assert.assertEquals(expectedStatusCode, (Integer) result.status());
    }

}
