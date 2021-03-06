package controllers.TripControllerTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import java.io.IOException;
import java.util.*;

import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import util.TripUtil;
import testingUtilities.FakeClient;
import testingUtilities.FakePlayClient;
import testingUtilities.PlayResultToJson;
import testingUtilities.TestState;

public class TripControllerTest {

  private Application application;
  private FakeClient fakeClient;
  private User user;
  private User otherUser;
  private User adminUser;
  private User anotherUser;
  private Destination christchurch;
  private Destination westMelton;
  private Destination helkett;
  private TripDestinationLeaf tripChristchurch;
  private TripDestinationLeaf tripWestMelton;
  private TripDestinationLeaf tripHelkett;
  private List<TripNode> tripNodes;
  private TripComposite trip;
  private TripComposite trip2;
  private ObjectNode tripDestination1;
  private ObjectNode tripDestination2;
  private ObjectNode tripDestination3;
  private ObjectNode tripComposite1;
  private TripUtil tripUtil;

  @Before
  public void setUp() throws ServerErrorException, IOException, FailedToSignUpException {

    // Configuration
    Map<String, String> testSettings = new HashMap<>();
    testSettings.put("db.default.driver", "org.h2.Driver");
    testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
    testSettings.put("play.evolutions.db.default.enabled", "true");
    testSettings.put("play.evolutions.db.default.autoApply", "true");
    testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");
    tripUtil = new TripUtil();

    // Fake Client
    application = Helpers.fakeApplication(testSettings);
    Helpers.start(application);
    fakeClient = new FakePlayClient(application);

    // Users
    user = fakeClient.signUpUser("Tommy", "Tester", "tommy@tester.com", "testing");
    otherUser = fakeClient.signUpUser("Indy", "Inspector", "indy@inspector.com", "testing");
    adminUser = fakeClient.signUpUser("Sam", "Admin", "sam@admin.com", "testing");
    anotherUser = fakeClient.signUpUser("Another", "User", "user@test.com", "testing");

    // Making an admin
    // TODO - investigate why this code causes duplicate key exception. Must be to do with
    // population script.
    Role role = new Role(RoleType.ADMIN);
    role.save();
    List<Role> roles = new ArrayList<>();
    roles.add(role);
    adminUser = User.find.byId(adminUser.getUserId());
    adminUser.setRoles(roles);
    adminUser.save();

    // Insert all trip roles into the db.
    Role tripOwner = new Role(RoleType.TRIP_OWNER);
    Role tripManager = new Role(RoleType.TRIP_MANAGER);
    Role tripMember = new Role(RoleType.TRIP_MEMBER);
    tripOwner.save();
    tripManager.save();
    tripMember.save();

    // Creating some initial destinations
    DestinationType destinationType = new DestinationType("city");
    destinationType.save();
    Country country = new Country("New Zealand", "NZ", true);
    country.save();
    String district = "Canterbury";

    christchurch =
        new Destination(
            "Christchurch",
            destinationType,
            district,
            0.0,
            0.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);
    christchurch.save();

    westMelton =
        new Destination(
            "West Melton",
            destinationType,
            district,
            0.0,
            0.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);
    westMelton.save();

    helkett =
        new Destination(
            "Helkett",
            destinationType,
            district,
            0.0,
            0.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);
    helkett.save();

    // Creating a trip

    tripChristchurch =
        new TripDestinationLeaf(
            christchurch, new Date(1564272000), 43200, new Date(1564358400), 43200);
    tripWestMelton =
        new TripDestinationLeaf(
            westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
    tripHelkett =
        new TripDestinationLeaf(
            westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
    tripChristchurch.save();
    tripWestMelton.save();
    tripHelkett.save();
    tripNodes = new ArrayList<>();
    tripNodes.add(tripChristchurch);
    tripNodes.add(tripWestMelton);
    List<User> users = new ArrayList<>();
    users.add(user);
    trip = new TripComposite(tripNodes, users, "Testing Trip 1");

    tripNodes.remove(tripWestMelton);
    tripNodes.add(tripHelkett);
    trip2 = new TripComposite(tripNodes, users, "Find the family graves");
    trip2.save();

    tripDestination1 = Json.newObject();
    tripDestination1.put("nodeType", "TripDestinationLeaf");
    tripDestination1.put("destinationId", 1);
    tripDestination1.put("arrivalDate", 123456789);
    tripDestination1.put("arrivalTime", 450);
    tripDestination1.put("departureDate", 123856789);
    tripDestination1.put("departureTime", 240);

    tripDestination2 = Json.newObject();
    tripDestination2.put("nodeType", "TripDestinationLeaf");
    tripDestination2.put("destinationId", 2);
    tripDestination2.put("arrivalDate", 123456789);
    tripDestination2.put("arrivalTime", 450);
    tripDestination2.put("departureDate", 123856789);
    tripDestination2.put("departureTime", 240);

    tripDestination3 = Json.newObject();
    tripDestination3.put("nodeType", "TripDestinationLeaf");
    tripDestination3.put("destinationId", 1);
    tripDestination3.put("arrivalDate", 123456789);
    tripDestination3.put("arrivalTime", 450);
    tripDestination3.put("departureDate", 123856789);
    tripDestination3.put("departureTime", 240);

    tripComposite1 = Json.newObject();
    tripComposite1.put("nodeType", "TripComposite");
    tripComposite1.put("tripNodeId", trip.getTripNodeId());

    Destination morocco =
        new Destination(
            "Morocco",
            destinationType,
            district,
            12.0,
            45.0,
            country,
            adminUser.getUserId(),
            new ArrayList<>(),
            true);
    morocco.save();
    TripDestinationLeaf tripMorocco =
        new TripDestinationLeaf(morocco, new Date(1564273000), 43200, new Date(1564359000), 43200);
    tripMorocco.save();

    List<TripNode> tripNodes3 = new ArrayList<>();
    tripNodes3.add(tripChristchurch);
    tripNodes3.add(tripMorocco);

    TripComposite trip3 = new TripComposite(tripNodes3, users, "Testing Trip 3");
    trip3.save();

    List<TripNode> tripNodes1 = new ArrayList<>();
    tripNodes1.add(trip3);

    trip.setTripNodes(tripNodes1);
    trip.save();
  }

  @Test
  public void createTripWithUsers() throws IOException {
    String endpoint = "/api/users/" + user.getUserId() + "/trips";
    List<Integer> userIds = new ArrayList<>();
    userIds.add(otherUser.getUserId());
    ObjectNode tripBody = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);
    tripBody.put("name", "Pirate Trip");
    tripBody.putArray("tripNodes").addAll(tripDestinations);
    tripBody.putArray("userIds").add(Json.newObject().put("userId", otherUser.getUserId()).put("role", "TRIP_MEMBER"));
    // tripBody.set("userIds", Json.toJson(userIds));

    System.out.println(tripBody.toString());
    Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
    Assert.assertEquals(201, result.status());
    int tripId = PlayResultToJson.convertResultToJson(result).get("tripNodeId").asInt();
    TripComposite receivedTrip = TripComposite.find.byId(tripId);
    Assert.assertNotNull(receivedTrip);
    Assert.assertEquals(2, receivedTrip.getUsers().size());
  }

  /**
   * Check for 403 from the POST trips endpoint. Occurs when a user tries to add their own ID to the
   * userIds field.
   */
  @Test
  public void cannotCreateTripWithUser() {
    String endpoint = "/api/users/" + user.getUserId() + "/trips";
    ObjectNode tripBody = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    List<Integer> userIds = new ArrayList<>();
    userIds.add(user.getUserId());
    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);
    tripBody.put("name", "Pirate trip");
    tripBody.putArray("tripNodes").addAll(tripDestinations);
    tripBody.putArray("userIds").add(Json.newObject().put("userId", user.getUserId()).put("role", "TRIP_MEMBER"));
    //tripBody.set("userIds", Json.toJson(userIds));
    Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
    Assert.assertEquals(403, result.status());
  }

  /**
   * Edits a trip with users in the Trip
   */
  @Test
  public void editTripWithUsers() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);
    String endpoint = "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId();
    ObjectNode tripBody = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    ArrayNode tripUsers = Json.newArray();
    ObjectNode otherUserObject = Json.newObject();
    otherUserObject.put("userId", otherUser.getUserId());
    otherUserObject.put("role", RoleType.TRIP_MEMBER.toString());
    tripUsers.add(otherUserObject);

    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);

    tripBody.put("name", "Some trip");
    tripBody.putArray("tripNodes").addAll(tripDestinations);
    tripBody.putArray("userIds").addAll(tripUsers);
    Result result = fakeClient.makeRequestWithToken("PUT", tripBody, endpoint, user.getToken());
    Assert.assertEquals(200, result.status());
  }

  /**
   * Checks the scenario where the trips have no users in the trip
   */
  @Test
  public void cannotHaveNoUsersInTrip() {
    String endpoint = "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId();
    ObjectNode tripBody = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);

    tripBody.put("name", "Some trip");
    tripBody.putArray("tripNodes").addAll(tripDestinations);
    List<Integer> userIds = new ArrayList<>();
    tripBody.set("userIds", Json.toJson(userIds));
    Result result = fakeClient.makeRequestWithToken("PUT", tripBody, endpoint, user.getToken());
    Assert.assertEquals(403, result.status());
  }

  /**
   * Checks that contiguous destinations are being detected as expected
   */
  @Test
  public void destinationsAreContiguous() {
    ArrayNode nodes = Json.newArray();
    nodes.add(tripDestination1);
    nodes.add(tripDestination3);
    boolean isContiguous = tripUtil.checkContiguousDestinations(nodes);
    Assert.assertTrue(isContiguous);
  }

  /**
   * Checks that non-contiguous destinations are not passed as contiguous destinations
   */
  @Test
  public void destinationsNotContiguous() {
    ArrayNode nodes = Json.newArray();
    nodes.add(tripDestination1);
    nodes.add(tripDestination2);
    boolean isContiguous = tripUtil.checkContiguousDestinations(nodes);
    Assert.assertFalse(isContiguous);
  }

  /**
   * Check that the contiguous destination function returns true when contiguous destinations are
   * separated by a trip composite object.
   */
  @Test
  public void destinationsAreContiguousSeparated() {
    // Check function still returns true for contiguous destinations separated by a
    // TripComposite(sub-trip)
    ArrayNode nodes = Json.newArray();
    nodes.add(tripDestination1);
    nodes.add(tripComposite1);
    nodes.add(tripDestination3);
    boolean isContiguous = tripUtil.checkContiguousDestinations(nodes);
    Assert.assertTrue(isContiguous);
  }

  /**
   * Checks the undo-deletion of the trip node works
   * @param trip the trip to undo deletion of
   */
  private void restore(TripComposite trip) {
    trip.setDeleted(false);
    trip.setDeletedExpiry(null);
    trip.save();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertTrue(optionalTrip.isPresent());
  }

  /**
   * Checks undo-deletion of trip works when the user owns the trip
   */
  @Test
  public void restoreTripOk() {
    trip.delete();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertFalse(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId() + "/restore",
            user.getToken());
    Assert.assertEquals(200, result.status());
  }

  /**
   * Checks the undo-deletion of the trip does not work when the user trying to undo the deletion
   * does not own the trip and gives appropriate status code.
   */
  @Test
  public void restoreTripForbidden() {
    trip.delete();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertFalse(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId() + "/restore",
            otherUser.getToken());
    Assert.assertEquals(403, result.status());
    restore(trip);
  }

  /**
   * Checks undo-deletion of the trip does not work when the user is not logged in and gives
   * appropriate status code.
   */
  @Test
  public void restoreTripUnauthorized() {
    trip.delete();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertFalse(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithNoToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId() + "/restore");
    Assert.assertEquals(401, result.status());
    restore(trip);
  }

  /**
   * Checks the undo-deletion of the trip works when the admin is the on trying to undo the deletion
   */
  @Test
  public void restoreTripAdmin() {
    trip.delete();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertFalse(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId() + "/restore",
            adminUser.getToken());
    Assert.assertEquals(200, result.status());
  }

  /**
   * Checks that the undo-deletion of the trip does not work when the trip does not even exist and
   * gives appropriate status code.
   */
  @Test
  public void restoreTripNotFound() {
    trip.delete();
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertFalse(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + -50 + "/restore",
            user.getToken());
    Assert.assertEquals(404, result.status());
    restore(trip);
  }

  /**
   * Checks the undo-deletion of the trip when the trip is not even deleted and gives appropriate status code.
   */
  @Test
  public void restoreTripBadRequest() {

    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    Assert.assertTrue(optionalTrip.isPresent());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId() + "/restore",
            user.getToken());
    Assert.assertEquals(400, result.status());
  }

  /**
   * Gets the high level trips (the trips without a parent)
   *
   * @param token the token of the user that wants to get the trip
   * @param userId the user ID that wants to get the trip
   * @param statusCode the status code to test
   */
  private void getHighLevelTrips(String token, int userId, int statusCode) {
    Result result =
        fakeClient.makeRequestWithToken(
            "GET", "/api/users/" + userId + "/trips/high-level-trips", token);

    Assert.assertEquals(result.status(), statusCode);
  }

  /**
   * Checks if the high level trips (the trips without a parent) endpoint works when the user owns the
   * trip
   */
  @Test
  public void getHighLevelTripsUserOk() {
    getHighLevelTrips(user.getToken(), user.getUserId(), 200);
  }

  /**
   * Checks if the high level trips (the trips without a parent) endpoint works when the admin wants
   * to view the trip
   */
  @Test
  public void getHighLevelTripsAdminOk() {
    getHighLevelTrips(adminUser.getToken(), user.getUserId(), 200);
  }


  /**
   * Checks if the high level trips (the trips without a parent) endpoint does not works when the
   * user is not logged in.
   */
  @Test
  public void getHighLevelTripsUnauthorised() {
    Result result =
        fakeClient.makeRequestWithNoToken(
            "GET", "/api/users/" + user.getUserId() + "/trips/high-level-trips");

    Assert.assertEquals(401, result.status());
  }

  /**
   * Checks if the high level trips (the trips without a parent) endpoint does not works when the
   * user does not owns the trip is trying to view another user's trip
   */
  @Test
  public void getHighLevelTripsForbidden() {
    getHighLevelTrips(otherUser.getToken(), user.getUserId(), 403);
  }

  /**
   * The Test suite for updating a trip with the PUT endpoint.
   *
   * @param token the token of the user that wants to update the trip
   * @param userId the user ID that wants to update the trip
   * @param statusCode the status code to test
   * @param authorised true if an authorised user
   */
  private void updateTrip(String token, int userId, int statusCode, boolean authorised) {
    List<Integer> userIds = new ArrayList<>();
    userIds.add(userId);
    ObjectNode body = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    ArrayNode userArray = Json.newArray();
    ObjectNode userObject = Json.newObject();
    userObject.put("userId", userId);
    userObject.put("role", RoleType.TRIP_OWNER.toString());
    userArray.add(userObject);
    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);
    body.put("name", "PutTest");
    body.putArray("tripNodes").addAll(tripDestinations);
    body.putArray("userIds").addAll(userArray);
    Result result;
    if (authorised) {
      result =
              fakeClient.makeRequestWithToken(
                      "PUT", body, "/api/users/" + userId + "/trips/" + trip.getTripNodeId(), token);
    } else {
      result =
              fakeClient.makeRequestWithNoToken(
                      "PUT", body, "/api/users/" + userId + "/trips/" + trip.getTripNodeId());
    }
    Assert.assertEquals(statusCode, result.status());
  }

  /**
   * Checks if the updating of trips works when the user owns the trip being updated
   */
  @Test
  public void updateTripOk() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);
    updateTrip(user.getToken(), user.getUserId(), 200, true);
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
        tripComposite -> Assert.assertEquals("PutTest", tripComposite.getName()));
  }

  /**
   * Checks if the updating of trips does not work when the trip does not exist
   */
  @Test
  public void updateTripNotFound() {
    updateTrip(otherUser.getToken(), otherUser.getUserId(), 404, true);
  }

  /**
   * Checks if the updating of trips does not work when the user is not logged in
   */
  @Test
  public void updateTripForbidden() {
    updateTrip(user.getToken(), user.getUserId(), 401, false);
  }

  /**
   * Checks if the updating of trips works when the admin wants to update the trip
   */
  @Test
  public void updateTripAdmin() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);
    updateTrip(adminUser.getToken(), user.getUserId(), 200, true);
    Optional<TripComposite> optionalTrip =
        TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
        tripComposite -> Assert.assertEquals("PutTest", tripComposite.getName()));
  }

  /**
   * Send a PUT request to edit a trip.
   * @param token auth token for the user.
   * @param userId id of the user who owns the trip.
   * @param statusCode expected status code from the endpoint.
   * @param authorised whether the request should be sent with token or not.
   * @param body JSON body for the put request specifying the edits to be made.
   */
  private void updateTripWithBody(String token, int userId, int tripId, int statusCode, boolean authorised,
                                  ObjectNode body) {
    Result result;
    if (authorised) {
      result =
              fakeClient.makeRequestWithToken(
                      "PUT", body, "/api/users/" + userId + "/trips/" + tripId, token);
    } else {
      result =
              fakeClient.makeRequestWithNoToken(
                      "PUT", body, "/api/users/" + userId + "/trips/" + tripId);
    }
    Assert.assertEquals(statusCode, result.status());
  }

  /**
   * Checks the trip members cannot update a trip and gives appropriate status code
   */
  @Test
  public void updateTripAsTripMemberForbidden() {
    String originalName = trip.getName();
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_MEMBER);

    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();
    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());

    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),403, true,
            tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(originalName, tripComposite.getName()));
  }

  /**
   * Test that a trip manager editing trip name works fine.
   */
  @Test
  public void updateTripAsTripManagerOk() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_MANAGER);
    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();

    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());

    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),200, true, tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals("Edited Trip", tripComposite.getName()));
  }

  /**
   * Test that a manager can't edit trip users.
   */
  @Test
  public void updateTripAsTripManagerForbidden() {
    int numTripUsers = trip.getUsers().size();
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_MANAGER);
    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();

    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    tripNodes.add(tripDestination3);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());
    userJson.put(otherUser, RoleType.TRIP_MEMBER.toString());

    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),200, true,
            tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(numTripUsers, tripComposite.getUsers().size()));
  }

  /**
   * Trip owner is allowed to edit a trip's users.
   */
  @Test
  public void updateTripUserAsTripOwnerOk() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);

    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();
    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    tripNodes.add(tripDestination3);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());
    userJson.put(otherUser, RoleType.TRIP_MEMBER.toString());
    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),200, true,
            tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals("Edited Trip", tripComposite.getName()));
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(3, tripComposite.getTripNodes().size()));
  }

  /**
   * Give the 'other user' the trip manager role.
   */
  @Test
  public void updateTripUserPermissionsAsOwner() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);

    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();
    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    tripNodes.add(tripDestination3);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());
    userJson.put(otherUser, RoleType.TRIP_MANAGER.toString());
    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),200, true,
            tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals("Edited Trip", tripComposite.getName()));
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(3, tripComposite.getTripNodes().size()));
    optionalTrip.ifPresent(
            tripComposite -> {
              Assert.assertEquals(2, tripComposite.getUserRoles().size());
              boolean containsUpdatedRole = false;
              for (UserRole tripUserRole : tripComposite.getUserRoles()) {
                if (tripUserRole.getUser().getUserId() == otherUser.getUserId() &&
                        tripUserRole.getRole().getRoleType().equals(RoleType.TRIP_MANAGER.toString())) {
                  containsUpdatedRole = true;
                }
              }
              Assert.assertTrue(containsUpdatedRole);
            });
  }

  /**
   * Check that a trip owner for higher level trip can't edit the trip details for a lower level trip.
   */
  @Test
  public void updateLowerLevelTripBad() {
    TripControllerTestUtil.setUserTripRole(user, trip, RoleType.TRIP_OWNER);
    // Add trip2 as a sub trip of trip.
    List<TripNode> newTripNodes = trip.getTripNodes();
    newTripNodes.add(trip2);
    trip.setTripNodes(newTripNodes);
    String trip2OriginalName = trip2.getName();

    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();
    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());
    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip2.getTripNodeId(),
            403, true, tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip2.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(trip2OriginalName, tripComposite.getName()));
  }

  /**
   * Trip owner of trip 2 can't edit the higher level trip (trip).
   */
  @Test
  public void updateHigherLevelTripBad() {
    List<TripNode> newTripNodes = trip.getTripNodes();
    newTripNodes.add(trip2);
    trip.setTripNodes(newTripNodes);
    String tripOriginalName = trip.getName();
    TripControllerTestUtil.setUserTripRole(user, trip2, RoleType.TRIP_OWNER);

    Map<User, String> userJson = new HashMap<>();
    List<ObjectNode> tripNodes = new ArrayList<>();
    tripNodes.add(tripDestination1);
    tripNodes.add(tripDestination2);
    userJson.put(user, RoleType.TRIP_MEMBER.toString());
    ObjectNode tripRequestBody = TripControllerTestUtil.createTripJsonBody("Edited Trip", tripNodes, userJson);

    updateTripWithBody(user.getToken(), user.getUserId(), trip.getTripNodeId(),403, true,
            tripRequestBody);
    Optional<TripComposite> optionalTrip =
            TripComposite.find.query().where().eq("tripNodeId", trip.getTripNodeId()).findOneOrEmpty();
    optionalTrip.ifPresent(
            tripComposite -> Assert.assertEquals(tripOriginalName, tripComposite.getName()));
  }

  /**
   * Checks the a user can create a trip by themselves (not as a group trip)
   * @throws IOException
   */
  @Test
  public void createSoloTripOwner() throws IOException {

      String endpoint = "/api/users/" + user.getUserId() + "/trips";
      ObjectNode tripBody = Json.newObject();
      ArrayNode tripDestinations = Json.newArray();
      tripDestinations.add(tripDestination1);
      tripDestinations.add(tripDestination2);
      tripBody.put("name", "Solo Trip");
      tripBody.putArray("tripNodes").addAll(tripDestinations);
      tripBody.putArray("userIds");
      Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
      Assert.assertEquals(201, result.status());
      JsonNode jsonResult = PlayResultToJson.convertResultToJson(result);
      int tripId = jsonResult.get("tripNodeId").asInt();
      Optional<TripComposite> receivedTrip = TripComposite.find.query().where().eq("tripNodeId", tripId).findOneOrEmpty();
      Assert.assertTrue(receivedTrip.isPresent());
      Assert.assertEquals(1, jsonResult.get("userRoles").size());
      Assert.assertEquals(user.getUserId(), jsonResult.get("userRoles").get(0).get("user").get("userId").asInt());
      Assert.assertEquals("TRIP_OWNER", jsonResult.get("userRoles").get(0).get("role").get("roleType").asText());
  }

  /**
   * Checks that a user can create a group trip with an owner, manager and a member.
   * @throws IOException
   */
  @Test
  public void createGroupTripOwnerManagerMember() throws IOException {
    String endpoint = "/api/users/" + user.getUserId() + "/trips";
    ObjectNode tripBody = Json.newObject();
    ArrayNode tripDestinations = Json.newArray();
    tripDestinations.add(tripDestination1);
    tripDestinations.add(tripDestination2);
    tripBody.put("name", "Solo Trip");
    tripBody.putArray("tripNodes").addAll(tripDestinations);
    JsonNode manager = Json.newObject().put("userId", otherUser.getUserId()).put("role", "TRIP_MANAGER");
    JsonNode member = Json.newObject().put("userId", anotherUser.getUserId()).put("role", "TRIP_MEMBER");
    tripBody.putArray("userIds").add(manager).add(member);
    Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
    JsonNode jsonResult = PlayResultToJson.convertResultToJson(result);
    System.out.println(jsonResult.get("userRoles").toString());
    Assert.assertEquals(3, jsonResult.get("userRoles").size());

    Assert.assertEquals(otherUser.getUserId(), jsonResult.get("userRoles").get(0).get("user").get("userId").asInt());
    Assert.assertEquals("TRIP_MANAGER", jsonResult.get("userRoles").get(0).get("role").get("roleType").asText());

    Assert.assertEquals(anotherUser.getUserId(), jsonResult.get("userRoles").get(1).get("user").get("userId").asInt());
    Assert.assertEquals("TRIP_MEMBER", jsonResult.get("userRoles").get(1).get("role").get("roleType").asText());

    Assert.assertEquals(user.getUserId(), jsonResult.get("userRoles").get(2).get("user").get("userId").asInt());
    Assert.assertEquals("TRIP_OWNER", jsonResult.get("userRoles").get(2).get("role").get("roleType").asText());
  }


  @After
  public void tearDown() {
    Helpers.stop(application);
    TestState.clear();
  }

}
