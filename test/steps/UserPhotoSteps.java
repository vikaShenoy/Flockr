package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.PersonalPhoto;
import models.Role;
import models.RoleType;
import models.User;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Result;
import testingUtilities.FakeClient;
import testingUtilities.PlayResultToJson;
import testingUtilities.TestState;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test that user photo features work as expected.
 */
public class UserPhotoSteps {

    private Result result;
    private JsonNode photos;
    private String photoName = "";
    private boolean isPublic = false;
    private boolean isPrimary = false;
    private DataTable photoList;
    private int newPhotoId;
    private List<String> photosToRemove = new ArrayList<>();
    private int nonExistentPhotoId;
    final Logger log = LoggerFactory.getLogger(this.getClass());
    private User aliceAdmin;
    private User bobAdmin;
    private User tylerRegular;
    private User karenRegular;

    @After("@UserPhotos")
    public void tearDown() {
        if (this.photosToRemove.size() > 0) {
            log.info("Now deleting test photo files...");
            boolean deleted = false;
            for (String filename : this.photosToRemove) {
                int indexOfPoint = filename.lastIndexOf('.');
                String fileType = filename.substring(indexOfPoint);
                String filenameBody = filename.substring(0, indexOfPoint);
                String thumbFilename = filenameBody + "_thumb" + fileType;
                File file = new File(System.getProperty("user.dir") + "/storage/photos/", filename);
                File thumbFile = new File(System.getProperty("user.dir") + "/storage/photos/", thumbFilename);

                log.info("Going to delete photo and its thumbnail in clean up step");
                deleted = file.delete() && thumbFile.delete();
            }
            if (deleted) {
                log.info("Deletion of test images successful");
            } else {
                log.warn("Deletion of test images failed, maybe the images were deleted in endpoint calls ¯\\_(ツ)_/¯");
            }
        }
    }

    @Given("^the user has the following photos in the system:$")
    public void theUserHasTheFollowingPhotosInTheSystem(DataTable dataTable) {
        User testUser = TestState.getInstance().getUser(0);
        this.photoList = dataTable;
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(testUser.getUserId());
        Assert.assertNotNull(user);
        List<PersonalPhoto> photos = new ArrayList<>();
        for (Map<String, String> row : list) {
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), Boolean.valueOf(row.get("isPublic")), user, Boolean.valueOf(row.get("isPrimary")), null, false);
            photo.save();
            photo = PersonalPhoto.find.byId(photo.getPhotoId());
            photos.add(photo);
        }
        user.setPersonalPhotos(photos);
        user.save();
        user = User.find.byId(testUser.getUserId());

        Assert.assertNotNull(user);
        Assert.assertEquals(list.size(), user.getPersonalPhotos().size());
    }

    @Given("^the admin user has the following photos in the system:$")
    public void theAdminUserHasTheFollowingPhotosInTheSystem(DataTable dataTable) {
        User testUser = TestState.getInstance().getUser(1);
        this.photoList = dataTable;
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(testUser.getUserId());
        Assert.assertNotNull(user);
        List<PersonalPhoto> photos = new ArrayList<>();
        for (Map<String, String> row : list) {
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), Boolean.valueOf(row.get("isPublic")), user, Boolean.valueOf(row.get("isPrimary")), null, false);
            photo.save();
            photo = PersonalPhoto.find.byId(photo.getPhotoId());
            photos.add(photo);
        }
        user.setPersonalPhotos(photos);
        user.save();
        user = User.find.byId(testUser.getUserId());

        Assert.assertNotNull(user);
        Assert.assertEquals(list.size(), user.getPersonalPhotos().size());
        TestState.getInstance().addUser(user);
    }

    @When("^the user tries to retrieve their photos$")
    public void theUserTriesToRetrieveTheirPhotos() throws IOException {
        User testUser = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result photosRes = fakeClient.makeRequestWithToken("GET", "/api/users/" +
                testUser.getUserId() + "/photos", testUser.getToken());

        Assert.assertEquals(200, photosRes.status());

        this.photos = testingUtilities.PlayResultToJson.convertResultToJson(photosRes);

        Assert.assertNotNull(this.photos);
    }

    @Then("^the user gets a list of the following photos:$")
    public void theUserGetsAListOfTheFollowingPhotos(DataTable datatable) {
        List<Map<String, String>> list = datatable.asMaps(String.class, String.class);

        for (Map<String, String> row : list) {
            Assert.assertEquals(row.get("filename"), this.photos.asText());
            Assert.assertEquals(row.get("isPrimary"), this.photos.asText());
        }
    }

    @Given("^a user has a photo called \"([^\"]*)\"$")
    public void aUserHasAPhotoCalled(String photoName) {
        this.photoName = photoName;
    }

    @Given("^the admin has a photo called \"([^\"]*)\"$")
    public void theAdminHasAPhotoCalled(String photoName) { this.photoName = photoName; }

    @Given("they want the photo to be public")
    public void theyWantThePhotoToBePublic() {
        isPublic = true;
    }

    @Given("they want the photo to be private")
    public void theyWantThePhotoToBePrivate() {
        isPublic = false;
    }

    @Given("they want the photo to be their profile photo")
    public void theyWantThePhotoToBeTheirProfilePhoto() {
        isPrimary = true;
    }


    @Given("^two admin users exist$")
    public void thatTwoAdminUsersExist() throws Throwable {
        List<Role> adminRole = Role.find.query().where().eq("role_type", RoleType.ADMIN.toString()).findList();

        this.aliceAdmin = TestState.getInstance().getUser(2);
        this.bobAdmin = TestState.getInstance().getUser(3);
        this.aliceAdmin.setRoles(adminRole);
        this.bobAdmin.setRoles(adminRole);
        this.aliceAdmin.save();
        this.bobAdmin.save();
    }

    @When("^the first admin adds the photo to the second admin$")
    public void theFirstAdminAddsThePhotoToTheSecondAdmin() throws Throwable {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(true));
        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", photoName);

        if (!file.exists()) {
            Assert.fail(String.format("File %s was not found", file));
        }

        this.result = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + this.bobAdmin.getUserId() + "/photos",
                this.aliceAdmin.getToken(),
                file,
                values);

        Assert.assertNotNull(this.result);
        JsonNode resultAsJson = PlayResultToJson.convertResultToJson(this.result);
        Assert.assertNotNull(resultAsJson.get("filenameHash").asText());
        this.photosToRemove.add(resultAsJson.get("filenameHash").asText());
    }

    @Then("^the photo is not added$")
    public void thePhotoIsNotAdded() throws Throwable {
        int statusCode = this.result.status();
        Assert.assertTrue(statusCode >= 400 && statusCode < 500);
    }

    @Given("^a regular user with first name Tyler exists$")
    public void aRegularUserWithFirstNameTylerExists() throws Throwable {
        this.tylerRegular = TestState.getInstance().getUser(4);
        Assert.assertEquals("Tyler", this.tylerRegular.getFirstName());
    }

    @And("^a regular user with first name Karen exists$")
    public void aRegularUserWithFirstNameKarenExists() throws Throwable {
        this.karenRegular = TestState.getInstance().getUser(5);
        Assert.assertEquals("Karen", this.karenRegular.getFirstName());
    }

    @When("^a regular user Tyler tries to upload a photo for a regular user Karen$")
    public void aRegularUserTylerTriesToUploadAPhotoForARegularUserKaren() throws Throwable {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(true));
        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", "dog.jpg");

        if (!file.exists()) {
            Assert.fail(String.format("File %s was not found", file));
        }

        this.result = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + this.karenRegular.getUserId() + "/photos",
                this.tylerRegular.getToken(),
                file,
                values);

        if (this.result.status() == 200) {
            Assert.assertNotNull(this.result);
            JsonNode resultAsJson = PlayResultToJson.convertResultToJson(this.result);
            Assert.assertNotNull(resultAsJson.get("filenameHash").asText());
            this.photosToRemove.add(resultAsJson.get("filenameHash").asText());
        }

    }

    @When("they add the photo")
    public void theyAddThePhoto() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(this.isPrimary));
        values.put("isPublic", Boolean.toString(this.isPublic));

        User user = TestState.getInstance().getUser(0);
        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", photoName);

        if (!file.exists()) {
            Assert.fail(String.format("File %s was not found", file));
        }

        this.result = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + user.getUserId() + "/photos",
                user.getToken(),
                file,
                values);

        if (this.result.status() == 200) {
            JsonNode resultAsJson = PlayResultToJson.convertResultToJson(this.result);
            Assert.assertNotNull(resultAsJson.get("filenameHash").asText());
            this.photosToRemove.add(resultAsJson.get("filenameHash").asText());
        }
    }

    @Then("the photo is added")
    public void isItAdded() {
        Assert.assertEquals(201, this.result.status());
    }

    @When("^the user requests all their photos$")
    public void theUserRequestsAllTheirPhotos() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);

        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/photos", user.getToken());

        Assert.assertEquals(200, this.result.status());
    }

    @Then("^the list does not contain the primary photo$")
    public void theUserGetsTheSameList() throws IOException {
        JsonNode jsonNode = PlayResultToJson.convertResultToJson(this.result);
        Assert.assertTrue(jsonNode.isArray());
        Iterator<JsonNode> iterator = jsonNode.iterator();

        for (JsonNode personalPhoto : jsonNode) {
            Assert.assertFalse(personalPhoto.get("isPrimary").asBoolean());
        }
    }

    @When("^a user requests photos for a non signed up userId$")
    public void aUserRequestsPhotosForANonSignedUpUserId() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/500/photos", user.getToken());
        Assert.assertNotNull(this.result);
    }

    @When("^a non signed in user requests photos for another user$")
    public void aUserRequestsPhotosForANonSignedInUserId() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken("GET", "/api/users/1/photos");
        Assert.assertNotNull(this.result);
    }

    @Then("^they should receive a \"([^\"]*)\" error message with a (\\d+) error code$")
    public void theyShouldReceiveAErrorMessageWithAErrorCode(String errorMessage, int errorCode) {
        Assert.assertEquals(errorCode, this.result.status());
    }

    @Given("^one of the users is an admin$")
    public void oneOfTheUsersIsAnAdmin() {
        User admin = TestState.getInstance().getUser(1);
        List<Role> roles = Role.find.query().where().eq("role_type", "ADMIN").findList();
        Assert.assertEquals(1, roles.size());
        admin.setRoles(roles);
        admin.save();
    }

    @When("^the admin user requests the photos of another user$")
    public void theAdminUserRequestsThePhotosOfAnotherUser() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        User admin = TestState.getInstance().getUser(1);

        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/photos", admin.getToken());
        Assert.assertNotNull(this.result);
    }

    @Given("^a user has a photo called \"([^\"]*)\" already$")
    public void aUserHasAPhotoCalledAlready(String filename) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(true));

        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", filename);

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

        Assert.assertNotEquals(0, this.newPhotoId);
        Assert.assertEquals(201, this.result.status());
    }

    @Given("a user has a private photo called {string} already")
    public void aUserHasAPrivatePhotoCalledAlready(String filename) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(false));

        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/", filename);

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

        Assert.assertNotEquals(0, this.newPhotoId);
        Assert.assertEquals(201, this.result.status());
    }

    @When("^the user requests the thumbnail for this photo$")
    public void theUserRequestsTheThumbnailForThisPhoto() {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/" + this.newPhotoId + "?Authorization=" + user.getToken());

        Assert.assertNotNull(this.result);
        Assert.assertEquals(200, this.result.status());
    }

    @Then("^the thumbnail is returned in the response$")
    public void theThumbnailIsReturnedInTheResponse() {
        Assert.assertFalse(this.result.body().isKnownEmpty());
        Assert.assertTrue(this.result.body().contentLength().get().intValue() > 2000);
    }

    @When("^the user requests the thumbnail for a non existent photo$")
    public void theUserRequestsTheThumbnailForANonExistentPhoto() {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/5000?Authorization=" + user.getToken());

        Assert.assertNotNull(this.result);
    }

    @When("^the user requests the thumbnail for a photo without their token$")
    public void theUserRequestsTheThumbnailForAPhotoWithoutTheirToken() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/5000");

        Assert.assertNotNull(this.result);
    }

    @When("the user changes the photo permission to private")
    public void theUserChangesThePhotoPermissionToPrivate() throws IOException {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        ObjectNode reqBody = Json.newObject();
        reqBody.put("isPublic", "false");
        reqBody.put("isPrimary", "false");

        this.result = fakeClient.makeRequestWithToken("PATCH", reqBody, "/api/users/photos/"
                + this.newPhotoId, user.getToken());
        Assert.assertEquals(200, this.result.status());

        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/photos", user.getToken());
        this.photos = testingUtilities.PlayResultToJson.convertResultToJson(this.result);
    }

    @Then("the photo permission is set to private")
    public void thePhotoPermissionIsSetToPrivate() {
        for (JsonNode photo : this.photos) {
            int id = photo.get("photoId").asInt();
            if (this.newPhotoId == id) {
                boolean isPublic = photo.get("public").asBoolean();
                Assert.assertFalse(isPublic);
                boolean isPrimary = photo.get("primary").asBoolean();
                Assert.assertFalse(isPrimary);
            }
        }
    }

    @When("the admin changes the photo permission to private")
    public void theAdminChangesThePhotoPermissionToPrivate() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        User admin = TestState.getInstance().getUser(1);

        ObjectNode reqBody = Json.newObject();
        reqBody.put("isPublic", "false");
        reqBody.put("isPrimary", "false");

        this.result = fakeClient.makeRequestWithToken("PATCH",  reqBody, "/api/users/photos/"
                + this.newPhotoId, admin.getToken());
        Assert.assertNotNull(this.result);

        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/photos", user.getToken());
        this.photos = testingUtilities.PlayResultToJson.convertResultToJson(this.result);
    }

    @When("another user changes the photo permission to private")
    public void anotherUserChangesThePhotoPermissionToPrivate() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User anotherUser = TestState.getInstance().getUser(1);

        ObjectNode reqBody = Json.newObject();
        reqBody.put("isPublic", "false");
        reqBody.put("isPrimary", "false");

        this.result = fakeClient.makeRequestWithToken("PATCH",  reqBody, "/api/users/photos/"
                + this.newPhotoId, anotherUser.getToken());
    }

    @Then("that user is not allowed to change the photo permission")
    public void thatUserIsNotAllowedToChangeThePhotoPermission() {
        Assert.assertEquals(this.result.status(), 403);
    }

    @Given("a user has no photo with the photo id {int}")
    public void aUserHasNoPhotoWithThePhotoId(Integer photoId) {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.nonExistentPhotoId = photoId;
        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/photos/" + photoId, user.getToken());
        Assert.assertEquals(404, this.result.status());
    }

    @When("the user changes the photo permission of the non-existent photo to private")
    public void theUserChangesThePhotoPermissionOfTheNonExistentPhotoToPrivate() {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        ObjectNode reqBody = Json.newObject();
        reqBody.put("isPublic", "false");
        reqBody.put("isPrimary", "false");

        this.result = fakeClient.makeRequestWithToken("PATCH", reqBody, "/api/users/photos/"
                + this.nonExistentPhotoId, user.getToken());
    }

    @Then("the photo cannot be found")
    public void thePhotoCannotBeFound() {
        Assert.assertEquals(404, this.result.status());
    }

    @When("the user requests that the photo be deleted")
    public void theUserRequestsThatThePhotoBeDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        this.result = fakeClient.makeRequestWithToken("DELETE", "/api/users/photos/" + this.newPhotoId, user.getToken());
        Assert.assertEquals(200, result.status());
    }

    @Then("the photo is deleted")
    public void thePhotoIsDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);
        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/photos/" + this.newPhotoId, user.getToken());
        Assert.assertEquals(404, this.result.status());
    }

    //Start of GET single photo testing



    @When("the logged out user requests the photo")
    public void theLoggedOutUserRequestsThePhoto() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken("GET", "/api/users/photos/" + this.newPhotoId);
        Assert.assertNotNull(this.result);
    }

    @When("the logged in user requests the photo")
    public void theLoggedInUserRequestsThePhoto() {
        // Write code here that turns the phrase above into concrete actions
        User user = TestState.getInstance().getUser(1);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/" + this.newPhotoId + "?Authorization=" + user.getToken());

        Assert.assertNotNull(this.result);
    }

    @When("the user requests the photo")
    public void theUserRequestsThePhoto() {

        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/" + this.newPhotoId + "?Authorization=" + user.getToken());

        Assert.assertNotNull(this.result);

    }

    @Then("the photo is returned in the response body with a status of {int}")
    public void thePhotoIsReturnedInTheResponseBodyWithAStatusOf(Integer stat) {
        Assert.assertFalse(this.result.body().isKnownEmpty());
        Assert.assertTrue(this.result.body().contentLength().get().intValue() > 2000);
        Assert.assertTrue(this.result.status() == 200);
    }

    @When("no user has a photo with id {int}")
    public void noUserHasAPhotoWithId(Integer id) {
        User user = TestState.getInstance().getUser(0);
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken(
                "GET",
                "/api/users/photos/" + id + "?Authorization=" + user.getToken());

        Assert.assertNotNull(this.result);
    }

    @When("the admin user requests the photo")
    public void theAdminUserRequestsThePhoto() {

        User admin = TestState.getInstance().getUser(1);
        List<Role> roles = Role.find.query().where().eq("role_type", "ADMIN").findList();
        Assert.assertEquals(1, roles.size());
        admin.setRoles(roles);
        admin.save();

        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        this.result = fakeClient.makeRequestWithNoToken("GET", "/api/users/photos/" + this.newPhotoId + "?Authorization=" + admin.getToken());
        Assert.assertNotNull(this.result);

    }

    // End of GET single photo testing


    @When("another user requests that the photo be deleted")
    public void anotherUserRequestsThatThePhotoBeDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(1);
        this.result = fakeClient.makeRequestWithToken("DELETE", "/api/users/photos/" + this.newPhotoId, user.getToken());
    }
}
