package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import models.*;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.Security;
import utils.FakeClient;
import utils.PlayResultToJson;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static play.test.Helpers.route;

public class AdminSteps {

    // data
    private int userId;
    private String authToken;
    private ArrayNode roles;
    private int roleId;
    private int otherUserId;

    @Given("A traveller user exists")
    public void a_traveller_user_exists() {

        List<Role> roles = Role.find.query().where().eq("role_type", RoleType.TRAVELLER.toString()).findList();
        this.roleId = roles.get(0).getRoleId();

        Assert.assertNotEquals(0, this.roleId);

        Assert.assertEquals("TRAVELLER", roles.get(0).getRoleType().toString());
        Assert.assertEquals(1, roles.size());

        createUser(roles, false);

        Assert.assertNotEquals(0, this.userId);
    }

    @Given("An admin user exists")
    public void an_admin_user_exists() {

        Role role = Role.find.query().where().eq("role_type", RoleType.ADMIN.toString()).findOne();
        Assert.assertNotNull(role);

        this.roleId = role.getRoleId();
        Assert.assertNotEquals(0, this.roleId);

        List<Role> roles = Role.find.query().where().eq("role_type", RoleType.ADMIN.toString()).findList();
        createUser(roles, false);

        Assert.assertNotEquals(0, this.userId);
    }

    @Given("An admin user and another user exists")
    public void an_admin_user_and_another_user_exists() {

        List<Role> travellerList = Role.find.query().where().eq("role_type", RoleType.TRAVELLER.toString()).findList();
        List<Role> adminList = Role.find.query().where().eq("role_type", RoleType.ADMIN.toString()).findList();
        this.roleId = travellerList.get(0).getRoleId();

        Assert.assertNotEquals(0, adminList.get(0).getRoleId());
        Assert.assertNotEquals(0, this.roleId);

        this.otherUserId = createUser(travellerList, true);
        createUser(adminList, false);

        Assert.assertNotEquals(0, this.otherUserId);
        Assert.assertNotEquals(0, this.userId);
    }

    /**
     * A helper function to create a user with the roles in the list given.
     * @param roles List&ltRole&gt list of roles the user will have.
     */
    private int createUser(List<Role> roles, Boolean otherUser) {

        Nationality nationality = new Nationality("New Zealand");
        nationality.save();

        List<Nationality> nationalities = Nationality.find.query().findList();

        TravellerType travellerType = new TravellerType("adventurer");
        travellerType.save();

        List<TravellerType> travellerTypes = TravellerType.find.query().findList();

        Passport passport = new Passport("New Zealand");
        passport.save();

        List<Passport> passports = Passport.find.query().findList();

        Date date = new Date();

        Security security = new Security();
        String password = security.hashPassword("password");

        User user;

        if (otherUser) {
            user = new User("traveller", "", "user", password, "male", "other-user@travelEA.com", nationalities, travellerTypes, date, passports, roles, "");
        } else {
            user = new User("traveller", "", "user", password, "male", "user@travelEA.com", nationalities, travellerTypes, date, passports, roles, "");
        }

        user.save();

        this.userId = user.getUserId();

        return user.getUserId();
    }

    @And("The user is logged in")
    public void the_user_is_logged_in() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        ObjectNode reqJsonBody = Json.newObject();
        reqJsonBody.put("email", "user@travelEA.com");
        reqJsonBody.put("password", "password");

        Result loginResult = fakeClient.makeRequestWithNoToken("POST", reqJsonBody, "/api/auth/users/login");

        JsonNode authenticationResponseAsJson = PlayResultToJson.convertResultToJson(loginResult);

        Assert.assertEquals(200, loginResult.status());

        this.authToken = authenticationResponseAsJson.get("token").asText();

        Assert.assertNotNull(this.authToken);
        Assert.assertTrue(this.authToken.length() > 0);
    }

    @When("The user requests its roles")
    public void the_user_requests_its_roles() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Result rolesResult = fakeClient.makeRequestWithToken("GET", "/api/users/" + this.userId + "/roles", this.authToken);
        this.roles =  (ArrayNode) PlayResultToJson.convertResultToJson(rolesResult);

        this.roles.get(0);

        Assert.assertEquals(200, rolesResult.status());
        Assert.assertEquals(1, this.roles.size());
    }

    @When("The user requests the roles of another user")
    public void the_user_requests_the_roles_of_another_user() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();

        Result rolesResult = fakeClient.makeRequestWithToken("GET", "/api/users/" + this.otherUserId + "/roles", this.authToken);
        this.roles =  (ArrayNode) PlayResultToJson.convertResultToJson(rolesResult);

        this.roles.get(0);

        Assert.assertEquals(200, rolesResult.status());
        Assert.assertEquals(1, this.roles.size());

    }

    @When("The user requests the roles available on the system")
    public void the_user_requests_the_roles_available_on_the_system() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result rolesResult = fakeClient.makeRequestWithToken("GET", "/api/users/roles", this.authToken);
        this.roles =  (ArrayNode) PlayResultToJson.convertResultToJson(rolesResult);

        this.roles.get(0);

        Assert.assertEquals(200, rolesResult.status());
        Assert.assertNotEquals(0, this.roles.size());
    }

    @Then("^A list of roles is returned containing the \"([^\"]*)\" role$")
    public void a_list_of_roles_is_returned_containing_the_role(String role) {
        Assert.assertEquals(this.roleId, this.roles.get(0).get("roleId").asInt());
        Assert.assertEquals(role, this.roles.get(0).get("roleType").asText());
    }

    @Then("A list of roles is returned containing all the roles")
    public void a_list_of_roles_is_returned_containing_all_the_roles() {
        List<Role> roles = Role.find.all();

        Assert.assertEquals(roles.size(), this.roles.size());

        for (int i = 0; i < roles.size(); i++) {
            Assert.assertEquals(roles.get(i).getRoleType().toString(), this.roles.get(i).get("roleType").asText());
        }
    }
}
