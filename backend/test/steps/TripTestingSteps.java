package steps;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.*;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.TestState;

import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class TripTestingSteps {

    private String tripName;
    private ArrayNode tripDestinations;
    private Result result;

    @Given("^I have a trip named \"([^\"]*)\"$")
    public void iHaveATripNamed(String tripName) {
        this.tripName = tripName;
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
    }

    @When("I click the Add Trip button")
    public void iClickTheAddATripButton() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        ObjectNode jsonBody = Json.newObject();
        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));

        this.result = fakeClient.makeRequestWithToken("POST", jsonBody, "/api/users/1/trips", user.getToken());
        Assert.assertNotNull(this.result);
    }

    @When("I update a trip and click the Save Trip button")
    public void iUpdateATripAndClickTheSaveTripButton() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        ObjectNode jsonBody = Json.newObject();

        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));

        this.result = fakeClient.makeRequestWithToken("PUT", jsonBody, "/api/users/1/trips/1", user.getToken());
    }

    @When("^I send a request to get a trip with id (\\d+)$")
    public void iSendARequestToGetATrip(int tripId) {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/1/trips/" + tripId, user.getToken());
    }

    @When("I send a request to get trips")
    public void iSendARequestToGetTrips() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/1/trips", user.getToken());
    }

    @Then("The server should return a {int} status indicating the Trip is successfully created")
    public void theServerShouldReturnAStatus(int status) {
        Assert.assertEquals(status, this.result.status());
    }

    @Then("The server should return a {int} status indicating the Trip is not successfully created")
    public void theServerIndicatesTripIsNotSuccessfullyCreated(int status) {
        Assert.assertEquals(status, this.result.status());
    }

    @Then("The server should return a {int} status indicating the Trip is successfully updated")
    public void theServerIndicatesTheTripIsSuccessfullyUpdated(int status) {
        Assert.assertEquals(status, this.result.status());
    }

    @Then("The server should return a {int} status indicating the Trip exists")
    public void theServerIndicatesTheTripExists(int status) {
        Assert.assertEquals(status, this.result.status());
    }

    @Then("The server should return a {int} status indicating the Trip is not found")
    public void theServerIndicatesTheTripIsNotFound(int status) {
        Assert.assertEquals(status, this.result.status());
    }

}
