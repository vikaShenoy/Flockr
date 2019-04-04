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

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import static play.test.Helpers.route;

public class DestinationTestingSteps {
    @Inject
    private Application application;
    private JsonNode userData;
    private Result result;

    // user data
    private String firstName;
    private String middleName;
    private String lastName;
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
        this.firstName = firstRow.get("firstName");
        this.middleName = firstRow.get("middleName");
        this.lastName = firstRow.get("lastName");
        this.email = firstRow.get("email");
        this.plainTextPassword = firstRow.get("password");


        // sign up a user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(200, signUpRes.status());

        // log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.email);
        logInReqBody.put("password", this.plainTextPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(signUpReqBody);
        Result logInRes = route(application, logInReq);

        System.out.println(utils.PlayResultToJson.convertResultToJson(logInRes));

        Assert.assertEquals(200, logInRes.status());

        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);

        // make the token available for the rest of the class
        this.authToken = logInResBody.get("token").asText();
        Assert.assertNotNull(this.authToken);
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
        Result result = route(application, resample);
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
    public void iShouldReceiveAnStatusCode(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Then("I should receive a {int} status code when getting the destination with id {int}")
    public void iShouldReceiveAStatusCodeWhenCheckingForTheDestination(Integer expectedStatusCode, Integer destinationId) throws IOException {
        Http.RequestBuilder checkDeletion = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/" + destinationId.toString());
        Result getDestRes = route(application, checkDeletion);
        Assert.assertEquals(expectedStatusCode, (Integer) getDestRes.status());
    }

    @Given("that I have a destination created with id {int}")
    public void thatIHaveADestinationCreatedWithId(int destinationId) throws IOException {
        Http.RequestBuilder checkDeletion = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/" + destinationId);
        Result result = route(application, checkDeletion);
        // check that the destination's name has some text in it
        JsonNode res = utils.PlayResultToJson.convertResultToJson(result);
        String destinationName = res.get("destinationName").asText();
        Assert.assertTrue(destinationName.length() > 0);
    }

    @When("I make a {string} request to {string} to delete the destination")
    public void iMakeARequestToToDeleteTheDestination(String requestMethod, String endpoint) throws IOException {
        Http.RequestBuilder deleteReq = Helpers.fakeRequest()
                .method(requestMethod)
                .uri(endpoint)
                .header("Authorization", this.authToken);
        Result deleteRes = route(application, deleteReq);
        Assert.assertEquals(200, deleteRes.status());
    }

    @Given("that I am logged in")
    public void thatIAmLoggedIn() {
        Assert.assertTrue(this.authToken.length() > 0);
    }
}
