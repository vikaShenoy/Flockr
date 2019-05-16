package steps;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import models.Nationality;
import models.Passport;
import models.Role;
import models.RoleType;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.test.Helpers;
import utils.FakePlayClient;

public class CommonTestSteps {

    @Inject
    private Application application;

    @Before
    public void setUp() {
        Module testModule = new AbstractModule() {
            @Override
            public void configure() {
            }
        };
        GuiceApplicationBuilder builder = new GuiceApplicationLoader()
                .builder(new ApplicationLoader.Context(Environment.simple()))
                .overrides(testModule);
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
        Helpers.start(application);
        TestState.getInstance().setApplication(application);
        TestState.getInstance().setFakeClient(new FakePlayClient(application));
        this.createRoles();
        this.createNationalities();
        this.createPassports();
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
        chilean.save();
        mexican.save();
    }

    /**
     * Make up some passports in the database
     */
    private void createPassports() {
        Passport china = new Passport("China");
        Passport southKorean = new Passport("South Korea");
        china.save();
        southKorean.save();
    }

    @After
    public void tearDown() {
        Application application = TestState.getInstance().getApplication();
        Helpers.stop(application);
        TestState.clear();
    }


}
