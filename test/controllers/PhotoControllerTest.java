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
import testingUtilities.FakeClient;
import testingUtilities.FakePlayClient;
import testingUtilities.PlayResultToJson;
import testingUtilities.TestState;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Test the PhotoController.
 */
public class PhotoControllerTest {

  Application application;
  FakeClient fakeClient;
  User user;
  User otherUser;
  User adminUser;
  PersonalPhoto photo;
  DestinationPhoto destPhoto;
  Destination destination;
  private int coverPhotoId;

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
    user = fakeClient.signUpUser("Timmy", "Tester", "timmy@tester.com", "abc123");
    user = User.find.byId(user.getUserId());
    otherUser = fakeClient.signUpUser("Tammy", "Tester", "tammy@tester.com", "abc123");
    adminUser = fakeClient.signUpUser("Andy", "Admin", "andy@admin.com", "abc123");

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

    File file =
        new File(
            System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/",
            "cucumber.jpeg");

    if (!file.exists()) {
      Assert.fail(String.format("File %s was not found", file));
    }

    Result result =
        fakeClient.makeMultipartFormRequestWithFileAndToken(
            "POST", "/api/users/" + user.getUserId() + "/photos", user.getToken(), file, values);
    Assert.assertEquals(201, result.status());

    int newPhotoId = PlayResultToJson.convertResultToJson(result).get("photoId").asInt();

    PersonalPhoto personalPhoto = PersonalPhoto.find.byId(newPhotoId);
    Assert.assertNotNull(personalPhoto);
    user = User.find.byId(user.getUserId());

    File file2 =
        new File(
            System.getProperty("user.dir") + "/test/resources/fileStorageForTests/photos/",
            "cat.jpeg");

    if (!file2.exists()) {
      Assert.fail(String.format("File %s was not found", file2));
    }

    Result result2 =
        fakeClient.makeMultipartFormRequestWithFileAndToken(
            "POST", "/api/users/" + user.getUserId() + "/photos", user.getToken(), file2, values);
    Assert.assertEquals(201, result2.status());

    int destPhotoId = PlayResultToJson.convertResultToJson(result2).get("photoId").asInt();

    photo = PersonalPhoto.find.byId(destPhotoId);
    Assert.assertNotNull(photo);
    photo.setCover(true);
    photo.save();
    user.setCoverPhoto(photo);
    user.save();

    // Add some destinations
    DestinationType destinationType = new DestinationType("city");
    Country country = new Country("Peru", "PE", true);
    String district = "Test District";
    destination =
        new Destination(
            "Test City",
            destinationType,
            district,
            0.0,
            0.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);

    destinationType.save();
    country.save();
    destination.save();

    destPhoto = new DestinationPhoto(destination, photo);
    destPhoto.save();
  }

  @After
  public void tearDown() {
    String path = System.getProperty("user.dir") + "/storage/photos";

    if (coverPhotoId != 0) {
      PersonalPhoto coverPhoto = PersonalPhoto.find.byId(coverPhotoId);
      File coverFile = new File(path, coverPhoto.getFilenameHash());
      coverFile.delete();
    }

    File photoFile = new File(path, photo.getFilenameHash());
    File thumbFile = new File(path, photo.getThumbnailName());
    photoFile.delete();
    thumbFile.delete();
    Helpers.stop(application);
    TestState.clear();
  }

  @Test
  public void addCoverPhotoGood() {
    setUserCoverPhoto(user.getToken(), 200, user.getUserId(), photo.getPhotoId());
  }

  @Test
  public void addCoverPhotoGoodAdmin() {
    setUserCoverPhoto(adminUser.getToken(), 200, user.getUserId(), photo.getPhotoId());
  }

  @Test
  public void addCoverPhotoNotFoundPhoto() {
    setUserCoverPhoto(user.getToken(), 404, user.getUserId(), 90000);
  }

  @Test
  public void addCoverPhotoNotFoundUser() {
    setUserCoverPhoto(user.getToken(), 404, 90000, photo.getPhotoId());
  }

  @Test
  public void addCoverPhotoUnauthorised() {
    Result result =
        fakeClient.makeRequestWithNoToken(
            "PUT",
            String.format("/api/users/%d/photos/%d/cover", user.getUserId(), photo.getPhotoId()));
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void addCoverPhotoForbiddenWrongUser() {
    setUserCoverPhoto(otherUser.getToken(), 403, user.getUserId(), photo.getPhotoId());
  }

  @Test
  public void addCoverPhotoForbiddenWrongOwner() {
    setUserCoverPhoto(otherUser.getToken(), 403, otherUser.getUserId(), photo.getPhotoId());
  }

  @Test
  public void deleteCoverPhotoGood() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
  }

  @Test
  public void deleteCoverPhotoGoodAdmin() {
    deleteCoverPhotoWithToken(adminUser.getToken(), 200, user.getUserId());
  }

  @Test
  public void deleteCoverPhotoNotFoundPhoto() {
    deleteCoverPhotoWithToken(adminUser.getToken(), 404, adminUser.getUserId());
  }

  @Test
  public void deleteCoverPhotoNotFoundUser() {
    deleteCoverPhotoWithToken(adminUser.getToken(), 404, 90000);
  }

  @Test
  public void deleteCoverPhotoUnauthorised() {
    Result result =
        fakeClient.makeRequestWithNoToken(
            "DELETE", String.format("/api/users/%d/photos/cover", user.getUserId()));
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void deleteCoverPhotoForbidden() {
    deleteCoverPhotoWithToken(otherUser.getToken(), 403, user.getUserId());
  }

  @Test
  public void undoDeleteCoverPhotoGood() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    undoDeleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId(), true);
  }

  @Test
  public void undoDeleteCoverPhotoGoodAdmin() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    undoDeleteCoverPhotoWithToken(adminUser.getToken(), 200, user.getUserId(), true);
  }

  @Test
  public void undoDeleteCoverPhotoNotFoundPhoto() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    undoDeleteCoverPhotoWithToken(adminUser.getToken(), 404, adminUser.getUserId(), false);
  }

  @Test
  public void undoDeleteCoverPhotoNotFoundUser() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    undoDeleteCoverPhotoWithToken(user.getToken(), 404, 900000, false);
  }

  @Test
  public void undoDeleteCoverPhotoNotFoundPhotoNotDeleted() {
    undoDeleteCoverPhotoWithToken(user.getToken(), 404, 900000, false);
  }

  @Test
  public void undoDeleteCoverPhotoUnauthorised() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    Result result =
        fakeClient.makeRequestWithNoToken(
            "PUT", String.format("/api/users/%d/photos/%d/cover/undodelete", user.getUserId(), photo.getPhotoId()));
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void undoDeleteCoverPhotoForbidden() {
    deleteCoverPhotoWithToken(user.getToken(), 200, user.getUserId());
    undoDeleteCoverPhotoWithToken(otherUser.getToken(), 403, user.getUserId(), true);
  }

  @Test
  public void undoDeleteGood() {
    undoDeletePhotoWithToken(user.getToken(), 200, photo.getPhotoId(), true);
  }

  @Test
  public void undoDeleteGoodAdmin() {
    undoDeletePhotoWithToken(adminUser.getToken(), 200, photo.getPhotoId(), true);
  }

  @Test
  public void undoDeleteBadRequest() {
    undoDeletePhotoWithToken(user.getToken(), 400, photo.getPhotoId(), false);
  }

  @Test
  public void undoDeleteUnauthorised() {
    photo.delete();

    Result result =
        fakeClient.makeRequestWithNoToken(
            "PUT", String.format("/api/users/photos/%d/undodelete", photo.getPhotoId()));
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void undoDeleteForbidden() {
    undoDeletePhotoWithToken(otherUser.getToken(), 403, photo.getPhotoId(), true);
  }

  @Test
  public void undoDeleteNotFound() {
    undoDeletePhotoWithToken(user.getToken(), 404, 10000000, false);
  }

  @Test
  public void iCanUndoAProfilePhoto() {
    // Set profile photo and delete "old" photo
    PersonalPhoto personalPhoto = new PersonalPhoto("abc", true, user, true, "thumb", false);
    personalPhoto.save();
    user.setProfilePhoto(personalPhoto);

    photo.delete();
    String endpoint =
        "/api/users/" + user.getUserId() + "/profilephoto/" + photo.getPhotoId() + "/undo";

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

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT", String.format("/api/users/photos/%d/undodelete", photoId), token);
    Assert.assertEquals(statusCode, result.status());

    if (statusCode == 200) {
      Optional<PersonalPhoto> optionalPhoto =
          PersonalPhoto.find.query().where().eq("photo_id", photo.getPhotoId()).findOneOrEmpty();
      Assert.assertTrue(optionalPhoto.isPresent());
    }
  }

  /**
   * Deletes a cover photo for a given user and checks the response matches the expected.
   *
   * @param token the token of the requesting user.
   * @param statusCode the expected status code.
   * @param userId the id of the user.
   */
  private void deleteCoverPhotoWithToken(String token, int statusCode, int userId) {
    Result result =
        fakeClient.makeRequestWithToken(
            "DELETE", String.format("/api/users/%d/photos/cover", userId), token);
    Assert.assertEquals(statusCode, result.status());

    if (statusCode == 200) {
      Optional<PersonalPhoto> optionalPhoto =
          PersonalPhoto.find
              .query()
              .setIncludeSoftDeletes()
              .where()
              .eq("photo_id", photo.getPhotoId())
              .and()
              .eq("deleted", true)
              .findOneOrEmpty();
      Assert.assertTrue(optionalPhoto.isPresent());
    }
  }

  /**
   * Deletes a cover photo for a user and checks that the correct status code is found.
   *
   * @param token the token to send with the undo request.
   * @param statusCode the status code of the result.
   * @param userId the id of the user to undo.
   * @param delete true if the photo should actually be deleted first.
   */
  private void undoDeleteCoverPhotoWithToken(
      String token, int statusCode, int userId, boolean delete) {
    if (delete) photo.delete();

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT", String.format("/api/users/%d/photos/%d/cover/undodelete", userId, photo.getPhotoId()), token);
    Assert.assertEquals(statusCode, result.status());

    if (statusCode == 200) {
      Optional<PersonalPhoto> optionalPhoto =
          PersonalPhoto.find.query().where().eq("photo_id", photo.getPhotoId()).findOneOrEmpty();
      Assert.assertTrue(optionalPhoto.isPresent());
    }
  }

  @Test
  public void undoDeleteDestinationPhotoGood() {
    undoDeleteDestinationPhotoWithToken(
        user.getToken(), 200, destPhoto.getDestinationPhotoId(), true);
  }

  @Test
  public void undoDeleteDestinationPhotoGoodAdmin() {
    undoDeleteDestinationPhotoWithToken(
        adminUser.getToken(), 200, destPhoto.getDestinationPhotoId(), true);
  }

  @Test
  public void undoDeleteDestinationPhotoNotFound() {
    undoDeleteDestinationPhotoWithToken(user.getToken(), 404, 10000000, false);
  }

  @Test
  public void undoDeleteDestinationPhotoBadRequest() {
    undoDeleteDestinationPhotoWithToken(
        user.getToken(), 400, destPhoto.getDestinationPhotoId(), false);
  }

  @Test
  public void undoDeleteDestinationPhotoUnauthorized() {
    destPhoto.delete();

    Result result =
        fakeClient.makeRequestWithNoToken(
            "PUT", "/api/users/photos/" + destPhoto.getDestinationPhotoId() + "/undodelete");
    Assert.assertEquals(401, result.status());
  }

  @Test
  public void undoDeleteDestinationPhotoForbidden() {
    undoDeleteDestinationPhotoWithToken(
        otherUser.getToken(), 403, destPhoto.getDestinationPhotoId(), false);
  }

  /**
   * Deletes a destination photo and checks that the correct status code is found.
   *
   * @param token the token to send with the undo request.
   * @param statusCode the status code of the result.
   * @param photoId the id of the photo to undo.
   * @param delete true if the photo should actually be deleted first.
   */
  private void undoDeleteDestinationPhotoWithToken(
      String token, int statusCode, int photoId, boolean delete) {
    if (delete) destPhoto.delete();

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            String.format(
                "/api/destinations/%d/photos/%d/undodelete",
                destination.getDestinationId(), photoId),
            token);
    Assert.assertEquals(statusCode, result.status());

    if (statusCode == 200) {
      boolean exists =
          PersonalPhoto.find.query().where().eq("photo_id", photo.getPhotoId()).exists();
      Assert.assertTrue(exists);
    }
  }

  /**
   * Sends a request to set the cover photo for a user and check the expected response code.
   *
   * @param token the token of the user requesting.
   * @param statusCode the expected status code.
   * @param userId the id of the user to set the cover photo.
   * @param photoId the id pof the photo to set as the cover photo.
   */
  private void setUserCoverPhoto(String token, int statusCode, int userId, int photoId) {
    Result result =
        fakeClient.makeRequestWithToken(
            "PUT", String.format("/api/users/%d/photos/%d/cover", userId, photoId), token);
    Assert.assertEquals(statusCode, result.status());

    try {
      coverPhotoId = PlayResultToJson.convertResultToJson(result).get("photoId").asInt();
    } catch (IOException | NullPointerException e) {}

    if (statusCode == 200) {
      Optional<User> optionalUser =
          User.find.query().where().eq("user_id", user.getUserId()).findOneOrEmpty();
      Assert.assertTrue(optionalUser.isPresent());
      Assert.assertNotNull(optionalUser.get().getCoverPhoto());
    }
  }
}
