package steps;

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

public class DestinationTestingSteps {
    @Inject
    private Application application;
    private JsonNode destinationData;
    private Result result;

    // user data
    private String email;
    private String plainTextPassword;
    private String authToken;

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

    @Given("a user with the following information exists:")
    public void aUserWithTheFollowingInformationExists(DataTable dataTable) throws IOException {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.email = firstRow.get("email");
        this.plainTextPassword = firstRow.get("password");

        // Signs up the user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(201, signUpRes.status());

        // Login the user to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.email);
        logInReqBody.put("password", this.plainTextPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(signUpReqBody);
        Result logInRes = route(application, logInReq);

        Assert.assertEquals(200, logInRes.status());
        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);

        // Make the token available for the rest of the class
        this.authToken = logInResBody.get("token").asText();
        Assert.assertNotNull(this.authToken);
    }

    @Given("that I want to create a Destination with the following valid data:")
    public void thatIWantToCreateADestinationWithTheFollowingValidData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.destinationData = Json.toJson(firstRow);
    }

    @Given("that I want to create a Destination with the following incomplete data:")
    public void thatIWantToCreateADestinationWithTheFollowingIncompleteData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        System.out.println(list.get(0));
        this.destinationData = Json.toJson(firstRow);
    }

    @When("I click the Add Destination button")
    public void iClickTheAddDestinationButton() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/destinations")
                .bodyJson(this.destinationData);
        this.result = route(application, request);
        Assert.assertTrue(!(this.result == null));
    }

    @Then("I should receive a {int} status code indicating that the Destination is successfully created")
    public void iShouldReceiveAnStatusCodeIndicatingThatTheDestinationIsSuccessfullyCreated(Integer expectedStatusCode) throws IOException {
        System.out.println(utils.PlayResultToJson.convertResultToJson(this.result));
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Then("I should receive a {int} status code indicating that the Destination is not successfully created")
    public void iShouldReceiveAStatusCodeIndicatingThatTheDestinationIsNotSuccessfullyCreated(Integer expectedStatusCode) throws IOException {
        System.out.println(utils.PlayResultToJson.convertResultToJson(this.result));
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Given("that I have resampled the database and there is a Destination with an ID of one")
    public void thatIHaveResampledTheDatabaseAndThereIsADestinationWithAnIDOfOne() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        this.result = route(application, request);
        Assert.assertEquals(200, this.result.status());
    }

    @When("I click the Delete Destination button")
    public void iClickTheDeleteButton() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/api/destinations/1")
                .header("Authorization", this.authToken);
        this.result = route(application, request);
        Assert.assertEquals(200, this.result.status());
    }

    @Then("I try to search the Destination with the ID of one")
    public void iTryToSearchTheDestinationWithTheIDOfOne() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/1");
        this.result = route(application, request);
    }

    @Then("I should receive a {int} status code indicating that the Destination with the given ID is not found")
    public void iShouldReceiveAStatusCodeIndicatingThatTheDestinationIsSuccessfullyDeleted(Integer expectedStatusCode) throws IOException {
        System.out.println(utils.PlayResultToJson.convertResultToJson(this.result));
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

}
