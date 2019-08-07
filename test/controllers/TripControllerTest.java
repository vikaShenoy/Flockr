package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;
import play.mvc.Result;


import java.io.IOException;
import java.util.*;

import static utils.PlayResultToJson.convertResultToJson;

public class TripControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;
    Destination christchurch;
    Destination westMelton;
    Destination helkett;
    TripDestination tripChristchurch;
    TripDestination tripWestMelton;
    TripDestination tripHelkett;
    List<TripDestination> tripDestinations;
    Trip trip;
    Trip trip2;
    ObjectNode tripDestination1;
    ObjectNode tripDestination2;

    @Before
    public void setUp() throws ServerErrorException, IOException, FailedToSignUpException {

        // Configuration
        Map<String, String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

        // Fake Client
        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);
        fakeClient = new FakePlayClient(application);

        // Users
        user = fakeClient.signUpUser("Tommy", "Tester", "tommy@tester.com",
                "testing");
        otherUser = fakeClient.signUpUser("Indy", "Inspector", "indy@inspector.com",
                "testing");
        adminUser = fakeClient.signUpUser("Sam", "Admin", "sam@admin.com",
                "testing");

        // Making an admin
        Role role = new Role(RoleType.ADMIN);
        role.save();
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        adminUser = User.find.byId(adminUser.getUserId());
        adminUser.setRoles(roles);
        adminUser.save();

        // Creating some initial destinations
        DestinationType destinationType = new DestinationType("city");
        destinationType.save();
        Country country = new Country("New Zealand", "NZ", true);
        country.save();
        District district = new District("Canterbury", country);
        district.save();

        christchurch = new Destination("Christchurch", destinationType, district,0.0, 0.0, country, user.getUserId(), new ArrayList<>(), true);
        christchurch.save();

        westMelton = new Destination("West Melton", destinationType, district,0.0, 0.0, country, user.getUserId(), new ArrayList<>(), true);
        westMelton.save();

        helkett = new Destination("Helkett", destinationType, district,0.0, 0.0, country, user.getUserId(), new ArrayList<>(), true);
        helkett.save();

        // Creating a trip

        tripChristchurch = new TripDestination(christchurch, new Date(1564272000), 43200, new Date(1564358400), 43200);
        tripWestMelton = new TripDestination(westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
        tripHelkett = new TripDestination(westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
        tripChristchurch.save();
        tripWestMelton.save();
        tripHelkett.save();
        tripDestinations = new ArrayList<>();
        tripDestinations.add(tripChristchurch);
        tripDestinations.add(tripWestMelton);
        List<User> users = new ArrayList<>();
        users.add(user);
        trip = new Trip(tripDestinations, users, "Testing Trip 1");
        trip.save();
        tripDestinations.remove(tripWestMelton);
        tripDestinations.add(tripHelkett);
        trip2 = new Trip(tripDestinations,users,"Find the family graves");
        trip2.save();
        tripDestination1 = Json.newObject();
        tripDestination1.put("destinationId", 1);
        tripDestination1.put("arrivalDate", 123456789);
        tripDestination1.put("arrivalTime", 450);
        tripDestination1.put("departureDate", 123856789);
        tripDestination1.put("departureTime", 240);

        tripDestination2 = Json.newObject();
        tripDestination2.put("destinationId", 2);
        tripDestination2.put("arrivalDate", 123456789);
        tripDestination2.put("arrivalTime", 450);
        tripDestination2.put("departureDate", 123856789);
        tripDestination2.put("departureTime", 240);
    }

    private void restore(Trip trip) {
        trip.setDeleted(false);
        trip.setDeletedExpiry(null);
        trip.save();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertTrue(optionalTrip.isPresent());
    }

    @Test
    public void restoreTripOk() {
        trip.delete();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertFalse(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId() + "/restore",
                user.getToken()
        );
        Assert.assertEquals(200, result.status());

    }

    @Test
    public void restoreTripForbidden() {
        trip.delete();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertFalse(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId() + "/restore",
                otherUser.getToken()
        );
        Assert.assertEquals(403, result.status());
        restore(trip);
    }

    @Test
    public void restoreTripUnauthorized() {
        trip.delete();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertFalse(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithNoToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId() + "/restore"
        );
        Assert.assertEquals(401, result.status());
        restore(trip);
    }

    @Test
    public void restoreTripAdmin() {
        trip.delete();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertFalse(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId() + "/restore",
                adminUser.getToken()
        );
        Assert.assertEquals(200, result.status());
    }

    @Test
    public void restoreTripNotFound() {
        trip.delete();
        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertFalse(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + -50 + "/restore",
                user.getToken()
        );
        Assert.assertEquals(404, result.status());
        restore(trip);
    }

    @Test
    public void restoreTripBadRequest() {

        Optional<Trip> optionalTrip = Trip.find.query()
                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
        Assert.assertTrue(optionalTrip.isPresent());

        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId() + "/restore",
                user.getToken()
        );
        Assert.assertEquals(400, result.status());
    }

    @Test
    public void createTripWithUsers() throws IOException {
        String endpoint = "/api/users/" + user.getUserId() + "/trips";
        ObjectNode tripBody = Json.newObject();
        ArrayNode tripDestinations = Json.newArray();
        tripDestinations.add(tripDestination1);
        tripDestinations.add(tripDestination2);
        tripBody.put("tripName", "Some trip");
        tripBody.putArray("tripDestinations").addAll(tripDestinations);
        List<Integer> userIds = new ArrayList<>();
        userIds.add(otherUser.getUserId());
        tripBody.set("userIds", Json.toJson(userIds));
        Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
        Assert.assertEquals(201, result.status());
        int tripId = PlayResultToJson.convertResultToJson(result).asInt();
        Trip receivedTrip = Trip.find.byId(tripId);
        Assert.assertNotNull(receivedTrip);
        Assert.assertEquals(2, receivedTrip.getUsers().size());
    }

    @Test
    public void cannotCreateTripWithUser() {
        String endpoint = "/api/users/" + user.getUserId() + "/trips";
        ObjectNode tripBody = Json.newObject();
        ArrayNode tripDestinations = Json.newArray();
        tripDestinations.add(tripDestination1);
        tripDestinations.add(tripDestination2);


        tripBody.put("tripName", "Some trip");
        tripBody.putArray("tripDestinations").addAll(tripDestinations);
        List<Integer> userIds = new ArrayList<>();
        userIds.add(user.getUserId());
        tripBody.set("userIds", Json.toJson(userIds));
        Result result = fakeClient.makeRequestWithToken("POST", tripBody, endpoint, user.getToken());
        Assert.assertEquals(403, result.status());
    }

    @Test
    public void editTripWithUsers() throws IOException {
        String endpoint = "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId();
        ObjectNode tripBody = Json.newObject();
        ArrayNode tripDestinations = Json.newArray();
        tripDestinations.add(tripDestination1);
        tripDestinations.add(tripDestination2);

        tripBody.put("tripName", "Some trip");
        tripBody.putArray("tripDestinations").addAll(tripDestinations);
        List<Integer> userIds = new ArrayList<>();
        userIds.add(otherUser.getUserId());
        tripBody.set("userIds", Json.toJson(userIds));
        Result result = fakeClient.makeRequestWithToken("PUT", tripBody, endpoint, user.getToken());
        Assert.assertEquals(200, result.status());
    }

    @Test
    public void cannotHaveNoUsersInTrip() {
        String endpoint = "/api/users/" + user.getUserId() + "/trips/" + trip.getTripId();
        ObjectNode tripBody = Json.newObject();
        ArrayNode tripDestinations = Json.newArray();
        tripDestinations.add(tripDestination1);
        tripDestinations.add(tripDestination2);

        tripBody.put("tripName", "Some trip");
        tripBody.putArray("tripDestinations").addAll(tripDestinations);
        List<Integer> userIds = new ArrayList<>();
        tripBody.set("userIds", Json.toJson(userIds));
        Result result = fakeClient.makeRequestWithToken("PUT", tripBody, endpoint, user.getToken());
        Assert.assertEquals(403, result.status());
    }





//    @Test
//    public void putSubTripOk() {
//
//        Optional<Trip> optionalParentTrip = Trip.find.query()
//                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
//        Optional<Trip> optionalSubTrip = Trip.find.query()
//                .where().eq("trip_id", trip2.getTripId()).findOneOrEmpty();
//        optionalSubTrip.get().setParentId(optionalParentTrip.get().getTripId());
//        Result result = fakeClient.makeRequestWithToken(
//                "PUT",
//                "/api/users/" + user.getUserId() + "/trips/" + optionalSubTrip.get().getTripId(),
//                user.getToken());
//        Assert.assertEquals(200, result.status());
//
//    }
//
//        @Test
//    public void putSubTripAdmin() {
//
//        Optional<Trip> optionalParentTrip = Trip.find.query()
//                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
//        Optional<Trip> optionalSubTrip = Trip.find.query()
//                .where().eq("trip_id", trip2.getTripId()).findOneOrEmpty();
//        optionalSubTrip.get().setParentId(optionalParentTrip.get().getTripId());
//        Result result = fakeClient.makeRequestWithToken(
//                "PUT",
//                "/api/users/" + user.getUserId() + "/trips/" + optionalSubTrip.get().getTripId(),
//                adminUser.getToken());
//        Assert.assertEquals(200, result.status());
//
//    }
//
//        @Test
//    public void putSubTripUnauthorized() {
//
//        Optional<Trip> optionalParentTrip = Trip.find.query()
//                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
//        Optional<Trip> optionalSubTrip = Trip.find.query()
//                .where().eq("trip_id", trip2.getTripId()).findOneOrEmpty();
//        optionalSubTrip.get().setParentId(optionalParentTrip.get().getTripId());
//        Result result = fakeClient.makeRequestWithNoToken(
//                "PUT",
//                "/api/users/" + user.getUserId() + "/trips/" + optionalSubTrip.get().getTripId());
//        Assert.assertEquals(401, result.status());
//
//    }
//
//        @Test
//    public void putSubTripNotFound() {
//
//        Optional<Trip> optionalParentTrip = Trip.find.query()
//                .where().eq("trip_id", trip.getTripId()).findOneOrEmpty();
//        Optional<Trip> optionalSubTrip = Trip.find.query()
//                .where().eq("trip_id", trip2.getTripId()).findOneOrEmpty();
//        optionalSubTrip.get().setParentId(optionalParentTrip.get().getTripId());
//        Result result = fakeClient.makeRequestWithToken(
//                "PUT",
//                "/api/users/" + user.getUserId() + "/trips/" + -999 , user.getToken());
//        Assert.assertEquals(404, result.status());
//
//    }





    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }


}
