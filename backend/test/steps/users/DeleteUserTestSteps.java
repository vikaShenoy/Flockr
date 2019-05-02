package steps.users;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToLoginException;
import models.*;
import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.mvc.Result;
import play.test.Helpers;
import util.Security;
import utils.FakeClient;
import utils.FakePlayClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeleteUserTestSteps {

    @Inject
    private Application application;

    private FakeClient fakeClient;
    private String sharedPassword = "IAmASecurePassword"; // made up password for tests
    private User adminAnna; // admin
    private User defaultAdminDaniel; // default admin
    private User alice; // regular user
    private User bob; // regular user
    private Result resultBeingInspected; // used to persist server results between test steps

    private Role superAdminRole;
    private Role adminRole;
    private Role travellerRole;

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
        this.fakeClient = new FakePlayClient(application);
        this.createRoles();
    }

    private void createRoles() {
        superAdminRole = new Role(RoleType.SUPER_ADMIN);
        adminRole = new Role(RoleType.ADMIN);
        travellerRole = new Role(RoleType.TRAVELLER);
        superAdminRole.save();
        adminRole.save();
        travellerRole.save();
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
    }

    @Given("that the default admin exists")
    public void thatTheDefaultAdminExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(superAdminRole);
        this.defaultAdminDaniel = this.createUser(roles, "Daniel", "Default Admin", "admin@travelea.com");
    }

    @Given("an admin user exists")
    public void anAdminUserExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(adminRole);
        this.adminAnna = this.createUser(roles, "Anna", "Admin", "amna.admin@travekea.com");
    }

    @Given("two regular users exist")
    public void twoRegularUsersExist() {
        List<Role> roles = new ArrayList<>();
        roles.add(travellerRole);
        this.alice = this.createUser(roles, "Alice", "Traveller", "alice@gmail.com");
        this.bob = this.createUser(roles, "Bob", "Admin", "bob@email.com");
    }

    @Given("^that the default admin is logged in$")
    public void thatTheDefaultAdminIsLoggedIn() throws FailedToLoginException {
        defaultAdminDaniel = this.fakeClient.loginMadeUpUser(defaultAdminDaniel, this.sharedPassword);
        Assert.assertTrue(defaultAdminDaniel.getToken().length() > 0);
    }

    @Given("^the admin is logged in$")
    public void theAdminIsLoggedIn() throws FailedToLoginException {
        adminAnna = this.fakeClient.loginMadeUpUser(adminAnna, this.sharedPassword);
    }

    @Given("^the two regular users are logged in$")
    public void theTwoRegularUsersAreLoggedIn() throws FailedToLoginException {
        alice = this.fakeClient.loginMadeUpUser(alice, this.sharedPassword);
        bob = this.fakeClient.loginMadeUpUser(bob, this.sharedPassword);
        Assert.assertTrue(alice.getToken().length() > 0);
        Assert.assertTrue(bob.getToken().length() > 0);
    }

    @When("the admin tries to delete the regular user")
    public void theAdminTriesToDeleteTheRegularUser() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + bob.getUserId(), this.adminAnna.getToken());
    }

    @Then("the regular user should be deleted")
    public void theRegularUserShouldBeDeleted() {
        Result res = fakeClient.makeRequestWithToken("GET", "api/users/" + bob.getUserId(), this.adminAnna.getToken());
        Assert.assertEquals(404, res.status());
    }

    @When("the admin tries to delete the default admin")
    public void theAdminTriesToDeleteTheDefaultAdmin() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + defaultAdminDaniel.getUserId(), this.adminAnna.getToken());
    }

    @Then("the default admin should not be deleted")
    public void theDefaultAdminShouldNotBeDeleted() {
        Result res = fakeClient.makeRequestWithToken("GET", "api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
        Assert.assertEquals(200, res.status());
    }

    @When("a regular user tries to delete another regular user")
    public void aRegularUserTriesToDeleteAnotherRegularUser() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + bob.getUserId(), this.alice.getToken());
    }

    @Then("the second user should not be deleted")
    public void theSecondUserShouldNotBeDeleted() {
        fakeClient.makeRequestWithToken("GET", "api/users/" + bob.getUserId(), this.alice.getToken());
    }

    @When("a regular user tries to delete their own profile")
    public void aRegularUserTriesToDeleteTheirOwnProfile() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + alice.getUserId(), this.alice.getToken());
    }

    @When("the default admin tries to delete their own profile")
    public void theDefaultAdminTriesToDeleteTheirOwnProfile() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
    }

    @When("a regular user tries to delete the default admin")
    public void aRegularUserTriesToDeleteTheDefaultAdmin() {
        fakeClient.makeRequestWithToken("DELETE", "api/users/" + defaultAdminDaniel.getUserId(), this.bob.getToken());
    }

    private User createUser(List<Role> roles, String firstName, String lastName, String email) {

        Nationality nationality = new Nationality("Bolivia");
        nationality.save();

        List<Nationality> nationalities = Nationality.find.query().findList();

        TravellerType travellerType = new TravellerType("");
        travellerType.save();

        List<TravellerType> travellerTypes = TravellerType.find.query().findList();

        Passport passport = new Passport("New Zealand");
        passport.save();

        List<Passport> passports = Passport.find.query().findList();

        Date date = new Date();

        String passwordHash = new Security().hashPassword(this.sharedPassword);

        User user;

        user = new User(firstName, "", lastName, passwordHash, "Other", email, nationalities, travellerTypes, date, passports, roles, "");

        user.save();
        return user;
    }
}
