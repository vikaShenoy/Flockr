package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
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
import utils.TestState;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Add some destinations
        DestinationType destinationType = new DestinationType("city");
        Country country = new Country("Peru", "PE", true);
        District district = new District("Test District", country);
        destination1 = new Destination("Test City 1", destinationType, district,
                0.0, 0.0, country, user.getUserId(), new ArrayList<>(), true);
        destination2 = new Destination("Test City 2", destinationType, district,
                1.0, 1.0, country, user.getUserId(), new ArrayList<>(), true);

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

        trip = new TripComposite(tripNodes, new ArrayList<>(), "test trip");
        trip.save();
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void firstTest() {
        TripNode superTrip = new TripComposite();
        ((TripComposite) superTrip).setName("Composite Trip");
        TripNode subTrip1 = new TripDestinationLeaf();
        TripNode subTrip2 = new TripDestinationLeaf();
        TripNode compTrip2 = new TripComposite();
        ((TripComposite) compTrip2).setName("comp Trip 2");

        superTrip.addTripNodes(subTrip1);
        superTrip.addTripNodes(subTrip2);
        superTrip.addTripNodes(compTrip2);

        superTrip.save();

        Assert.assertEquals(3, superTrip.getTripNodes().size());

        TripComposite afterSavedTripNode = TripComposite.find.byId(superTrip.getTripNodeId());

        Assert.assertEquals("Composite Trip", afterSavedTripNode.getName());
        Assert.assertEquals(3, afterSavedTripNode.getTripNodes().size());
    }

    @Test
    public void secondTest() {
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

        newTripJson.putArray("tripNodes")
                .add(firstNode)
                .add(secondNode)
                .add(thirdNode);
        newTripJson.putArray("userIds");



        Result result = fakeClient.makeRequestWithToken("POST", (ObjectNode) newTripJson,"/api/users/" + user.getUserId() + "/trips", user.getToken());
        Assert.assertEquals(201, result.status());

    }

}