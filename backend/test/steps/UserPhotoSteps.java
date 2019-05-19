package steps;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.PersonalPhoto;
import models.Role;
import models.User;
import org.junit.Assert;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.PlayResultToJson;
import utils.TestState;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserPhotoSteps {

    private Result result;
    private JsonNode photos;
    private String photoName = "";
    private boolean isPublic = false;
    private boolean isPrimary = false;
    private DataTable photoList;

    @Given("^the user has the following photos in the system:$")
    public void theUserHasTheFollowingPhotosInTheSystem(DataTable dataTable) {
        User testUser = TestState.getInstance().removeUser(0);
        this.photoList = dataTable;
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(testUser.getUserId());
        Assert.assertNotNull(user);
        List<PersonalPhoto> photos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), Boolean.valueOf(row.get("isPublic")), user, Boolean.valueOf(row.get("isPrimary")));
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

        this.photos = utils.PlayResultToJson.convertResultToJson(photosRes);

        Assert.assertNotNull(this.photos);
    }

    @Then("^the user gets a list of the following photos:$")
    public void theUserGetsAListOfTheFollowingPhotos(DataTable datatable) {
        List<Map<String, String>> list = datatable.asMaps(String.class, String.class);

        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            Assert.assertEquals(row.get("filename"), this.photos.asText());
            Assert.assertEquals(row.get("isPrimary"), this.photos.asText());
        }
    }

    @Given("^a user has a photo called \"([^\"]*)\"$")
    public void aUserHasAPhotoCalled(String photoName) {
        this.photoName = photoName;
    }

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

    @When("they add the photo")
    public void theyAddThePhoto() {
        // TODO: Exe to clean up this mess

//        User user = TestState.getInstance().getUser(0);
//        Application application = TestState.getInstance().getApplication();
//        File file = new File(System.getProperty("user.dir") + "/test/fileStorageForTests/photos/", photoName);
//        Assert.assertTrue(file.exists());
//
//        // determine content type for photo
//        String contentType = "";
//        if (file.getName().contains(".jpg") || file.getName().contains(".jpeg")) {
//            contentType = "image/jpeg";
//        } else if (file.getName().contains(".png")) {
//            contentType = "image/png";
//        }
//
//        Http.MultipartFormData.FilePart<Source<ByteString, ?>> part = new Http.MultipartFormData.FilePart<>("image", file.getName(), contentType, FileIO.fromFile(file));
//
//        // construct text fields
//        Map<String, String[]> textFields = new HashMap<>();
//        String[] isPrimaryArray = {Boolean.toString(isPrimary)};
//        String[] isPublicArray = {Boolean.toString(isPublic)};
//        textFields.put("isPrimary", isPrimaryArray);
//        textFields.put("isPublic", isPublicArray);
//
//        // construct file parts
//        List<Http.MultipartFormData.FilePart> fileParts = new ArrayList<>();
//        fileParts.add(part);
//
//        // TODO: add this kind of request to FakeClient interface
//
//        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/users/" + user.getUserId() + "/photos")
//                .method("POST")
//                .header("Authorization", user.getToken())
//                .bodyMultipart(textFields, fileParts);
//
//        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/" + user.getUserId() + "/photos")
//                .method("POST")
//                .header("Authorization", user.getToken())
//                .bodyRaw(
//                    Collections.singletonList(part),
//                    play.libs.Files.singletonTemporaryFileCreator(),
//                    application.asScala().materializer()
//                );
//        result = Helpers.route(application, request);
    }

    @Then("the photo is added")
    public void isItAdded() {
        //TODO: fix this
//        Assert.assertEquals(200, this.result.status());
    }

    @When("^the user requests all their photos$")
    public void theUserRequestsAllTheirPhotos() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        User user = TestState.getInstance().getUser(0);

        this.result = fakeClient.makeRequestWithToken("GET", "/api/users/" + user.getUserId() + "/photos", user.getToken());

        Assert.assertEquals(200, this.result.status());
    }

    @Then("^the user gets the same list$")
    public void theUserGetsTheSameList() throws IOException {
        JsonNode jsonNode = PlayResultToJson.convertResultToJson(this.result);
        Assert.assertTrue(jsonNode.isArray());
        Iterator<JsonNode> iterator = jsonNode.iterator();
        Iterator<Map<String, String>> rowsIterator = this.photoList.asMaps().iterator();
        ObjectMapper objectMapper = new ObjectMapper();

        while (iterator.hasNext()) {
            JsonNode nextPhoto = iterator.next();
            PersonalPhoto personalPhoto = objectMapper.treeToValue(nextPhoto, PersonalPhoto.class);
            Map<String, String> row = rowsIterator.next();
            Assert.assertEquals(row.get("filename"), personalPhoto.getFilenameHash());
            Assert.assertEquals(Boolean.valueOf(row.get("isPrimary")), personalPhoto.isPrimary());
            Assert.assertEquals(Boolean.valueOf(row.get("isPublic")), personalPhoto.isPublic());
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
}
