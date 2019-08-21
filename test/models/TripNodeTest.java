package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;

public class TripNodeTest {

  private Application application;
  private FakeClient fakeClient;
  private User user;
  private User otherUser;
  private User adminUser;
  private Destination destination1;
  private Destination destination2;
  private TripComposite trip;
  private TripDestinationLeaf leaf1;
  private TripDestinationLeaf leaf2;
  private TripDestinationLeaf leaf3;
  private TripDestinationLeaf leaf4;
  private TripComposite trip2;

  @Before
  public void setUp() throws ServerErrorException, IOException, FailedToSignUpException {
    Map<String, String> testSettings = new HashMap<>();
    testSettings.put("db.default.driver", "org.h2.Driver");
    testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
    testSettings.put("play.evolutions.db.default.enabled", "true");
    testSettings.put("play.evolutions.db.default.autoApply", "true");
    testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");
    testSettings.put("environment_test", "test");

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
    Assert.assertNotNull(adminUser);
    adminUser.setRoles(roles);
    adminUser.save();

    // Add some destinations
    DestinationType destinationType = new DestinationType("city");
    Country country = new Country("Peru", "PE", true);
    District district = new District("Test District", country);
    destination1 =
        new Destination(
            "Test City 1",
            destinationType,
            district,
            0.0,
            0.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);
    destination2 =
        new Destination(
            "Test City 2",
            destinationType,
            district,
            1.0,
            1.0,
            country,
            user.getUserId(),
            new ArrayList<>(),
            true);

    destinationType.save();
    country.save();
    district.save();
    destination1.save();
    destination2.save();

    leaf1 = new TripDestinationLeaf(destination1, null, null, null, null);
    leaf2 = new TripDestinationLeaf(destination2, null, null, null, null);
    leaf1.save();
    leaf2.save();

    List<TripNode> tripNodes = new ArrayList<>();
    tripNodes.add(leaf1);
    tripNodes.add(leaf2);
    ArrayList<User> users = new ArrayList<>();
    users.add(user);
    trip = new TripComposite(tripNodes, users, "test trip");
    trip.save();

    leaf3 = new TripDestinationLeaf(destination1, null, null, null, null);
    leaf4 = new TripDestinationLeaf(destination2, null, null, null, null);
    List<TripNode> tripNodes2 = new ArrayList<>();

    trip2 = new TripComposite(tripNodes2, users, "test trip2");
    trip2.addTripNode(leaf3);
    trip2.addTripNode(leaf4);

    trip2.save();
  }

  @After
  public void tearDown() {
    Helpers.stop(application);
    TestState.clear();
  }

  @Test
  public void checkGetTripDiffCompositeDestination() throws IOException {
    TripComposite superTrip = new TripComposite(new ArrayList<>(), new ArrayList<>(), "super trip");

    superTrip.addTripNode(leaf1);
    superTrip.addTripNode(trip);
    superTrip.addUser(user);

    superTrip.save();

    Result result =
        fakeClient.makeRequestWithToken(
            "GET",
            "/api/users/" + user.getUserId() + "/trips/" + superTrip.getTripNodeId(),
            user.getToken());

    Assert.assertEquals(200, result.status());
    JsonNode resultJson = PlayResultToJson.convertResultToJson(result);

    Assert.assertEquals(superTrip.getTripNodeId(), resultJson.get("tripNodeId").asInt());

    Assert.assertEquals(superTrip.getTripNodes().size(), resultJson.get("tripNodes").size());

    Assert.assertEquals(
        destination1.getDestinationId(),
        resultJson.get("tripNodes").get(0).get("destination").get("destinationId").asInt());

    Assert.assertTrue(resultJson.get("tripNodes").get(1).get("destination").isNull());

    Assert.assertEquals(user.getUserId(), resultJson.get("users").get(0).get("userId").asInt());

    Assert.assertEquals(0, resultJson.get("tripNodes").get(0).get("users").size());
  }

  @Test
  public void checkTripsArePersistent() {
    TripComposite superTrip = new TripComposite();
    superTrip.setName("Composite Trip");
    TripNode subTrip1 = new TripDestinationLeaf();
    TripNode subTrip2 = new TripDestinationLeaf();
    TripComposite compTrip2 = new TripComposite();
    compTrip2.setName("comp Trip 2");

    superTrip.addTripNode(subTrip1);
    superTrip.addTripNode(subTrip2);
    superTrip.addTripNode(compTrip2);

    superTrip.save();

    Assert.assertEquals(3, superTrip.getTripNodes().size());

    TripComposite afterSavedTripNode = TripComposite.find.byId(superTrip.getTripNodeId());

    Assert.assertEquals("Composite Trip", afterSavedTripNode.getName());
    Assert.assertEquals(3, afterSavedTripNode.getTripNodes().size());
  }

  @Test
  public void checkCreateTripEndpoint() throws IOException {
    String date = null;
    ObjectNode newTripJson = Json.newObject();
    newTripJson.put("name", "New Trip");

    ObjectNode firstNode = Json.newObject();
    firstNode.put("destinationId", leaf2.getDestination().getDestinationId());
    firstNode.put("nodeType", "TripDestinationLeaf");
    firstNode.put("arrivalDate", date);
    firstNode.put("arrivalTime", date);
    firstNode.put("departureDate", date);
    firstNode.put("departureTime", date);

    ObjectNode secondNode = Json.newObject();
    secondNode.put("tripNodeId", trip.getTripNodeId());
    secondNode.put("nodeType", "TripComposite");

    ObjectNode thirdNode = Json.newObject();
    thirdNode.put("destinationId", leaf1.getDestination().getDestinationId());
    thirdNode.put("nodeType", "TripDestinationLeaf");
    secondNode.put("arrivalDate", date);
    secondNode.put("arrivalTime", date);
    secondNode.put("departureDate", date);
    secondNode.put("departureTime", date);

    newTripJson.putArray("tripNodes").add(firstNode).add(secondNode).add(thirdNode);
    newTripJson.putArray("userIds");

    Result result =
        fakeClient.makeRequestWithToken(
            "POST", newTripJson, "/api/users/" + user.getUserId() + "/trips", user.getToken());
    Assert.assertEquals(201, result.status());

    JsonNode jsonNode = PlayResultToJson.convertResultToJson(result);
    int tripId = jsonNode.get("tripNodeId").asInt();

    TripComposite receivedTrip = TripComposite.find.byId(tripId);
    Assert.assertEquals(3, receivedTrip.getTripNodes().size());
  }

  @Test
  public void userUpdatesTrip() throws IOException {

    String date = null;
    ObjectNode updatedTripJson = Json.newObject();
    updatedTripJson.put("name", "Updated Trip");

    ObjectNode firstNode = Json.newObject();
    firstNode.put("destinationId", leaf2.getDestination().getDestinationId());
    firstNode.put("nodeType", "TripDestinationLeaf");
    firstNode.put("arrivalDate", date);
    firstNode.put("arrivalTime", date);
    firstNode.put("departureDate", date);
    firstNode.put("departureTime", date);

    ObjectNode secondNode = Json.newObject();
    secondNode.put("tripNodeId", trip2.getTripNodeId());
    secondNode.put("nodeType", "TripComposite");

    updatedTripJson.putArray("tripNodes").add(firstNode).add(secondNode);
    updatedTripJson.putArray("userIds").add(user.getUserId());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            updatedTripJson,
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId(),
            user.getToken());
    Assert.assertEquals(200, result.status());
    JsonNode tripJson = PlayResultToJson.convertResultToJson(result);
    int receivedTripNodeId = tripJson.get("tripNodeId").asInt();
    TripComposite receivedTrip = TripComposite.find.byId(receivedTripNodeId);
    Assert.assertNotNull(receivedTrip);
    Assert.assertEquals(2, receivedTrip.getTripNodes().size());
  }

  @Test
  public void updateTripAdmin() throws IOException {

    String date = null;
    ObjectNode updatedTripJson = Json.newObject();
    updatedTripJson.put("name", "Updated Trip");

    ObjectNode firstNode = Json.newObject();
    firstNode.put("destinationId", leaf2.getDestination().getDestinationId());
    firstNode.put("nodeType", "TripDestinationLeaf");
    firstNode.put("arrivalDate", date);
    firstNode.put("arrivalTime", date);
    firstNode.put("departureDate", date);
    firstNode.put("departureTime", date);

    ObjectNode secondNode = Json.newObject();
    secondNode.put("tripNodeId", trip2.getTripNodeId());
    secondNode.put("nodeType", "TripComposite");

    updatedTripJson.putArray("tripNodes").add(firstNode).add(secondNode);
    updatedTripJson.putArray("userIds").add(user.getUserId());

    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            updatedTripJson,
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId(),
            adminUser.getToken());
    Assert.assertEquals(200, result.status());
    int receivedTripId = PlayResultToJson.convertResultToJson(result).get("tripNodeId").asInt();
    TripComposite tripComposite = TripComposite.find.byId(receivedTripId);

    Assert.assertNotNull(tripComposite);

    Assert.assertEquals(2, tripComposite.getTripNodes().size());
    Assert.assertTrue(tripComposite.getTripNodes().contains(trip2));
  }

  @Test
  public void updateTripForbidden() {

    String date = null;
    ObjectNode updatedTripJson = Json.newObject();
    updatedTripJson.put("name", "Updated Trip");

    ObjectNode firstNode = Json.newObject();
    firstNode.put("destinationId", leaf2.getDestination().getDestinationId());
    firstNode.put("nodeType", "TripDestinationLeaf");
    firstNode.put("arrivalDate", date);
    firstNode.put("arrivalTime", date);
    firstNode.put("departureDate", date);
    firstNode.put("departureTime", date);

    ObjectNode secondNode = Json.newObject();
    secondNode.put("tripNodeId", trip.getTripNodeId());
    secondNode.put("nodeType", "TripComposite");

    updatedTripJson.putArray("tripNodes").add(firstNode).add(secondNode);
    updatedTripJson.putArray("userIds").add(user.getUserId());

    System.out.println(trip.getUsers());
    Result result =
        fakeClient.makeRequestWithToken(
            "PUT",
            updatedTripJson,
            "/api/users/" + user.getUserId() + "/trips/" + trip.getTripNodeId(),
            otherUser.getToken());
    Assert.assertEquals(403, result.status());
  }

  @Test
  public void checkOrderPersists() throws IOException {

    List<User> users = new ArrayList<>();
    users.add(user);

    List<TripNode> subTripNodes = new ArrayList<>();
    subTripNodes.add(leaf3);
    subTripNodes.add(leaf4);

    TripComposite subTrip = new TripComposite(subTripNodes, users, "sub trip");
    subTrip.save();

    String date = null;
    ObjectNode newTripJson = Json.newObject();
    newTripJson.put("name", "Updated Trip");

    ObjectNode firstNode = Json.newObject();
    firstNode.put("destinationId", leaf1.getDestination().getDestinationId());
    firstNode.put("nodeType", "TripDestinationLeaf");
    firstNode.put("arrivalDate", date);
    firstNode.put("arrivalTime", date);
    firstNode.put("departureDate", date);
    firstNode.put("departureTime", date);

    ObjectNode secondNode = Json.newObject();
    secondNode.put("tripNodeId", subTrip.getTripNodeId());
    secondNode.put("nodeType", "TripComposite");

    ObjectNode thirdNode = Json.newObject();
    thirdNode.put("destinationId", leaf2.getDestination().getDestinationId());
    thirdNode.put("nodeType", "TripDestinationLeaf");
    thirdNode.put("arrivalDate", date);
    thirdNode.put("arrivalTime", date);
    thirdNode.put("departureDate", date);
    thirdNode.put("departureTime", date);

    newTripJson.putArray("tripNodes").add(firstNode).add(secondNode).add(thirdNode);
    newTripJson.putArray("userIds");

    List<TripNode> superTripNodes = new ArrayList<>();
    superTripNodes.add(leaf1);
    superTripNodes.add(subTrip);
    superTripNodes.add(leaf2);

    Result result =
        fakeClient.makeRequestWithToken(
            "POST", newTripJson, "/api/users/" + user.getUserId() + "/trips", user.getToken());

    Assert.assertEquals(201, result.status());
    int tripId = PlayResultToJson.convertResultToJson(result).get("tripNodeId").asInt();

    Result result1 =
        fakeClient.makeRequestWithToken(
            "GET", "/api/users/" + user.getUserId() + "/trips/" + tripId, user.getToken());

    Assert.assertEquals(200, result1.status());

    JsonNode jsonNode = PlayResultToJson.convertResultToJson(result1);
    JsonNode persistedTripNodes = jsonNode.get("tripNodes");

    for (int i = 0; i < persistedTripNodes.size(); i++) {
      Assert.assertEquals(
          superTripNodes.get(i).getName(), persistedTripNodes.get(i).get("name").asText());
    }
  }
}
