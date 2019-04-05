package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import models.*;
import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
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
import utils.TestAuthenticationHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import static play.test.Helpers.route;

public class DestinationTestingSteps {
    @Inject
    private Application application;
    private JsonNode userData;
    private JsonNode destinationData;
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
        this.authToken = TestAuthenticationHelper.theFollowingUserExists(dataTable, application);
    }

    @Given("that I am logged in")
    public void thatIAmLoggedIn() {
        Assert.assertTrue(this.authToken.length() > 0);
    }

    @Given("^that I have a destination created with id (\\d+)$")
    public void thatIHaveADestinationCreatedWithId(int destinationId) throws IOException {

        DestinationType destinationType1 = DestinationType.find.query().
                where().eq("destination_type_id", 1).findOne();
        Country country1 = Country.find.query().
                where().eq("country_id", 1).findOne();
        District district1 = District.find.query().where().eq("district_id", 1).findOne();
        Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234, country1 );
        destination1.save();

        Http.RequestBuilder deleteRequest = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/" + destinationId);
        Result result = route(application, deleteRequest);

        // check that the destination's name has some text in it
        JsonNode res = utils.PlayResultToJson.convertResultToJson(result);
        String destinationName = res.get("destinationName").asText();
        Assert.assertTrue(destinationName.length() > 0);
    }

    @Given("that I want to create a Destination with the following valid data:")
    public void thatIWantToCreateADestinationWithTheFollowingValidData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userData = Json.toJson(firstRow);

        this.destinationData = Json.toJson(firstRow);
    }

    @Given("that I want to create a Destination with the following incomplete data:")
    public void thatIWantToCreateADestinationWithTheFollowingIncompleteData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        System.out.println(list.get(0));

        this.destinationData = Json.toJson(firstRow);
    }

    @Given("the database has been populated with countries, districts and destination types")
    public void theDatabaseHasBeenPopulatedWithCountriesDistrictsAndDestinationTypes() {

        DestinationType destinationType1 = new DestinationType("Event");
        DestinationType destinationType2 = new DestinationType("City");

        Country country1 = new Country("United States of America");
        Country country2 = new Country("Australia");

        District district1 = new District("Black Rock City", country1);
        District district2 = new District("New Farm", country2);

        destinationType1.save();
        destinationType2.save();

        country1.save();
        country2.save();

        district1.save();
        district2.save();

    }

    @When("^I make a \"([^\"]*)\" request to \"([^\"]*)\" to delete the destination$")
    public void iMakeARequestToToDeleteTheDestination(String requestMethod, String endpoint) {
        Http.RequestBuilder deleteReq = Helpers.fakeRequest()
                .method(requestMethod)
                .uri(endpoint)
                .header("Authorization", this.authToken);
        Result deleteRes = route(application, deleteReq);
        Assert.assertEquals(200, deleteRes.status());
    }

    @When("^I make a \"([^\"]*)\" request to \"([^\"]*)\" with the data$")
    public void iMakeARequestToWithTheData(String requestMethod, String endpoint) {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(requestMethod)
                .uri(endpoint)
                .bodyJson(this.destinationData);
        this.result = route(application, request);
        Assert.assertNotNull(this.result);
    }

    @Then("^I should receive an (\\d+) status code$")
    public void iShouldReceiveAnStatusCode(Integer expectedStatusCode) {
        Assert.assertEquals(expectedStatusCode, (Integer) this.result.status());
    }

    @Then("^I should receive a (\\d+) status code when getting the destination with id (\\d+)$")
    public void iShouldReceiveAStatusCodeWhenGettingTheDestinationWithId(Integer expectedStatusCode, Integer destinationId) {
        Http.RequestBuilder checkDeletion = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/destinations/" + destinationId.toString());
        Result getDestRes = route(application, checkDeletion);
        Assert.assertEquals(expectedStatusCode, (Integer) getDestRes.status());
    }
}
