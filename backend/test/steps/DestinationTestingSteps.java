package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
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
                Destination destination = fakeClient.makeTestDestination(Json.toJson(destinationList.get(i)), user.getToken());
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

    @Given("the database has been populated with the following countries, districts and destination types:")
    public void theDatabaseHasBeenPopulatedWithCountriesDistrictsAndDestinationTypes(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        for (Map<String, String> row : rows) {
            DestinationType destinationType = new DestinationType(row.get("destinationType"));
            Country country = new Country(row.get("country"));
            District district = new District(row.get("district"), country);
            destinationType.save();
            country.save();
            district.save();
            Assert.assertNotEquals(0, destinationType.getDestinationTypeId());
            Assert.assertNotEquals(0, country.getCountryId());
            Assert.assertNotEquals(0, district.getDistrictId());
        }
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

        this.destinationNode = Json.newObject();
        this.destinationNode.put("destinationName", firstRow.get("destinationName"));
        this.destinationNode.put("destinationTypeId", firstRow.get("destinationTypeId"));
        this.destinationNode.put("districtId", firstRow.get("districtId"));
        this.destinationNode.put("latitude", firstRow.get("latitude"));
        this.destinationNode.put("longitude", firstRow.get("longitude"));
        this.destinationNode.put("countryId", firstRow.get("countryId"));
        this.destinationNode.put("isPublic", firstRow.get("isPublic"));

        for (int i = 0; i < destinationList.size(); i++) {
            try {
                Destination destination = fakeClient.makeTestDestination(Json.toJson(destinationList.get(i)), user.getToken());
                TestState.getInstance().addDestination(destination);


                this.result = fakeClient.makeRequestWithToken("PUT", this.destinationNode, "/api/destinations/" + destination.getDestinationId(), user.getToken());

                Result getDestination = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + destination.getDestinationId(), user.getToken());
                JsonNode destinationData = utils.PlayResultToJson.convertResultToJson(getDestination);

                Assert.assertEquals(destinationData.get("destinationName").asText(), firstRow.get("destinationName"));
                Assert.assertEquals(destinationData.get("destinationType").get("destinationTypeId").asText(), firstRow.get("destinationTypeId"));
                Assert.assertEquals(destinationData.get("destinationDistrict").get("districtId").asText(), firstRow.get("districtId"));
                Assert.assertEquals(destinationData.get("destinationLat").asText(), firstRow.get("latitude"));
                Assert.assertEquals(destinationData.get("destinationLon").asText(), firstRow.get("longitude"));
                Assert.assertEquals(destinationData.get("destinationCountry").get("countryId").asText(), firstRow.get("countryId"));
                Assert.assertEquals(destinationData.get("isPublic").asText(), firstRow.get("isPublic"));

            } catch (UnauthorizedException unauthorisedExceptionE) {
                Assert.fail(Arrays.toString(unauthorisedExceptionE.getStackTrace()));
            } catch (ServerErrorException serverErrorE) {
                Assert.fail(Arrays.toString(serverErrorE.getStackTrace()));
            }
        }
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

}
