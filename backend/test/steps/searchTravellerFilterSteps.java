package steps;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.User;
import org.junit.Assert;
import org.junit.Test;
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
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class searchTravellerFilterSteps {

    private Result result;
    private ArrayNode array;

    @Given("I populate the database with test data")
    public void iPopulateTheDatabaseWithTestData() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder resampleRequest = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        Application application = TestState.getInstance().getApplication();
        Result resampleResult = route(application, resampleRequest);

        Assert.assertEquals(200, resampleResult.status());
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("Authorization", user.getToken())
                .uri("/api/users");
        this.result = route(application, request);
        ArrayNode array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertEquals(200, result.status());
        Assert.assertTrue(array.size() > 0);
    }

    @When("I want all types of nationalities from the database")
    public void iRequestNationalitiesFromTheDatabase() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/nationalities");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the {int} nationality id")
    public void iSearchTravellersWithTheNationalityId(Integer nationalityId) throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&nationality=" + nationalityId);
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Male")
    public void iSearchTravellersWithTheGenderMale() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Male");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Female")
    public void iSearchTravellersWithTheGenderFemale() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Female");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Other")
    public void iSearchTravellersWithTheGenderOther() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Other");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the traveller type ID {int}")
    public void iSearchTravellersWithTheTravellerTypeID(Integer travellerTypeId) throws IOException {
        User user = TestState.getInstance().getUser(0);

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&travellerType=" + travellerTypeId.toString());
        Application application = TestState.getInstance().getApplication();
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
            Assert.assertEquals(expectedResults.get(i).get("nationalityName"), this.array.get(i).get("nationalityName").asText());
        }
    }

    @Then("I get the following [{string}] emails")
    public void iGetTheFollowing(String email) {
        Assert.assertEquals(email, this.array.get(0).get("email").asText());
    }
}
