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
    private ArrayList<JsonNode> searchResults;
    private PlayResultToJson result;

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

    @Given("the backend server is operating")
    public void theBackendServerIsOperating() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .uri("/");
        Result result = route(application, request);
        Assert.assertEquals(200, result.status());
    }

    @Given("the database has been populated with test data")
    public void theDatabaseHasBeenPopulatedWithTestData() throws IOException {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/travellers");
        Result result = route(application, request);

//        Assert.assertTrue(this.result.convertResultToJson(result).isArray());
    }

    @Given("I have logged in with email {string} and password {string}")
    public void iHaveLoggedInWithEmailAndPassword(String email, String password) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request nationalities from the database")
    public void iRequestNationalitiesFromTheDatabase() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get a list of all nationalities as follows:")
    public void iGetAListOfAllNationalitiesAsFollows(DataTable dataTable) {
        // Write code here that turns the phrase above into concrete actions
        // For automatic transformation, change DataTable to one of
        // E, List<E>, List<List<E>>, List<Map<K,V>>, Map<K,V> or
        // Map<K, List<V>>. E,K,V must be a String, Integer, Float,
        // Double, Byte, Short, Long, BigInteger or BigDecimal.
        //
        // For other transformations you can register a DataTableType.
    }

    @When("I request travellers from the {int}")
    public void iRequestTravellersFromThe(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
    }

    @Then("I get the following [{string}, {string}, {string}]")
    public void iGetTheFollowing(String string, String string2, String string3) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get the following [{string}, {string}]")
    public void iGetTheFollowing(String string, String string2) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers from the Not a Country")
    public void iRequestTravellersFromTheNotACountry() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get the following []")
    public void iGetTheFollowing() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers from the male")
    public void iRequestTravellersFromTheMale() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get the following [{string}, {string}, {string}, {string}, {string}]")
    public void iGetTheFollowing(String string, String string2, String string3, String string4, String string5) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers from the female")
    public void iRequestTravellersFromTheFemale() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get the following [{string}, {string}, {string}, {string}]")
    public void iGetTheFollowing(String string, String string2, String string3, String string4) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers from the other")
    public void iRequestTravellersFromTheOther() {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @Then("I get the following [{string}]")
    public void iGetTheFollowing(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers in the range {int} to {int}")
    public void iRequestTravellersInTheRangeTo(Integer int1, Integer int2) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }

    @When("I request travellers of the type {int}")
    public void iRequestTravellersOfTheType(Integer int1) {
        // Write code here that turns the phrase above into concrete actions
        throw new cucumber.api.PendingException();
    }
}
