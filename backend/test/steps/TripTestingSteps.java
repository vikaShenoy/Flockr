package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import models.Country;
import models.Destination;
import models.DestinationType;
import models.District;
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

import utils.PlayResultToJson;
import utils.TestAuthenticationHelper;

import static play.test.Helpers.route;

public class TripTestingSteps {
   @Inject
    private Application application;
    private String tripName;
    private ArrayNode tripDestinations;
    private Result result;
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

    @Given("this user exists:")
    public void thisUserExists(DataTable dataTable) throws IOException {
        this.authToken = TestAuthenticationHelper.theFollowingUserExists(dataTable, this.application);
        Assert.assertNotNull(this.authToken);
    }

    @Given("^I log in with email \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void iLogInWithEmailAndPassword(String email, String password) throws IOException {

        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", email);
        reqJsonBody.put("password", password);

        Http.RequestBuilder loginRequest = Helpers.fakeRequest()
                .method("POST")
                .bodyJson(reqJsonBody)
                .uri("/api/auth/users/login");
        Result loginResult = route(application, loginRequest);
        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(loginResult);
        Assert.assertEquals(200, loginResult.status());
        this.authToken = authenticationResponseAsJson.get("token").asText();
        Assert.assertNotNull(this.authToken);
    }

    @Given("^I have a trip named \"([^\"]*)\"$")
    public void iHaveATripNamed(String tripName) {
        this.tripName = tripName;
    }

    @Given("Destinations have been added to the database")
    public void destinationsHaveBeenAddedToTheDatabase() {

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

        Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234, country1 );
        Destination destination2 = new Destination("Brisbane City",destinationType2, district2, 11.1234,11.1234, country2 );

        destination1.save();
        destination2.save();
    }

    @Given("I have trip destinations")
    public void iHaveTripDestinations(DataTable dataTable) {
        List<Map<String, String>> tripDestinations = dataTable.asMaps(String.class, String.class);
        ArrayNode tripDestinationsJson = Json.newArray();


        for (Map<String, String> tripDestinationJson : tripDestinations) {
            ObjectNode tripDestination = Json.newObject();
            tripDestination.set("destinationId", Json.toJson(Long.parseLong(tripDestinationJson.get("destinationId"))));
            tripDestination.set("arrivalDate", Json.toJson(Long.parseLong(tripDestinationJson.get("arrivalDate"))));
            tripDestination.set("arrivalTime", Json.toJson(Long.parseLong(tripDestinationJson.get("arrivalTime"))));
            tripDestination.set("departureDate", Json.toJson(Long.parseLong(tripDestinationJson.get("departureDate"))));
            tripDestination.set("departureTime", Json.toJson(Long.parseLong(tripDestinationJson.get("departureTime"))));

            tripDestinationsJson.add(tripDestination);
        }

        this.tripDestinations = tripDestinationsJson;

        System.out.println(tripDestinationsJson.asText());
    }

    @When("I send a request to add a trip")
    public void iSendARequestToAddATrip() {
        ObjectNode jsonBody = Json.newObject();
        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/users/1/trips")
                .bodyJson(jsonBody)
                .header("Authorization", this.authToken);

        this.result = route(application, checkCreation);
        Assert.assertNotNull(this.result);
    }

    @When("I send a request to update a trip")
    public void iSendARequestToUpdateATrip() {
        ObjectNode jsonBody = Json.newObject();

        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("PUT")
                .uri("/api/users/1/trips/1")
                .bodyJson(jsonBody)
                .header("Authorization",  this.authToken);

        this.result = route(application, checkCreation);
    }

    @When("^I send a request to get a trip with id (\\d+)$")
    public void iSendARequestToGetATrip(int tripId) {
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/users/1/trips/" + tripId)
                .header("Authorization",  this.authToken);

        this.result = route(application, checkCreation);
    }

    @When("I send a request to get trips")
    public void iSendARequestToGetTrips() {
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/users/1/trips")
                .header("authorization",  this.authToken);

        this.result = route(application, checkCreation);
    }

    @Then("^The server should return a (\\d+) status$")
    public void theServerShouldReturnAStatus(int status) {
        Assert.assertEquals(status, this.result.status());
    }
}
