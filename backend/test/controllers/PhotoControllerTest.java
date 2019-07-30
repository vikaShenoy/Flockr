package controllers;

import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class PhotoControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;
    PersonalPhoto photo;
    DestinationPhoto destPhoto;
    Destination destination;

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

        // Make some users
        fakeClient = new FakePlayClient(application);
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

        PersonalPhoto personalPhoto = PersonalPhoto.find.byId(newPhotoId);
        Assert.assertNotNull(personalPhoto);
        user = User.find.byId(user.getUserId());

        File file2 = new File(System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/",
                "cat.jpeg");

        if (!file2.exists()) {
            Assert.fail(String.format("File %s was not found", file2));
        }

        Result result2 = fakeClient.makeMultipartFormRequestWithFileAndToken(
                "POST",
                "/api/users/" + user.getUserId() + "/photos",
                user.getToken(),
                file2,
                values);
        Assert.assertEquals(201, result2.status());

        int destPhotoId = PlayResultToJson.convertResultToJson(result2).get("photoId").asInt();

        photo = PersonalPhoto.find.byId(destPhotoId);
        Assert.assertNotNull(photo);

        // Add some destinations
        DestinationType destinationType = new DestinationType("city");
        Country country = new Country("Peru", "PE", true);
        District district = new District("Test District", country);
        destination = new Destination("Test City", destinationType, district,
                0.0, 0.0, country, user.getUserId(), new ArrayList<>(), true);

        destinationType.save();
        country.save();
        district.save();
        destination.save();

        destPhoto = new DestinationPhoto(destination, photo);
        destPhoto.save();
    }

    @After
    public void tearDown() {
        //TODO: delete created photo files.
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void undoDeleteGood() {
        undoDeletePhotoWithToken(
                user.getToken(),
                200,
                photo.getPhotoId(),
                true);
    }

    @Test
    public void undoDeleteGoodAdmin() {
        undoDeletePhotoWithToken(
                adminUser.getToken(),
                200,
                photo.getPhotoId(),
                true);
    }

    @Test
    public void undoDeleteBadRequest() {
        undoDeletePhotoWithToken(
                user.getToken(),
                400,
                photo.getPhotoId(),
                false);
    }

    @Test
    public void undoDeleteUnauthorised() {
        photo.delete();

        Result result = fakeClient.makeRequestWithNoToken(
                "PUT",
                "/api/users/photos/" + photo.getPhotoId() + "/undodelete");
        Assert.assertEquals(401, result.status());
    }

    @Test
    public void undoDeleteForbidden() {
        undoDeletePhotoWithToken(
                otherUser.getToken(),
                403,
                photo.getPhotoId(),
                true
        );
    }


    @Test
    public void undoDeleteNotFound() {
        undoDeletePhotoWithToken(
                user.getToken(),
                404,
                10000000,
                false
        );
    }

    @Test
    public void iCanUndoAProfilePhoto() {
        // Set profile photo and delete "old" photo
        PersonalPhoto personalPhoto = new PersonalPhoto("abc", true, user, true, "thumb");
        personalPhoto.save();
        user.setProfilePhoto(personalPhoto);

        photo.delete();
        String endpoint = "/api/users/" + user.getUserId() + "/profilephoto/" + photo.getPhotoId() +  "/undo";

        Result result = fakeClient.makeRequestWithToken("PUT", endpoint, user.getToken());
        Assert.assertEquals(200, result.status());
        User retrievedUser = User.find.byId(user.getUserId());
        Assert.assertNotNull(retrievedUser);
        Assert.assertNotNull(retrievedUser.getProfilePhoto());
        Assert.assertEquals(retrievedUser.getProfilePhoto().getPhotoId(), photo.getPhotoId());

    }

    /**
     * Deletes a photo and checks that the correct status code is found.
     *
     * @param token the token to send with the undo request.
     * @param statusCode the status code of the result.
     * @param photoId the id of the photo to undo.
     * @param delete true if the photo should actually be deleted first.
     */
    private void undoDeletePhotoWithToken(String token, int statusCode, int photoId, boolean delete) {
        if (delete) photo.delete();

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/photos/" + photoId + "/undodelete",
                token);
        Assert.assertEquals(statusCode, result.status());

        if (statusCode == 200) {
            Optional<PersonalPhoto> optionalPhoto = PersonalPhoto.find.query().where().eq("photo_id", photo.getPhotoId()).findOneOrEmpty();
            Assert.assertTrue(optionalPhoto.isPresent());
        }
    }

    @Test
    public void undoDeleteDestinationPhotoGood() {
        undoDeleteDestinationPhotoWithToken(user.getToken(), 200, destPhoto.getDestinationPhotoId(), true);
    }

    @Test
    public void undoDeleteDestinationPhotoGoodAdmin() {
        undoDeleteDestinationPhotoWithToken(adminUser.getToken(), 200, destPhoto.getDestinationPhotoId(), true);
    }

    @Test
    public void undoDeleteDestinationPhotoNotFound() {
        undoDeleteDestinationPhotoWithToken(user.getToken(), 404, 10000000, false);
    }

    @Test
    public void undoDeleteDestinationPhotoBadRequest() {
        undoDeleteDestinationPhotoWithToken(user.getToken(), 400, destPhoto.getDestinationPhotoId(), false);
    }

    @Test
    public void undoDeleteDestinationPhotoUnauthorized() {
        destPhoto.delete();

        Result result = fakeClient.makeRequestWithNoToken(
                "PUT",
                "/api/users/photos/" + destPhoto.getDestinationPhotoId() + "/undodelete");
        Assert.assertEquals(401, result.status());
    }

    @Test
    public void undoDeleteDestinationPhotoForbidden() {
        undoDeleteDestinationPhotoWithToken(otherUser.getToken(), 403, destPhoto.getDestinationPhotoId(), false);
    }

    /**
     * Deletes a destination photo and checks that the correct status code is found.
     *
     * @param token the token to send with the undo request.
     * @param statusCode the status code of the result.
     * @param photoId the id of the photo to undo.
     * @param delete true if the photo should actually be deleted first.
     */
    private void undoDeleteDestinationPhotoWithToken(String token, int statusCode, int photoId, boolean delete) {
        if (delete) destPhoto.delete();

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/destinations/" + destination.getDestinationId() + "/photos/" + photoId + "/undodelete",
                token);
        Assert.assertEquals(statusCode, result.status());

        if (statusCode == 200) {
            Optional<PersonalPhoto> optionalPhoto = PersonalPhoto.find.query().where().eq("photo_id", photo.getPhotoId()).findOneOrEmpty();
            Assert.assertTrue(optionalPhoto.isPresent());
        }
    }
}