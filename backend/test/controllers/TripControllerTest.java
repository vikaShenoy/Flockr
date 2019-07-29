package controllers;

import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.TestState;
import play.mvc.Result;


import java.io.IOException;
import java.util.*;

public class TripControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;
    Destination christchurch;
    Destination westMelton;
    TripDestination tripChristchurch;
    TripDestination tripWestMelton;
    List<TripDestination> tripDestinations;
    Trip trip;

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

        // Creating a trip

        tripChristchurch = new TripDestination(christchurch, new Date(1564272000), 43200, new Date(1564358400), 43200);
        tripWestMelton = new TripDestination(westMelton, new Date(1564358400), 50400, new Date(1564358400), 68400);
        tripChristchurch.save();
        tripWestMelton.save();
        tripDestinations = new ArrayList<>();
        tripDestinations.add(tripChristchurch);
        tripDestinations.add(tripWestMelton);
        trip = new Trip(tripDestinations, user, "Testing Trip 1");
        trip.save();
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

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }


}
