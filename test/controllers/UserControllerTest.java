package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.*;
import play.Application;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;

    @Before
    public void setUp() throws ServerErrorException, IOException, FailedToSignUpException {
        Map<String, String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);

        TestState.getInstance().setApplication(application);
        TestState.getInstance().setFakeClient(new FakePlayClient(application));

        // Make some users
        fakeClient = TestState.getInstance().getFakeClient();
        user = fakeClient.signUpUser("Timmy", "Tester", "timmy@tester.com",
                "abc123");
        user = User.find.byId(user.getUserId());
        otherUser = fakeClient.signUpUser("Tammy", "Tester", "tammy@tester.com",
                "abc123");
        adminUser = fakeClient.signUpUser("Andy", "Admin", "andy@admin.com",
                "abc123");

        // Add admin role for admin user
        Role role = new Role(RoleType.ADMIN);
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        role.save();
        adminUser = User.find.byId(adminUser.getUserId());
        adminUser.setRoles(roles);
        adminUser.save();

        // Add some photos

        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Map<String, String> values = new HashMap<>();
        values.put("isPrimary", Boolean.toString(false));
        values.put("isPublic", Boolean.toString(false));

        File file = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/",
                "cucumber.jpeg");

        if (!file.exists()) {
            Assert.fail(String.format("File %s was not found", file));
        }

        Result result = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + user.getUserId() + "/photos",
                user.getToken(),
                file,
                values);
        Assert.assertEquals(201, result.status());

        int newPhotoId = PlayResultToJson.convertResultToJson(result).get("photoId").asInt();

        PersonalPhoto photo = PersonalPhoto.find.byId(newPhotoId);
        Assert.assertNotNull(photo);
        user = User.find.byId(user.getUserId());
    }


    public List<User> createAdditionalUsers() throws ServerErrorException, IOException, FailedToSignUpException {
        List<User> users = new ArrayList<>();
        // Create extra users to test pagination works as expected
        for (int i = 0; i < 10; i++) {
            users.add(fakeClient.signUpUser("testing", "testing", "tester" + i + "@gmail.com", "abc123"));
        }

        return users;
    }

    @After
    public void tearDown() {
        Application application = TestState.getInstance().getApplication();
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void undoDeleteUserGood() {

        user.delete(); // Delete the user
        Optional<User> deletedUser = User.find.query().where().eq("user_id", user.getUserId()).findOneOrEmpty();
        Assert.assertFalse(deletedUser.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/undodelete",
                adminUser.getToken());
        Assert.assertEquals(200, result.status()); //Check the status code is correct.
        Optional<User> undoneUser = User.find.query().where().eq("user_id", user.getUserId()).findOneOrEmpty();
        Assert.assertTrue(undoneUser.isPresent());
    }

    @Test
    public void undoDeleteUserUnauthorized() {
        user.delete(); // Delete the user
        Optional<User> deletedUser = User.find.query().where().eq("user_id", user.getUserId()).findOneOrEmpty();
        Assert.assertFalse(deletedUser.isPresent());

        Result result = fakeClient.makeRequestWithNoToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/undodelete");
        Assert.assertEquals(401, result.status()); //Check the status code is correct.
    }

    @Test
    public void undoDeleteUserForbidden() {
        user.delete(); // Delete the user
        Optional<User> deletedUser = User.find.query().where().eq("user_id", user.getUserId()).findOneOrEmpty();
        Assert.assertFalse(deletedUser.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/undodelete",
                otherUser.getToken());
        Assert.assertEquals(403, result.status()); //Check the status code is correct.
    }

    @Test
    public void undoDeleteUserNotFound() {
        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + 1000000 + "/undodelete",
                adminUser.getToken());
        Assert.assertEquals(404, result.status()); //Check the status code is correct.
    }

    @Test
    public void undoDeleteUserBadRequest() {
        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/undodelete",
                adminUser.getToken());
        Assert.assertEquals(400, result.status()); //Check the status code is correct.
    }

    @Test
    public void iCanGetPaginatedItems() throws Exception {
        // Creates additional users for pagination testing
        List<User> additionalUsers = createAdditionalUsers();

        String endpoint = "/api/users/search?limit=" + 5 + "&offset=9";
        Result result = fakeClient.makeRequestWithToken("GET", endpoint, user.getToken());
        Assert.assertEquals(200, result.status());
        JsonNode userJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertEquals(4, userJson.size());
        Assert.assertEquals(additionalUsers.get(6).getUserId(), userJson.get(0).get("userId").asInt());
    }

    @Test
    public void iCanGetDefaultPaginatedItems() throws Exception {
        createAdditionalUsers();

        // By default, if limit or offset is not set, then limit = 20, offset = 0
        String endpoint = "/api/users/search";
        Result result = fakeClient.makeRequestWithToken("GET", endpoint, user.getToken());
        Assert.assertEquals(200, result.status());
        JsonNode userJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertEquals(13, userJson.size());

    }

    @Test
    public void iCanSearchForUsers() throws Exception {
        String endpoint = "/api/users/search?name=mmy";
        Result result = fakeClient.makeRequestWithToken("GET", endpoint, user.getToken());
        Assert.assertEquals(200, result.status());
        JsonNode userJson = PlayResultToJson.convertResultToJson(result);
        Assert.assertEquals(2, userJson.size());
        Assert.assertEquals(user.getUserId(), userJson.get(0).get("userId").asInt());
        Assert.assertEquals(otherUser.getUserId(), userJson.get(1).get("userId").asInt());
    }
}