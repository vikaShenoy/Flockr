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
import gherkin.deps.com.google.gson.JsonObject;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

import static play.test.Helpers.route;

public class DestinationTestingSteps {

    private JsonNode destinationData;
    private Result existingDestination;
    private ObjectNode destinationNode;
    private Result result;
    private JsonNode destinations;
    private int newPhotoId;
    private List<String> photosToRemove = new ArrayList<>();
    private JsonNode photoData;
    private User user;
    private Destination destination;
    private int destinationId;


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
                TestState.getInstance().addDestination(destination1);
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
                this.destinationId = destination.getDestinationId();
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

    @Then("I get a message saying that the destination already exists")
    public void i_get_a_message_saying_that_the_destination_already_exists() {
        Assert.assertEquals(409, result.status());

    }

    @When("the user adds {string} to the destination {string}")
    public void theUserAddsStringToTheDestinationString(String photoName, String destinationName) {
        List<Destination> destinations = TestState.getInstance().getDestinations();
        for (Destination currentDestination : destinations) {
            if (currentDestination.getDestinationName().equals(destinationName)) {
                this.destination = currentDestination;
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

        List<DestinationPhoto> publicDestinationPhotos = destination.getPublicDestinationPhotos();

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

        Destination destinationToChange = TestState.getInstance().getDestination(1);

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


    @Given("that another user have the following destinations:")
    public void thatAnotherUserHaveTheFollowingDestinations(DataTable dataTable) throws IOException {
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

                Destination dest = Destination.find.byId(destination.getDestinationId());
                Assert.assertNotNull(dest);

                this.result = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + destination.getDestinationId(), user.getToken());
                existingDestination = this.result;
            } catch (UnauthorizedException | ServerErrorException e) {
                Assert.fail(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    @When("I try to update another user's destination with the following information:")
    public void iTryToUpdateAnotherUsersDestinationWithTheInformation(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);

        List<Map<String, String>> destinationList = dataTable.asMaps();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.destinationData = utils.PlayResultToJson.convertResultToJson(existingDestination);

        this.destinationNode = Json.newObject();
        this.destinationNode.put("destinationName", firstRow.get("destinationName"));
        this.destinationNode.put("destinationTypeId", firstRow.get("destinationTypeId"));
        this.destinationNode.put("districtId", firstRow.get("districtId"));
        this.destinationNode.put("latitude", firstRow.get("latitude"));
        this.destinationNode.put("longitude", firstRow.get("longitude"));
        this.destinationNode.put("countryId", firstRow.get("countryId"));
        this.destinationNode.put("isPublic", firstRow.get("isPublic"));

        for (int i = 0; i < destinationList.size(); i++) {
            this.result = fakeClient.makeRequestWithToken("PUT", this.destinationNode, "/api/destinations/" + this.destinationData.get("destinationId").asInt(), user.getToken());
        }
    }

    @Then("I get an error indicating that I am not allowed to make changes on the destination")
    public void iGetAnErrorIndicatingThatIAmNotAllowedToMakeChangesOnTheDestination() {
        Assert.assertEquals(403, this.result.status());
    }

    @Given("that destination is linked to the user's destination photo")
    public void thatDestinationIsLinkedToTheUsersDestinationPhoto() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(true));

        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", "monkey.png");

        if (!file.exists()) {
            Assert.fail(String.format("File %s was not found", file));
        }

        this.result = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + user.getUserId() + "/photos",
                user.getToken(),
                file,
                values);

        Assert.assertNotNull(this.result);
        JsonNode resultAsJson = PlayResultToJson.convertResultToJson(this.result);
        this.newPhotoId = resultAsJson.get("photoId").asInt();
        this.photosToRemove.add(resultAsJson.get("filenameHash").asText());

        Assert.assertNotNull(this.newPhotoId);
        Assert.assertEquals(201, this.result.status());

        List<Destination> destinations = TestState.getInstance().getDestinations();

        Destination destination = null;
        for (Destination currentDestination : destinations) {
            if (currentDestination.getDestinationName().equals("The Dairy Down The Street")) {
                destination = currentDestination;
                this.destinationId = destination.getDestinationId();
            }
        }

        List<PersonalPhoto> personalPhotos = user.getPersonalPhotos();
        PersonalPhoto personalPhoto = null;
        for (PersonalPhoto currentPersonalPhoto : personalPhotos) {
            if (currentPersonalPhoto.getFilenameHash().equals("monkey.png")) {
                personalPhoto = currentPersonalPhoto;
            }
        }

        ObjectNode requestBody = Json.newObject();
        requestBody.put("photoId", this.newPhotoId);

        if (personalPhoto != null) {
            requestBody.put("photoId", personalPhoto.getPhotoId());
        }

        int destinationId = destination != null ? destination.getDestinationId() : 0;
        this.result = fakeClient.makeRequestWithToken("POST", requestBody,"/api/destinations/" + destinationId + "/photos", user.getToken());
        Assert.assertEquals(201, this.result.status());
    }

    @Then("the other user's private destination is deleted")
    public void theOtherUsersPrivateDestinationIsDeleted() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);
        Result destination = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + this.destinationId, user.getToken());
        Assert.assertEquals(404, destination.status());
    }

    @Then("the photo is changed to link to the public destination")
    public void thePhotoIsChangedToLinkToThePublicDestination() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);
        Destination destination = TestState.getInstance().getDestination(0);

        this.result = fakeClient.makeRequestWithToken("GET", "/api/destinations/" + destination.getDestinationId() + "/photos", user.getToken());
        this.photoData = utils.PlayResultToJson.convertResultToJson(this.result);


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
