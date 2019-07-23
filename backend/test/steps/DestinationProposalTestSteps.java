package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.Destination;
import models.DestinationProposal;
import models.TravellerType;
import models.User;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Result;
import utils.FakeClient;
import utils.PlayResultToJson;
import utils.TestState;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DestinationProposalTestSteps {
    List<Integer> travellerTypeIds;
    Result result;

    @When("I propose the following traveller types")
    public void iProposeTheFollowingTravellerTypes(DataTable dataTable) {
        User testUser = TestState.getInstance().getUser(0);
        Destination destination = TestState.getInstance().getDestination(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<Map<String, String>> travellerTypesMap = dataTable.asMaps();
        travellerTypeIds = travellerTypesMap
                                            .stream()
                                            .map(travellerTypeMap -> Integer.parseInt(travellerTypeMap.get("travellerTypeId")))
                                            .collect(Collectors.toList());
        ObjectNode body = Json.newObject();
        body.set("travellerTypeIds", Json.toJson(travellerTypeIds));
        String endpoint = "/api/destinations/" + destination.getDestinationId() + "/proposals";
        result = fakeClient.makeRequestWithToken("POST", body, endpoint, testUser.getToken());
    }

    @Then("the proposal should be made")
    public void theProposalShouldBeMade() {
       Assert.assertEquals(200, result.status());
       Destination destination = TestState.getInstance().getDestination(0);
       DestinationProposal destinationProposal = DestinationProposal.find.query().where().eq("destination_destination_id", destination.getDestinationId()).findOne();
       List<TravellerType> travellerTypes = destinationProposal.getTravellerTypes();
       for (TravellerType travellerType : travellerTypes) {
           Assert.assertTrue(travellerTypeIds.contains(travellerType.getTravellerTypeId()));
       }
    }

    @Then("the proposal should not be made")
    public void theProposalShouldNotBeMade() {
        Assert.assertNotEquals(200, result.status());
    }

    @When("an admin accepts the proposal")
    public void anAdminAcceptsTheProposal() {
        User adminUser = TestState.getInstance().getUser(1);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("PATCH", "/api/destinations/proposals/1", adminUser.getToken());
    }

    @When("a user tries to accept the proposal")
    public void aUserTriesToAcceptTheProposal() {
        User adminUser = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("PATCH", "/api/destinations/proposals/1", adminUser.getToken());
    }

    @Then("the proposal is accepted")
    public void theProposalIsAccepted() {
        Assert.assertEquals(200, result.status());
    }

    @Then("the proposal is not accepted")
    public void theProposalIsNotAccepted() {
        Assert.assertNotEquals(200, result.status());
    }

    @When("an admin rejects the proposal")
    public void anAdminRejectsTheProposal() {
        User adminUser = TestState.getInstance().getUser(1);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("PATCH", "/api/destinations/proposals/1", adminUser.getToken());
    }

    @Then("the proposal is rejected")
    public void theProposalIsRejected() {
        Assert.assertEquals(200, result.status());

        // Make sure that proposal has been deleted
        int amountOfProposals = DestinationProposal.find.all().size();
        Assert.assertEquals(0, amountOfProposals);
    }

    @When("a user tries to reject the proposal")
    public void aUserTriesToRejectTheProposal() {
        User testUser = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("PATCH", "/api/destinations/proposals/1", testUser.getToken());
    }

    @When("an admin tries to reject a proposal that does not exist")
    public void aUserTriesToRejectAProposalThatDoesNotExist() {
        User adminUser = TestState.getInstance().getUser(1);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("PATCH", "/api/destinations/proposals/3", adminUser.getToken());
    }

    @Then("the proposal is not rejected")
    public void theProposalIsNotRejected() {
        Assert.assertNotEquals(200, result.status());

        // Make sure that the proposal is still there
        int amountOfProposals = DestinationProposal.find.all().size();
        Assert.assertEquals(1, amountOfProposals);
    }

    @When("an admin requests all proposals")
    public void anAdminRequestsAllProposals() {
       User testAdmin = TestState.getInstance().getUser(1);
       FakeClient fakeClient = TestState.getInstance().getFakeClient();
       result = fakeClient.makeRequestWithToken("GET", "/api/destinations/proposals", testAdmin.getToken());
    }

    @Then("the correct data will be returned")
    public void theCorrectDataWillBeReturned() throws IOException {
        Assert.assertEquals(200, result.status());
        JsonNode proposals = PlayResultToJson.convertResultToJson(result);
        JsonNode myProposalJson = proposals.get(0);
        ObjectMapper objectMapper = new ObjectMapper();
        DestinationProposal myProposal = objectMapper.treeToValue(myProposalJson, DestinationProposal.class);
        List<Integer> travellerTypeIds = myProposal.getTravellerTypes().stream().map(TravellerType::getTravellerTypeId).collect(Collectors.toList());
        myProposal.getTravellerTypes().equals(travellerTypeIds);
    }


}
