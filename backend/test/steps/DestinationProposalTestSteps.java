package steps;

import com.fasterxml.jackson.databind.JsonNode;
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
import utils.TestState;

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
        System.out.println(endpoint);
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

}
