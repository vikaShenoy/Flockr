package steps;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.PersonalPhoto;
import models.User;
import org.junit.Assert;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.TestState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        User testUser = TestState.getInstance().getUser(0);
        this.photoList = dataTable;
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(testUser.getUserId());
        Assert.assertNotNull(user);
        List<PersonalPhoto> photos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), user, Boolean.valueOf(row.get("isPublic")), Boolean.valueOf(row.get("isPrimary")));
            photo.save();
            photos.add(photo);

            Assert.assertNotEquals(0, photo.getPhotoId());
        }
        user.setPersonalPhotos(photos);
        Assert.assertEquals(list.size(), user.getPersonalPhotos().size());
        user.update();
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
    public void theyAddThePhoto() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Application application = TestState.getInstance().getApplication();
        File file = new File(System.getProperty("user.dir") + "/test/fileStorageForTests/photos/" + photoName);
        Assert.assertTrue(file.exists());

        // determine content type for photo
        String contentType = "";
        if (file.getName().contains(".jpg") || file.getName().contains(".jpeg")) {
            contentType = "image/jpeg";
        } else if (file.getName().contains(".png")) {
            contentType = "image/png";
        }

        Http.MultipartFormData.FilePart<Source<ByteString, ?>> part = new Http.MultipartFormData.FilePart<>("image", file.getName(), contentType, FileIO.fromFile(file));

        // construct text fields
        Map<String, String[]> textFields = new HashMap<>();
        String[] isPrimaryArray = {Boolean.toString(isPrimary)};
        String[] isPublicArray = {Boolean.toString(isPublic)};
        textFields.put("isPrimary", isPrimaryArray);
        textFields.put("isPublic", isPublicArray);

        // construct file parts
        List<Http.MultipartFormData.FilePart> fileParts = new ArrayList<>();
        fileParts.add(part);

        // TODO: add this kind of request to FakeClient interface

        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/users/" + user.getUserId() + "/photos")
                .method("POST")
                .header("Authorization", user.getToken())
                .bodyMultipart(textFields, fileParts);

//        Http.RequestBuilder request = Helpers.fakeRequest().uri("/api/" + user.getUserId() + "/photos")
//                .method("POST")
//                .header("Authorization", user.getToken())
//                .bodyRaw(
//                    Collections.singletonList(part),
//                    play.libs.Files.singletonTemporaryFileCreator(),
//                    application.asScala().materializer()
//                );
        result = Helpers.route(application, request);
    }

    @Then("the photo is added")
    public void isItAdded() {
        Assert.assertEquals(200, result.status());
    }

    @When("^the user requests all their photos$")
    public void theUserRequestsAllTheirPhotos() {

    }

    @Then("^the user gets the same list$")
    public void theUserGetsTheSameList() {

    }
}
