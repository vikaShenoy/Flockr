package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
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

import java.io.IOException;
import java.util.ArrayList;
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
    private JsonNode photos;

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
                .uri("/api/auth/travellers/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(200, signUpRes.status());

        // log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.email);
        logInReqBody.put("password", this.plainTextPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/travellers/login")
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
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);

        User user = User.find.byId(this.userId);
        ArrayList<PersonalPhoto> photos = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            PersonalPhoto photo = new PersonalPhoto(row.get("filename"), Boolean.valueOf(row.get("isPrimary")));
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
                .uri("/api/travellers/" + this.userId + "/photos")
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
}
