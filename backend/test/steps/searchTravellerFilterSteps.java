package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import utils.PlayResultToJson;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.AssertTrue;

import static play.test.Helpers.route;

public class searchTravellerFilterSteps {
    @Inject
    private Application application;
    private Result result;
    private String authToken;
    private ArrayNode array;

    @Before
    public void setUp() throws IOException {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        route(application, request);

        Helpers.start(application);
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
    }

    @Given("I have logged in with email {string} and password {string}")
    public void iHaveLoggedInWithEmailAndPassword(String email, String password) throws IOException {

        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password);

        Http.RequestBuilder loginRequest = Helpers.fakeRequest()
                .method("POST")
                .bodyJson(reqJsonBody)
                .uri("/api/auth/users/login");
        Result loginResult = route(application, loginRequest);
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(loginResult);
        this.authToken = authenticationResponseAsJson.get("token").asText();
        Assert.assertEquals(200, loginResult.status());
        Assert.assertNotNull(this.authToken);
    }

    @Given("the database has been populated with test data")
    public void theDatabaseHasBeenPopulatedWithTestData() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users");
        this.result = route(application, request);
        ArrayNode array = (ArrayNode) PlayResultToJson.convertResultToJson(result);
        Assert.assertTrue(array.size() > 0);
    }

    @When("I want all types of nationalities from the database")
    public void iRequestNationalitiesFromTheDatabase() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/nationalities");
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @Then("I get a list of all nationalities as follows:")
    public void iGetAListOfAllNationalitiesAsFollows(DataTable dataTable) {

        List<Map<String,String>> expectedResults = dataTable.asMaps();
        for (int i = 0; i < expectedResults.size(); i++) {
            Assert.assertEquals(Integer.parseInt(expectedResults.get(i).get("nationalityId")), this.array.get(i).get("nationalityId").asInt());
            Assert.assertEquals(expectedResults.get(i).get("nationalityName"), this.array.get(i).get("nationalityCountry").asText());
        }
    }

    @When("I search travellers with the {int} nationality id")
    public void iSearchTravellersWithTheNationalityId(Integer nationalityId) throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&nationality=" + nationalityId);
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @Then("I get the following [{string}] emails")
    public void iGetTheFollowing(String email) {
        Assert.assertEquals(email, this.array.get(0).get("email").asText());
    }

    @When("I search travellers with the gender Male")
    public void iRequestTravellersFromTheMaleGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Male");
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Female")
    public void iRequestTravellersFromTheFemaleGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Female");
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Other")
    public void iRequestTravellersFromTheOtherGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Other");
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the traveller type ID {int}")
    public void iRequestTravellersOfTheType(Integer travellerTypeId) throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&travellerType=" + travellerTypeId.toString());
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }
}
