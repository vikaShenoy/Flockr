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
import io.cucumber.datatable.DataTableType;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.api.test.FakeRequest;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;

import javax.inject.Inject;

import static play.test.Helpers.route;

public class TripTestingSteps {
   @Inject
    private Application application;
    private String tripName;
    private ArrayNode tripDestinations;
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
    public void tearDown() { Helpers.stop(application); }

    @Given("I have resampled")
    public void iHaveResampled() {
        Http.RequestBuilder resample = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/internal/resample");
        Result result = route(application, resample);
    }

    @When("I have a trip named {string}")
    public void iHaveATripNamed(String tripName) {
        this.tripName = tripName;
    }

    @When("I have trip destinations")
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
    }

    @Then("I send a request to add a trip")
    public void iSendARequestToAddATrip() {
        ObjectNode jsonBody = Json.newObject();
        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/travellers/1/trips")
                .bodyJson(jsonBody)
                .header("Authorization",  "some-token");

        this.result = route(application, checkCreation);
    }

       @Then("I send a request to update a trip")
        public void iSendARequestToUpdateATrip() {
        ObjectNode jsonBody = Json.newObject();

        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("PUT")
                .uri("/api/travellers/1/trips/1")
                .bodyJson(jsonBody)
                .header("Authorization",  "some-token");

        this.result = route(application, checkCreation);
    }

    @When("I send a request to get a trip with id {int}")
    public void iSendARequestToGetATrip(int tripId) {
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/travellers/1/trips/" + tripId)
                .header("Authorization",  "some-token");

        this.result = route(application, checkCreation);
    }

    @When("I send a request to get trips")
    public void iSendARequestToGetTrips() {
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/travellers/1/trips")
                .header("Authorization",  "some-token");

        this.result = route(application, checkCreation);
    }

    @Then("The server should return a {int} status")
    public void theServerShouldReturnAstatus(int status) {
        Assert.assertEquals(status, this.result.status());
    }


}
