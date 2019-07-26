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

import static org.junit.Assert.*;

public class DestinationControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;
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
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void undoDeleteGood() {
        destination.delete();
        Optional<Destination> optionalDestination = Destination.find.query()
                .where().eq("destination_id", destination.getDestinationId()).findOneOrEmpty();
        Assert.assertFalse(optionalDestination.isPresent());

        undoDeleteDestination(user.getToken(), destination.getDestinationId(), 200);
    }

    @Test
    public void undoDeleteGoodAdmin() {
        destination.delete();
        Optional<Destination> optionalDestination = Destination.find.query()
                .where().eq("destination_id", destination.getDestinationId()).findOneOrEmpty();
        Assert.assertFalse(optionalDestination.isPresent());
        undoDeleteDestination(adminUser.getToken(), destination.getDestinationId(), 200);
    }

    @Test
    public void undoDeleteBadRequest() {
        undoDeleteDestination(user.getToken(), destination.getDestinationId(), 400);
    }

    @Test
    public void undoDeleteUnauthorised() {
        destination.delete();
        Result result = fakeClient.makeRequestWithNoToken(
                "PUT",
                "/api/destinations/" + destination.getDestinationId() + "/undodelete");
        Assert.assertEquals(401, result.status());
    }

    @Test
    public void undoDeleteForbidden() {
        destination.delete();
        Optional<Destination> optionalDestination = Destination.find.query()
                .where().eq("destination_id", destination.getDestinationId()).findOneOrEmpty();
        Assert.assertFalse(optionalDestination.isPresent());
        undoDeleteDestination(otherUser.getToken(), destination.getDestinationId(), 403);
    }

    @Test
    public void undoDeleteNotFound() {
        undoDeleteDestination(user.getToken(), 10000000, 404);
    }

    /**
     * Calls the api to undo a deletion of a destination and checks the result.
     *
     * @param token the auth token to send with the request.
     * @param destinationId the destination id.
     * @param statusCode the status code of the result.
     */
    private void undoDeleteDestination(String token, int destinationId, int statusCode) {
        Result result = fakeClient.makeRequestWithToken(
                "PUT",
                "/api/destinations/" + destinationId + "/undodelete",
                token);
        Assert.assertEquals(statusCode, result.status());

        if (statusCode == 200) {
            Optional<Destination> optionalDestination = Destination.find.query()
                    .where().eq("destination_id", destination.getDestinationId()).findOneOrEmpty();
            Assert.assertTrue(optionalDestination.isPresent());
        }
    }
}