package steps;

import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import controllers.routes;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.PersonalPhoto;
import models.User;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class UserPhotoSteps {

    @Inject
    private Application application;
    private String authToken;
    private int userId;
    private String email;
    private String plainTextPassword;
    private Result result;
    private JsonNode photos;
    private String photoName = "";
    private boolean isPublic = false;
    private boolean isPrimary = false;
    private DataTable photoList;

    @Before("@UserPhoto")
    public void setUpUserPhoto() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);

        Helpers.start(application);
    }

    @After("@UserPhoto")
    public void tearDownUserPhoto() {
        Helpers.stop(application);
    }

    @Given("^a user exists with the following information:$")
    public void AUserExistsWithTheFollowingInformation(DataTable dataTable) throws IOException {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.email = firstRow.get("email");
        this.plainTextPassword = firstRow.get("password");
        JsonNode signUpReqBody = Json.toJson(firstRow);

        // sign up a user
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(201, signUpRes.status());

        // log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.email);
        logInReqBody.put("password", this.plainTextPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(signUpReqBody);
        Result logInRes = route(application, logInReq);

        Assert.assertEquals(200, logInRes.status());

        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);

        // make the token available for the rest of the class
        this.authToken = logInResBody.get("token").asText();
        this.userId = logInResBody.get("userId").asInt();
        Assert.assertNotNull(this.authToken);
        Assert.assertNotNull(this.userId);
    }

    @Given("^the user has the following photos in the system:$")
    public void theUserHasTheFollowingPhotosInTheSystem(DataTable dataTable) {
        this.photoList = dataTable;
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(this.userId);
        Assert.assertNotNull(user);
        List<PersonalPhoto> photos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), Boolean.valueOf(row.get("isPublic")), user, Boolean.valueOf(row.get("isPrimary")));
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
        Http.RequestBuilder photosReq = Helpers.fakeRequest()
                .method("GET")
                .uri("/api/users/" + this.userId + "/photos")
                .header("authorization", this.authToken);
        Result photosRes = route(application, photosReq);

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
        File file = new File("app/photos/" + photoName);

        Http.MultipartFormData.Part<Source<ByteString, ?>> part = new Http.MultipartFormData.FilePart<>("picture", "file.pdf", "application/pdf", FileIO.fromPath(file.toPath()), Files.size(file.toPath()));

        Http.MultipartFormData.DataPart isPrimaryForm = new Http.MultipartFormData.DataPart("isPrimary", Boolean.toString(isPrimary));
        Http.MultipartFormData.DataPart isPublicForm = new Http.MultipartFormData.DataPart("isPublic", Boolean.toString(isPublic));

        Http.RequestBuilder request = Helpers.fakeRequest().uri(routes.PhotoController.addPhoto(userId).url())
                .method("POST")
                .header("Authorization", authToken)
                .bodyRaw(
                        Collections.singletonList(part),
                        play.libs.Files.singletonTemporaryFileCreator(),
                        application.asScala().materializer()
                );

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
