package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import models.Role;
import models.User;
import org.junit.Assert;
import play.libs.Json;
import play.mvc.Result;
import testingUtilities.FakeClient;
import testingUtilities.TestState;

/**
 * Test that the user roles feature works as expected.
 */
public class UserRoleSteps {

    // User data
    private User user;

    private List<String> userRoles;

    // Admin data
    private User admin;

    private String currentAuthToken;
    private int currentUserId;

    private int statusResult;

    @Given("ROLES - A user with the following info exists...")
    public void rAUserWithTheFollowingInformationExists(DataTable dataTable) throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);

        // Sign up a user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        try {
            this.user = fakeClient.signUpUser(signUpReqBody);

            // Get user id
            Assert.assertNotEquals(0, this.user.getUserId());
        } catch (FailedToSignUpException | ServerErrorException e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
        }
    }

    @Given("ROLES - An admin with the following info exists...")
    public void rAnAdminWithTheFollowingInformationExists(DataTable dataTable) {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);

        JsonNode signUpReqBody = Json.toJson(firstRow);
        try {
            this.admin = fakeClient.signUpUser(signUpReqBody);

            // Give the admin an admin role
            List<Role> adminRoles = Role.find.all();
            this.admin.setRoles(adminRoles);
            this.admin.update();
            Assert.assertNotEquals(0, this.admin.getUserId());
        } catch (IOException | ServerErrorException | FailedToSignUpException e) {
            Assert.fail(Arrays.toString(e.getStackTrace()));
        }
    }

    @Given("ROLES - The admin is logged in...")
    public void rTheAdminIsLoggedIn() {
        this.currentAuthToken = this.admin.getToken();
        this.currentUserId = this.admin.getUserId();
        Assert.assertNotEquals("", this.admin.getToken());
    }

    @Given("ROLES - the user is logged in...")
    public void rTheUserIsLoggedIn() {
        this.currentAuthToken = this.user.getToken();
        this.currentUserId = this.user.getUserId();
        Assert.assertNotEquals("", this.user.getToken());
    }

    @When("ROLES - An admin adds an admin role to a user")
    public void rAnAdminAddsAnAdminRoleToAUser() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<String> types = new ArrayList<>();
        types.add("ADMIN");
        JsonNode typesJson = Json.toJson(types);
        ObjectNode data = Json.newObject();
        data.set("roles", typesJson);
        Result roleResult =
            fakeClient.makeRequestWithToken(
                "PATCH", data, "/api/users/" + this.user.getUserId() + "/roles",
                this.currentAuthToken);
        Assert.assertEquals(200, roleResult.status());
    }

    @When("ROLES - A user adds an admin role to themselves")
    public void rAUserAddsAnAdminRoleToThemselves() {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        List<String> roles = new ArrayList<>();
        roles.add("ADMIN");
        JsonNode typesJson = Json.toJson(roles);
        ObjectNode data = Json.newObject();
        data.set("roles", typesJson);

        Result roleResult =
            fakeClient.makeRequestWithToken(
                "PATCH",
                data,
                "/api/users/" + this.admin.getUserId() + "/roles",
                this.currentAuthToken);
        this.statusResult = roleResult.status();
    }

    @When("ROLES - I request roles from the database")
    public void rIRequestRolesFromTheDatabase() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        Result roleResult =
            fakeClient.makeRequestWithToken(
                "GET", "/api/users/" + this.user.getUserId() + "/roles", this.currentAuthToken);
        Assert.assertEquals(200, roleResult.status());

        // Save the response to be tested in the last step
        JsonNode roleBody = testingUtilities.PlayResultToJson.convertResultToJson(roleResult);
        this.userRoles = new ArrayList<>();
        for (JsonNode role : roleBody) {
            this.userRoles.add(role.get("roleType").asText());
        }
    }

    @Then("ROLES - The user has an admin role")
    public void the_user_has_role() {
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ADMIN");
        Assert.assertEquals(expected, this.userRoles);
    }

    @Then("ROLES - I receive a {int} status code")
    public void roles_I_receive_a_status_code(Integer int1) {
        Assert.assertEquals(403, statusResult);
    }
}
