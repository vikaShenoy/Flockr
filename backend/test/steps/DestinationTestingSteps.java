package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
import gherkin.deps.com.google.gson.JsonObject;
import io.cucumber.datatable.DataTable;
import models.*;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.TestAuthenticationHelper;
import utils.TestState;

import java.io.IOException;
import java.util.*;


import static play.test.Helpers.route;

public class DestinationTestingSteps {
    private JsonNode destinationData;
    private Result existingDestination;
    private ObjectNode destinationNode;
    private Result result;
    private JsonNode destinations;


    @Given("users with the following information exist:")
    public void usersWithTheFollowingInformationExists(DataTable dataTable) {
        Application application = TestState.getInstance().getApplication();
        TestAuthenticationHelper.theFollowingUsersExists(dataTable, application);
    }

    @Given("that I am logged in")
    public void thatIAmLoggedIn() {
        User user = TestState.getInstance().getUser(0);
        Assert.assertTrue(user.getToken().length() > 0);
    }

    @Given("that I have the following destinations:")
    public void thatIHaveTheFollowingDestinations(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        List<Map<String, String>> destinationList = dataTable.asMaps();
        for (int i = 0; i < destinationList.size(); i++) {
            try {
                ObjectNode currentDestination = (ObjectNode) Json.toJson(destinationList.get(i));
                Destination destination = fakeClient.makeTestDestination(currentDestination, user.getToken());

                destination.setDestinationOwner(user.getUserId());

                if (currentDestination.has("isPublic")) {
                    destination.setIsPublic(Boolean.parseBoolean(currentDestination.get("isPublic").asText()));
                }

                // Save to set permission as post endpoint doesn't do it
                destination.update();
                TestState.getInstance().addDestination(destination);

                Destination destination1 = Destination.find.byId(destination.getDestinationId());
                Assert.assertNotNull(destination1);
                Assert.assertTrue(destination1.getDestinationName().length() > 0);
                this.result = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + destination.getDestinationId(), user.getToken());
                existingDestination = this.result;
            } catch (UnauthorizedException | ServerErrorException e) {
                Assert.fail(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    @Given("that another user has the following destinations:")
    public void thatAnotherUserHasTheFollowingDestinations(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);
        List<Map<String, String>> destinationList = dataTable.asMaps();
        for (int i = 0; i < destinationList.size(); i++) {
            try {
                Destination destination = fakeClient.makeTestDestination(Json.toJson(destinationList.get(i)), user.getToken());
                TestState.getInstance().addDestination(destination);
                Assert.assertNotEquals(0, destination.getDestinationId());
            } catch (UnauthorizedException | ServerErrorException e) {
                Assert.fail(Arrays.toString(e.getStackTrace()));
            }
        }
    }


    @Given("that I want to create a Destination with the following valid data:")
    public void thatIWantToCreateADestinationWithTheFollowingValidData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.destinationData = Json.toJson(firstRow);
    }

    @Given("that I want to create a Destination with the following incomplete data:")
    public void thatIWantToCreateADestinationWithTheFollowingIncompleteData(DataTable dataTable) {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);

        this.destinationData = Json.toJson(firstRow);
    }

    @When("I click the Add Destination button")
    public void IClickTheAddDestination() {
        User user = TestState.getInstance().removeUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/destinations")
                .header("Authorization", user.getToken())
                .bodyJson(this.destinationData);
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        Assert.assertNotNull(this.result);
    }

    @When("I click the Delete Destination button")
    public void IClickTheDeleteDestinationButton() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().removeUser(0);
        Result result = fakeClient.makeRequestWithToken("DELETE", "/api/destinations/1", user.getToken());
        Assert.assertEquals(200, result.status());
    }

    @Then("The destination should be created successfully")
    public void iShouldReceiveAnStatusCodeIndicatingThatDestinationIsSuccessfullyCreated() {
        Assert.assertEquals(201, this.result.status());
    }

    @Then("I should receive an error, because the data is incomplete")
    public void iShouldReceiveAStatusCodeIndicatingThatTheDestinationIsNotSuccessfullyCreated() {
        Assert.assertEquals(400, this.result.status());
    }

    @Then("I should receive an error indicating that the Destination is not found")
    public void iShouldReceiveAStatusCodeWhenGettingTheDeletedDestinationWithId() {
        Optional<Destination> destination = Destination.find.query().where().eq("destination_id", 1).findOneOrEmpty();
        Assert.assertFalse(destination.isPresent());
    }

    @Then("I get a message saying that the destination already exists")
    public void i_get_a_message_saying_that_the_destination_already_exists() {
        Assert.assertEquals(409, result.status());

    }

    @When("the user adds {string} to the destination {string}")
    public void theUserAddsStringToTheDestinationString(String photoName, String destinationName) {
        User user = TestState.getInstance().getUser(0);
        List<Destination> destinations = TestState.getInstance().getDestinations();
        Destination destination = null;
        for (Destination currentDestination : destinations) {
            if (currentDestination.getDestinationName().equals(destinationName)) {
                destination = currentDestination;
            }
        }

        List<PersonalPhoto> personalPhotos = user.getPersonalPhotos();
        PersonalPhoto personalPhoto = null;
        System.out.println("personalPhotos size is: " + personalPhotos.size());
        for (PersonalPhoto currentPersonalPhoto : personalPhotos) {
            if (currentPersonalPhoto.getFilenameHash().equals(photoName)) {
                personalPhoto = currentPersonalPhoto;
            }
        }


        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        ObjectNode requestBody = Json.newObject();

        if (personalPhoto != null) {
            requestBody.put("photoId", personalPhoto.getPhotoId());
        }

        int destinationId = destination != null ? destination.getDestinationId() : 0;
        System.out.println("destinationId is: " + destinationId);
        result = fakeClient.makeRequestWithToken("POST", requestBody,"/api/destinations/" + destinationId + "/photos", user.getToken());
    }

    @Then("then the photo gets added to the destination")
    public void thenThePhotoGetAddedToTheDestination() {
        Assert.assertEquals(201, result.status());

        List<DestinationPhoto> destinationPhotos = DestinationPhoto.find.all();

        Assert.assertTrue(destinationPhotos.size() > 0);
    }

    @Then("the photo does not get added to the destination")
    public void thePhotoDoesNotGetAdded() {
        Assert.assertNotEquals(201, result.status());
        List<DestinationPhoto> destinationPhotos = DestinationPhoto.find.all();
        Assert.assertEquals(0, destinationPhotos.size());
    }



    @When("I update the Destination with the following information:")
    public void iUpdateTheDestinationWithTheFollowingInformation(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        List<Map<String, String>> destinationList = dataTable.asMaps();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        Double delta = 0.0001;

        this.destinationNode = Json.newObject();
        this.destinationNode.put("destinationName", firstRow.get("destinationName"));
        this.destinationNode.put("destinationTypeId", firstRow.get("destinationTypeId"));
        this.destinationNode.put("districtId", firstRow.get("districtId"));
        this.destinationNode.put("latitude", firstRow.get("latitude"));
        this.destinationNode.put("longitude", firstRow.get("longitude"));
        this.destinationNode.put("countryId", firstRow.get("countryId"));
        this.destinationNode.put("isPublic", firstRow.get("isPublic"));

        Destination destinationToChange = TestState.getInstance().getDestination(0);

        this.result = fakeClient.makeRequestWithToken("PUT", this.destinationNode,
                "/api/destinations/" + destinationToChange.getDestinationId(), user.getToken());

        Destination destination = Destination.find.byId(destinationToChange.getDestinationId());
        Assert.assertEquals(firstRow.get("destinationName"), destination.getDestinationName());
        Assert.assertEquals(firstRow.get("latitude"), destination.getDestinationLat().toString());
        Assert.assertEquals(firstRow.get("longitude"), destination.getDestinationLon().toString());
        Assert.assertEquals(Double.parseDouble(firstRow.get("districtId")),
                destination.getDestinationDistrict().getDistrictId(), delta);
        Assert.assertEquals(Double.parseDouble(firstRow.get("countryId")),
                destination.getDestinationCountry().getCountryId(), delta);
        Assert.assertEquals(Double.parseDouble(firstRow.get("destinationTypeId")),
                destination.getDestinationType().getDestinationTypeId(), delta);
        Assert.assertEquals(Boolean.parseBoolean(firstRow.get("isPublic")), destination.getIsPublic());
    }

    @Then("I should be allowed to update the Destination")
    public void iShouldBeAllowedToUpdateTheDestination() {
        Assert.assertEquals(200, this.result.status());
    }

    @Then("the Destination information is updated")
    public void theDestinationInformationIsUpdated() throws IOException {
        Assert.assertEquals(200, this.result.status());
        JsonNode originalDestination = utils.PlayResultToJson.convertResultToJson(existingDestination);
        System.out.println("it is: " + destinationNode);
        System.out.println("scond it" + originalDestination);
        Assert.assertEquals(destinationNode.get("destinationName").asText(), originalDestination.get("destinationName").asText());
        Assert.assertEquals(destinationNode.get("destinationTypeId").asText(), originalDestination.get("destinationType").get("destinationTypeId").asText());
        Assert.assertEquals(destinationNode.get("districtId").asText(), originalDestination.get("destinationDistrict").get("districtId").asText());
        Assert.assertNotEquals(destinationNode.get("latitude").asInt(), originalDestination.get("destinationLat").asInt());
        Assert.assertNotEquals(destinationNode.get("longitude").asInt(), originalDestination.get("destinationLon").asInt());
        Assert.assertEquals(destinationNode.get("countryId").asInt(), originalDestination.get("destinationCountry").get("countryId").asInt());
        Assert.assertNotEquals(destinationNode.get("isPublic").asBoolean(), originalDestination.get("isPublic").asBoolean());
    }

    @When("I try to update the Destination with the following information:")
    public void iTryToUpdateTheDestinationWithTheFollowingInformation(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        List<Map<String, String>> destinationList = dataTable.asMaps();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);

        this.destinationNode = Json.newObject();
        this.destinationNode.put("destinationName", firstRow.get("destinationName"));
        this.destinationNode.put("destinationTypeId", firstRow.get("destinationTypeId"));
        this.destinationNode.put("districtId", firstRow.get("districtId"));
        this.destinationNode.put("latitude", firstRow.get("latitude"));
        this.destinationNode.put("longitude", firstRow.get("longitude"));
        this.destinationNode.put("countryId", firstRow.get("countryId"));
        this.destinationNode.put("isPublic", firstRow.get("isPublic"));

        for (int i = 0; i < destinationList.size(); i++) {
            this.result = fakeClient.makeRequestWithToken("PUT", this.destinationNode, "/api/destinations/" + 10000, user.getToken());
        }
    }

    @Then("I get an error indicating that the Destination is not found")
    public void iGetAnErrorIndicatingThatTheDestinationIsNotFound() {

        Assert.assertEquals(404, this.result.status());
    }


    @When("the user gets their own destinations")
    public void theUserRetrievesTheirOwnPhotos() throws IOException {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Destination destination = TestState.getInstance().getDestination(0);

        Result destinationResult = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/destinations", user.getToken());
        destinations = utils.PlayResultToJson.convertResultToJson(destinationResult);
    }

    @When("another user gets the user's destinations")
    public void anotherUserGetsTheUsersDestinations() throws IOException {
        TestState testState = TestState.getInstance();
        User user = testState.getUser(0);
        User anotherUser = testState.getUser(1);
        FakeClient fakeClient = testState.getFakeClient();
        Result destinationResult = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/destinations", anotherUser.getToken());
        destinations = utils.PlayResultToJson.convertResultToJson(destinationResult);
    }


    @Then("{int} destinations should be returned")
    public void theListShouldBeTheSame(int numberOfDestinations) {
        Assert.assertEquals(numberOfDestinations, destinations.size());
    }



}
