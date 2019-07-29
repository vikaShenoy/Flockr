package controllers;

import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.After;
import org.junit.Before;
import play.Application;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.TestState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripControllerTest {

    Application application;
    FakeClient fakeClient;
    User user;
    User otherUser;
    User adminUser;
    Destination destination;
    DestinationProposal destinationProposal;

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
        fakeClient = new FakePlayClient(application);

        user = fakeClient.signUpUser("Tommy", "Tester", "tommy@tester.com",
                "testing");
        otherUser = fakeClient.signUpUser("Indy", "Inspector", "indy@inspector.com",
                "testing");
        adminUser = fakeClient.signUpUser("Sam", "Admin", "sam@admin.com",
                "testing");

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

        // Add some proposal
        TravellerType travellerType = new TravellerType("Gap Year");
        List<TravellerType> travellerTypes = new ArrayList<>();
        travellerTypes.add(travellerType);
        destinationProposal = new DestinationProposal(destination, travellerTypes, user);
        destinationProposal.save();
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }
}
