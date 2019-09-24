package steps.users;

import com.fasterxml.jackson.databind.JsonNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToLoginException;
import models.*;
import org.junit.Assert;
import play.mvc.Result;
import testingUtilities.TestState;
import util.Security;
import testingUtilities.FakeClient;
import testingUtilities.PlayResultToJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Test that deleting users works as expected.
 */
public class DeleteUserTestSteps {

    private String sharedPassword = "IAmASecurePassword"; // made up password for tests
    private User adminAnna; // admin
    private User defaultAdminDaniel; // default admin
    private User alice; // regular user
    private User bob; // regular user

    private Result resultBeingInspected; // user to persist results between test steps

    @Given("that the default admin exists")
    public void thatTheDefaultAdminExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestState.getInstance().getSuperAdminRole());
        this.defaultAdminDaniel = this.createUser(roles, "Daniel", "Default Admin", "admin@travelea.com");
        Assert.assertTrue(this.defaultAdminDaniel.isDefaultAdmin());
    }

    @Given("an admin user exists")
    public void anAdminUserExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestState.getInstance().getAdminRole());
        this.adminAnna = this.createUser(roles, "Anna", "Admin", "anna.admin@travelea.com");
        Assert.assertTrue(this.adminAnna.isAdmin());
    }

    @Given("^a regular user with first name Alice exists$")
    public void aRegularUserWithFirstNameAliceExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestState.getInstance().getTravellerRole());
        this.alice = this.createUser(roles, "Alice", "Traveller", "alice@gmail.com");
    }

    @Given("^a regular user with first name Bob exists$")
    public void aRegularUserWithFirstNameBobExists() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestState.getInstance().getTravellerRole());
        this.bob = this.createUser(roles, "Bob", "Admin", "bob@email.com");
    }

    @Given("^that the default admin is logged in$")
    public void thatTheDefaultAdminIsLoggedIn() throws FailedToLoginException {
        defaultAdminDaniel = TestState.getInstance().getFakeClient().loginMadeUpUser(defaultAdminDaniel, this.sharedPassword);
        Assert.assertTrue(defaultAdminDaniel.getToken().length() > 0);
        Assert.assertTrue(defaultAdminDaniel.isDefaultAdmin());
    }

    @Given("^the admin is logged in$")
    public void theAdminIsLoggedIn() throws FailedToLoginException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        adminAnna = fakeClient.loginMadeUpUser(adminAnna, this.sharedPassword);
        Assert.assertTrue(adminAnna.getToken().length() > 0);
    }

    @Given("^the two regular users are logged in$")
    public void theTwoRegularUsersAreLoggedIn() throws FailedToLoginException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        alice = fakeClient.loginMadeUpUser(alice, this.sharedPassword);
        bob = fakeClient.loginMadeUpUser(bob, this.sharedPassword);
        Assert.assertTrue(alice.getToken().length() > 0);
        Assert.assertTrue(bob.getToken().length() > 0);
    }

    @When("^the admin tries to delete the regular user Alice$")
    public void theAdminTriesToDeleteTheRegularUserAlice() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + alice.getUserId(), this.adminAnna.getToken());
        JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
        Assert.assertEquals(200, res.status());
    }

    @Then("^the regular user Alice should be deleted$")
    public void theRegularUserAliceShouldBeDeleted() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + alice.getUserId(), this.adminAnna.getToken());
        JsonNode resAsJson = PlayResultToJson.convertResultToJson(res);
        Assert.assertEquals(404, res.status());
    }

    @When("the admin tries to delete the default admin")
    public void theAdminTriesToDeleteTheDefaultAdmin() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.adminAnna.getToken());
        Assert.assertEquals(401, res.status());
    }

    @Then("the default admin should not be deleted")
    public void theDefaultAdminShouldNotBeDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
        Assert.assertEquals(200, res.status());
    }

    @When("^the regular user Alice tries to delete the other regular user Bob$")
    public void theRegularUserAliceTriesToDeleteTheOtherRegularUserBob() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(401, res.status());
    }

    @Then("^the regular user Bob should not be deleted$")
    public void theRegularUserBobShouldNotBeDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(200, res.status());
    }

    @When("^a regular user Bob tries to delete their own profile$")
    public void aRegularUserBobTriesToDeleteTheirOwnProfile() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + bob.getUserId(), this.bob.getToken());
        Assert.assertEquals(200, res.status());
    }

    @Then("^the regular user Bob should be deleted$")
    public void theRegularUserBobShouldBeDeleted() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("GET", "/api/users/" + bob.getUserId(), this.alice.getToken());
        Assert.assertEquals(404, res.status());
    }

    @When("the default admin tries to delete their own profile")
    public void theDefaultAdminTriesToDeleteTheirOwnProfile() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.defaultAdminDaniel.getToken());
        Assert.assertEquals(401, res.status());
    }

    @When("^a regular user Bob tries to delete the default admin$")
    public void aRegularUserBobTriesToDeleteTheDefaultAdmin() throws Throwable {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result res = fakeClient.makeRequestWithToken("DELETE", "/api/users/" + defaultAdminDaniel.getUserId(), this.bob.getToken());
        Assert.assertEquals(401, res.status());
    }

    @When("^someone tries to delete a user that does not exist$")
    public void someoneTriesToDeleteAUserThatDoesNotExist() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
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
