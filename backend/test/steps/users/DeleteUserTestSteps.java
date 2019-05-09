package steps.users;

import com.fasterxml.jackson.databind.JsonNode;
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
import utils.PlayResultToJson;

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

    private Role superAdminRole;
    private Role adminRole;
    private Role travellerRole;
    private Result resultBeingInspected; // user to persist results between test steps

    @Before("@DeleteUserSteps")
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
        this.createNationalities();
        this.createPassports();
    }

    /**
     * Set up the roles for each user
     */
    private void createRoles() {
        superAdminRole = new Role(RoleType.SUPER_ADMIN);
        adminRole = new Role(RoleType.ADMIN);
        travellerRole = new Role(RoleType.TRAVELLER);
        superAdminRole.save();
        adminRole.save();
        travellerRole.save();
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

    @After("@DeleteUserSteps")
    public void tearDown() {
        if (adminAnna != null) {
            User.find.deleteById(adminAnna.getUserId());
        }
        if (alice != null) {
            User.find.deleteById(alice.getUserId());
        }
        if (bob != null) {
            User.find.deleteById(bob.getUserId());
        }
        if (defaultAdminDaniel != null) {
            User.find.deleteById(defaultAdminDaniel.getUserId());
        }
        Helpers.stop(application);
    }

    @Given("that the default admin exists")
    public void thatTheDefaultAdminExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(superAdminRole);
        this.defaultAdminDaniel = this.createUser(roles, "Daniel", "Default Admin", "admin@travelea.com");
        Assert.assertTrue(this.defaultAdminDaniel.isDefaultAdmin());
    }

    @Given("an admin user exists")
    public void anAdminUserExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(adminRole);
        this.adminAnna = this.createUser(roles, "Anna", "Admin", "anna.admin@travelea.com");
        Assert.assertTrue(this.adminAnna.isAdmin());
    }

    @Given("^a regular user with first name Alice exists$")
    public void aRegularUserWithFirstNameAliceExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(travellerRole);
        this.alice = this.createUser(roles, "Alice", "Traveller", "alice@gmail.com");
    }

    @Given("^a regular user with first name Bob exists$")
    public void aRegularUserWithFirstNameBobExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(travellerRole);
        this.bob = this.createUser(roles, "Bob", "Admin", "bob@email.com");
    }

    @Given("^that the default admin is logged in$")
    public void thatTheDefaultAdminIsLoggedIn() throws FailedToLoginException {
        defaultAdminDaniel = this.fakeClient.loginMadeUpUser(defaultAdminDaniel, this.sharedPassword);
        Assert.assertTrue(defaultAdminDaniel.getToken().length() > 0);
        Assert.assertTrue(defaultAdminDaniel.isDefaultAdmin());
    }

    @Given("^the admin is logged in$")
    public void theAdminIsLoggedIn() throws FailedToLoginException {
        adminAnna = this.fakeClient.loginMadeUpUser(adminAnna, this.sharedPassword);
        Assert.assertTrue(adminAnna.getToken().length() > 0);
    }

    @Given("^the two regular users are logged in$")
    public void theTwoRegularUsersAreLoggedIn() throws FailedToLoginException {
        alice = this.fakeClient.loginMadeUpUser(alice, this.sharedPassword);
        bob = this.fakeClient.loginMadeUpUser(bob, this.sharedPassword);
        Assert.assertTrue(alice.getToken().length() > 0);
        Assert.assertTrue(bob.getToken().length() > 0);
    }

    @When("^the admin tries to delete the regular user Alice$")
    public void theAdminTriesToDeleteTheRegularUserAlice() throws IOException {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + alice.getUserId(), this.adminAnna.getToken());
        JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
        Assert.assertEquals(200, res.status());
    }

    @Then("^the regular user Alice should be deleted$")
    public void theRegularUserAliceShouldBeDeleted() throws IOException {
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + alice.getUserId(), this.adminAnna.getToken());
        JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
        Assert.assertEquals(404, res.status());
    }

    @When("the admin tries to delete the default admin")
    public void theAdminTriesToDeleteTheDefaultAdmin() {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.adminAnna.getToken());
        Assert.assertEquals(401, res.status());
    }

    @Then("the default admin should not be deleted")
    public void theDefaultAdminShouldNotBeDeleted() {
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
        Assert.assertEquals(200, res.status());
    }

    @When("^the regular user Alice tries to delete the other regular user Bob$")
    public void theRegularUserAliceTriesToDeleteTheOtherRegularUserBob() {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(401, res.status());
    }

    @Then("^the regular user Bob should not be deleted$")
    public void theRegularUserBobShouldNotBeDeleted() {
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(200, res.status());
    }

    @When("^a regular user Bob tries to delete their own profile$")
    public void aRegularUserBobTriesToDeleteTheirOwnProfile() {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + bob.getUserId(), this.bob.getToken());
        Assert.assertEquals(200, res.status());
    }

    @Then("^the regular user Bob should be deleted$")
    public void theRegularUserBobShouldBeDeleted() {
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(404, res.status());
    }

    @When("the default admin tries to delete their own profile")
    public void theDefaultAdminTriesToDeleteTheirOwnProfile() {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
        Assert.assertEquals(401, res.status());
    }

    @When("^a regular user Bob tries to delete the default admin$")
    public void aRegularUserBobTriesToDeleteTheDefaultAdmin() throws Throwable {
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.bob.getToken());
        Assert.assertEquals(401, res.status());
    }

    @When("^someone tries to delete a user that does not exist$")
    public void someoneTriesToDeleteAUserThatDoesNotExist() {
        this.resultBeingInspected = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + -1, this.defaultAdminDaniel.getToken());
    }

    @Then("^the server should respond saying that the user does not exist$")
    public void theServerShouldRespondSayingThatTheUserDoesNotExist() {
        Assert.assertEquals(404, this.resultBeingInspected.status());
    }

    private User createUser(List<Role> roles, String firstName, String lastName, String email) {
        List<Nationality> nationalities = Nationality.find.query().findList();
        List<TravellerType> travellerTypes = TravellerType.find.query().findList();
        List<Passport> passports = Passport.find.query().findList();
        Date date = new Date();
        String passwordHash = new Security().hashPassword(this.sharedPassword);
        User user = new User(firstName, "", lastName, passwordHash, "Other", email, nationalities, travellerTypes, date, passports, roles, "");
        user.save();
        return user;
    }
}
