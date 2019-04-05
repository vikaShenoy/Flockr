package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.AfterStep;
import cucumber.api.java.Before;
import cucumber.api.java.BeforeStep;
import cucumber.api.java.en.And;
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
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.PlayResultToJson;
import utils.TestAuthenticationHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class searchTravellerFilterSteps {
    @Inject
    private Application application;
    private ArrayList<JsonNode> searchResults;
    private Result result;
    private String authToken;
    private ArrayNode array;

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

    @Given("the following user exists:")
    public void theFollowingUserExists(DataTable dataTable) throws IOException {
        this.authToken = TestAuthenticationHelper.theFollowingUserExists(dataTable, application);
    }

    @And("^I have logged in with email \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void iHaveLoggedInWithEmailAndPassword(String email, String password) throws IOException {
        this.authToken = TestAuthenticationHelper.login(email, password, this.application);
    }

    @Given("I populate the database with test data")
    public void iPopulateTheDatabaseWithTestData() throws IOException {
        Http.RequestBuilder resampleRequest = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        Result resampleResult = route(application, resampleRequest);

        Assert.assertEquals(200, resampleResult.status());

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users");
        Result result = route(application, request);
        ArrayNode array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertEquals(200, result.status());
        Assert.assertTrue(array.size() > 0);
    }

    @When("I request nationalities from the database")
    public void iRequestNationalitiesFromTheDatabase() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/nationalities");
        Result result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I request travellers from the {int} nationality id")
    public void iRequestTravellersFromTheNationalityId(Integer nationalityId) throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&nationality=" + nationalityId);
        Result result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I request travellers from the Male gender")
    public void iRequestTravellersFromTheMaleGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Male");
        Result result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I request travellers from the Female gender")
    public void iRequestTravellersFromTheFemaleGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Female");
        Result result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I request travellers from the Other gender")
    public void iRequestTravellersFromTheOtherGender() throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Other");
        Result result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I request travellers of the type {int}")
    public void iRequestTravellersOfTheType(Integer travellerTypeId) throws IOException {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", this.authToken)
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&travellerType=" + travellerTypeId.toString());
        Result result = route(application, request);
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

    @Then("I get the following [{string}] emails")
    public void iGetTheFollowing(String email) {
        Assert.assertEquals(email, this.array.get(0).get("email").asText());
    }
}
