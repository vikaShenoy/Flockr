package steps;

import com.google.inject.Inject;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import models.*;
import play.Application;
import play.test.Helpers;
import testingUtilities.FakePlayClient;
import testingUtilities.TestState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to share common test steps across Cucumber tests.
 */
public class CommonTestSteps {

    @Inject
    private Application application;

    @Before
    public void before(Scenario scenario) {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Starting - " + scenario.getName());
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Before
    public void setUp() {
        Map<String,String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);

        TestState.getInstance().setApplication(application);
        TestState.getInstance().setFakeClient(new FakePlayClient(application));

        this.createRoles();
        this.createNationalities();
        this.createPassports();
        this.createDestinations();
        this.createTravellerTypes();
    }

    /**
     * Set up the roles for each user
     */
    private void createRoles() {
        TestState currentTestState = TestState.getInstance();

        Role superAdminRole = new Role(RoleType.SUPER_ADMIN);
        Role adminRole = new Role(RoleType.ADMIN);
        Role travellerRole = new Role(RoleType.TRAVELLER);
        superAdminRole.save();
        adminRole.save();
        travellerRole.save();
        currentTestState.setSuperAdminRole(superAdminRole);
        currentTestState.setAdminRole(adminRole);
        currentTestState.setTravellerRole(travellerRole);
    }

    /**
     * Make up some nationalities in the database
     */
    private void createNationalities() {
        Nationality chilean = new Nationality("Chile");
        Nationality mexican = new Nationality("Mexico");
        Nationality newZealand = new Nationality("New Zealand");
        Nationality australia = new Nationality("Australia");
        Nationality afghanistan = new Nationality("Afghanistan");
        Nationality peru = new Nationality("Peru");

        chilean.save();
        mexican.save();
        newZealand.save();
        australia.save();
        afghanistan.save();
        peru.save();
    }

    /**
     * Make up some passports in the database
     */
    private void createPassports() {
        Passport china = new Passport("China");
        Passport southKorean = new Passport("South Korea");
        Passport nz = new Passport("NZ");
        Passport aus = new Passport("Australia");
        Passport peru = new Passport("Peru");
        Passport bolivia = new Passport("Bolivia");

        china.save();
        southKorean.save();
        nz.save();
        aus.save();
        peru.save();
        bolivia.save();

    }

    /**
     * Make some Destinations in the database
     */
    private void createDestinations() {
        DestinationType event = new DestinationType("Event");
        DestinationType city = new DestinationType("City");

        event.save();
        city.save();

        Country unitedStatesOfAmerica = new Country("United States of America", "USA", true);
        Country australia = new Country("Australia", "AUS", true);

        unitedStatesOfAmerica.save();
        australia.save();

        String blackRockCity = "Black Rock City";
        String newFarm ="New Farm";

        Destination burningMan = new Destination("Burning Man",event, blackRockCity, 12.1234,12.1234, unitedStatesOfAmerica,  null, new ArrayList<>(), false);
        Destination brisbaneCity = new Destination("Brisbane City", city, newFarm, 11.1234,11.1234, australia, null, new ArrayList<>(), false);

        burningMan.save();
        brisbaneCity.save();
    }

    /**
     * Make some traveller types in the database
     */
    private void createTravellerTypes() {
        TravellerType travellerType1 = new TravellerType("Groupies");
        TravellerType travellerType2 = new TravellerType("Thrillseeker");
        TravellerType travellerType3 = new TravellerType("Gap Year");
        TravellerType travellerType4 = new TravellerType("Frequent Weekender");
        TravellerType travellerType5 = new TravellerType("Holidaymaker");
        TravellerType travellerType6 = new TravellerType("Functional/Business");
        TravellerType travellerType7 = new TravellerType("Backpacker");
        TravellerType travellerType8 = new TravellerType("Luxury Traveller");

        travellerType1.save();
        travellerType2.save();
        travellerType3.save();
        travellerType4.save();
        travellerType5.save();
        travellerType6.save();
        travellerType7.save();
        travellerType8.save();
    }

    @After
    public void tearDown() {
        Application application = TestState.getInstance().getApplication();
        Helpers.stop(application);
        TestState.clear();
    }

    @After
    public void after(Scenario scenario) {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Ending - " + scenario.getName() + ": Status – " + scenario.getStatus());
        System.out.println("-----------------------------------------------------------------------------------------");
    }

}
