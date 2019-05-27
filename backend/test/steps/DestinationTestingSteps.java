package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.ServerErrorException;
import exceptions.UnauthorizedException;
import io.cucumber.datatable.DataTable;
import io.ebean.Ebean;
import models.*;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.PlayResultToJson;
import utils.TestAuthenticationHelper;
import utils.TestState;

import java.io.IOException;
import java.util.*;

import static play.test.Helpers.route;

public class DestinationTestingSteps {

    private JsonNode destinationData;
    private Result result;
    private User user;
    private Destination destination;

    @Given("users with the following information exist:")
    public void usersWithTheFollowingInformationExists(DataTable dataTable) {
        Application application = TestState.getInstance().getApplication();
        TestAuthenticationHelper.theFollowingUsersExists(dataTable, application);
    }

    @Given("^that user (\\d+) logged in$")
    public void thatUserLoggedIn(int userIndex) throws Throwable {
        user = TestState.getInstance().getUser(userIndex);
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

                Result result = fakeClient.makeRequestWithNoToken("GET", "/api/destinations/" + destination.getDestinationId());

                // check that the destination's name has some text in it
                JsonNode res = utils.PlayResultToJson.convertResultToJson(result);
                String destinationName = res.get("destinationName").asText();
                Assert.assertTrue(destinationName.length() > 0);
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

    @Given("^that the user (\\d+) is a regular user$")
    public void thatTheUserIsARegularUser(int userIndex) throws Throwable {
        User user = TestState.getInstance().getUser(userIndex);
        Assert.assertFalse(user.isAdmin() || user.isDefaultAdmin());
    }

    @Given("^that the user (\\d+) is an admin$")
    public void thatTheUserIsAnAdmin(int userIndex) throws Throwable {
        User user = TestState.getInstance().getUser(userIndex);
        List<Role> roles = new ArrayList<>();
        roles.add(new Role(RoleType.ADMIN));
        user.setRoles(roles);
        Assert.assertTrue(user.isAdmin());
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
        List<Destination> destinations = TestState.getInstance().getDestinations();
        for (Destination currentDestination : destinations) {
           if (currentDestination.getDestinationName().equals(destinationName)) {
               destination = currentDestination;
           }
        }

        List<PersonalPhoto> personalPhotos = user.getPersonalPhotos();
        PersonalPhoto personalPhoto = null;
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

    @When("^the user gets all the photos for the destination$")
    public void theUserGetsAllThePhotosForTheDestination() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + destination.getDestinationId() + "/photos", user.getToken());
        Assert.assertEquals(200, result.status());
    }

    @Then("^the user can see all the public photos for the destination$")
    public void theUserCanSeeAllThePublicPhotosForTheDestination() throws Throwable {
        JsonNode resultAsJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertTrue(resultAsJson.isArray());
        ArrayNode destinationPhotos = (ArrayNode) resultAsJson;
        System.out.println(destinationPhotos);

        List<DestinationPhoto> publicDestinationPhotos = destination.getPublicDestinationPhotos();
        System.out.println(publicDestinationPhotos);

        int publicPhotosFound = 0;
        for (JsonNode destinationPhoto : destinationPhotos) {
            JsonNode personalPhoto = destinationPhoto.get("personalPhoto");
            if (personalPhoto.get("isPublic").asText().equals("true")) {
                publicPhotosFound += 1;
            }
        }
        Assert.assertEquals(1, publicPhotosFound);
    }

    @And("^the user can see only their private photos linked to the destination$")
    public void theUserCanSeeOnlyTheirPrivatePhotosLinkedToTheDestination() throws Throwable {
        JsonNode resultAsJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertTrue(resultAsJson.isArray());
        ArrayNode destinationPhotos = (ArrayNode) resultAsJson;

        int privatePhotosFound = 0;
        for (JsonNode node : destinationPhotos) {
            JsonNode personalPhoto = node.get("personalPhoto");
            if (personalPhoto.get("isPublic").asText().equals("false")) {
                privatePhotosFound += 1;
            }
        }
        Assert.assertEquals(1, privatePhotosFound);
    }

    @And("^the admin can see all the user's private photos linked to the destination$")
    public void theAdminCanSeeAllTheUserSPrivatePhotosLinkedToTheDestination() throws Throwable {
        List<DestinationPhoto> destinationPhotos = destination.getDestinationPhotos();

        // get number of private photos for venue in database
        int numberOfPrivatePhotosInDatabase = destinationPhotos.stream().mapToInt(destinationPhoto -> !destinationPhoto.getPersonalPhoto().isPublic() ? 1 : 0).sum();

        int numberOfPrivatePhotosInResponse = 0;

        JsonNode resultAsJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertTrue(resultAsJson.isArray());
        ArrayNode venuePhotos = (ArrayNode) resultAsJson;

        for (JsonNode venuePhoto : venuePhotos) {
            JsonNode personalPhoto = venuePhoto.get("personalPhoto");
            if (personalPhoto.get("isPublic").asText().equals("false")) {
                numberOfPrivatePhotosInResponse += 1;
            }
        }

        Assert.assertEquals(numberOfPrivatePhotosInDatabase, numberOfPrivatePhotosInResponse);
    }

    @When("^the user gets all the photos for a destination that does not exist$")
    public void theUserGetsAllThePhotosForADestinationThatDoesNotExist() throws Throwable {

        // find the maximum destination id
        List<Integer> destinationIds = Destination.find.query().where().findIds();
        int maxId = 0;
        for (int id : destinationIds) {
            maxId = id > maxId ? id : maxId;
        }

        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        result = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + (maxId + 1) + "/photos", user.getToken());
    }

    @Then("^they are told that the destination does not exist$")
    public void theyAreToldThatTheDestinationDoesNotExist() {
        Assert.assertEquals(404, result.status());
    }

    @Given("the photo {string} is linked to the destination {string}")
    public void thePhotoIsLinkedToTheDestination(String photoFilename, String destinationName) {
        this.theUserAddsStringToTheDestinationString(photoFilename, destinationName);
        Assert.assertEquals(201, this.result.status());
    }

    @Given("the destination {string} has public photos linked to it")
    public void theDestinationHasPublicPhotosLinkedToIt(String destinationName) {
        this.theUserAddsStringToTheDestinationString("monkey.png", destinationName);

        List<Destination> destinations = TestState.getInstance().getDestinations();
        destination = null;
        for (Destination currentDestination : destinations) {
            if (currentDestination.getDestinationName().equals(destinationName)) {
                destination = currentDestination;
            }
        }

        List<DestinationPhoto> publicDestinationPhotos = this.destination.getPublicDestinationPhotos();

        Assert.assertTrue(destination.getPublicDestinationPhotos().size() > 0);
    }
}
