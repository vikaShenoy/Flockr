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

        Destination destination1 = new Destination("Burning Man", destinationType1, district1, 12.1234, 12.1234, country1);
        Destination destination2 = new Destination("Brisbane City", destinationType2, district2, 11.1234, 11.1234, country2);

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

    @When("I click the Add Trip button")
    public void iClickTheAddATripButton() {
        User user = TestState.getInstance().getUser(0);
        Application application = TestState.getInstance().getApplication();
        ObjectNode jsonBody = Json.newObject();
        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/users/1/trips")
                .bodyJson(jsonBody)
                .header("Authorization", user.getToken());

        this.result = route(application, checkCreation);
        Assert.assertNotNull(this.result);
    }

    @When("I update a trip and click the Save Trip button")
    public void iUpdateATripAndClickTheSaveTripButton() {
        User user = TestState.getInstance().getUser(0);
        Application application = TestState.getInstance().getApplication();
        ObjectNode jsonBody = Json.newObject();

        jsonBody.set("tripName", Json.toJson(this.tripName));
        jsonBody.set("tripDestinations", Json.toJson(this.tripDestinations));
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("PUT")
                .uri("/api/users/1/trips/1")
                .bodyJson(jsonBody)
                .header("Authorization", user.getToken());

        this.result = route(application, checkCreation);
    }

    @When("^I send a request to get a trip with id (\\d+)$")
    public void iSendARequestToGetATrip(int tripId) {
        User user = TestState.getInstance().getUser(0);
        Application application = TestState.getInstance().getApplication();
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/users/1/trips/" + tripId)
                .header("Authorization", user.getToken());

        this.result = route(application, checkCreation);
    }

    @When("I send a request to get trips")
    public void iSendARequestToGetTrips() {
        User user = TestState.getInstance().getUser(0);
        Application application = TestState.getInstance().getApplication();
        Http.RequestBuilder checkCreation = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/users/1/trips")
                .header("authorization", user.getToken());

        this.result = route(application, checkCreation);
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
