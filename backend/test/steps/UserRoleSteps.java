package steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import controllers.InternalController;
import cucumber.api.java.Before;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.Role;
import models.RoleType;
import models.User;

import org.junit.Assert;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static play.mvc.Http.HttpVerbs.PATCH;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

public class UserRoleSteps {

    @Inject
    private Application application;


    private JsonNode userData;
    private Result result;

    // User data
    private String firstName;
    private String middleName;
    private String lastName;
    private String userEmail;
    private String userPassword;
    private String userAuthToken;
    private int userId;

    private List<String> userRoles;

    // Admin data
    private String adminAuthToken;
    private int adminUserId;
    private String adminEmail;
    private String adminPassword;

    private String currentAuthToken;

    private int statusResult;

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
    }

    @Given("ROLES - A user with the following info exists...")
    public void raUserWithTheFollowingInformationExists(DataTable dataTable) throws IOException {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.userEmail = firstRow.get("email");
        this.userPassword = firstRow.get("password");

        // Sign up a user
        JsonNode signUpReqBody = Json.toJson(firstRow);
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);

        // Get user id
        JsonNode signUpResBody = utils.PlayResultToJson.convertResultToJson(signUpRes);
        this.userId = signUpResBody.get("userId").asInt();
        Assert.assertEquals(201, signUpRes.status());
        Assert.assertNotEquals(0, this.userId);
    }

    @Given("ROLES - An admin with the following info exists...")
    public void ranAdminWithTheFollowingInformationExists(DataTable dataTable) throws IOException {
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        Map<String, String> firstRow = list.get(0);
        this.adminEmail = firstRow.get("email");
        this.adminPassword = firstRow.get("password");

        // Sign up another user (will be an admin)
        JsonNode signUpReqBody = Json.toJson(firstRow);
        Http.RequestBuilder signUpReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/signup")
                .bodyJson(signUpReqBody);
        Result signUpRes = route(application, signUpReq);
        Assert.assertEquals(201, signUpRes.status());
    }

    @Given("ROLES - The admin is logged in...")
    public void rthe_admin_is_logged_in() throws IOException {
        // Log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.adminEmail);
        logInReqBody.put("password", this.adminPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(logInReqBody);

        // Get auth token, user id
        Result logInRes = route(application, logInReq);
        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);
        System.out.println(logInResBody);
        this.adminAuthToken = logInResBody.get("token").asText();
        this.adminUserId = logInResBody.get("userId").asInt();
        this.currentAuthToken = adminAuthToken;

        // Give the admin an admin role
        User admin = User.find.byId(this.adminUserId);
        List<Role> adminRoles = Role.find.all();
        System.out.println("Admin roles are: "  + adminRoles);

        admin.setRoles(adminRoles);
        admin.save();


    }

    @Given("ROLES - the user is logged in...")
    public void rthe_user_is_logged_in() throws IOException {
        // Log in to get the auth token
        ObjectNode logInReqBody = Json.newObject();
        logInReqBody.put("email", this.userEmail);
        logInReqBody.put("password", this.userPassword);
        Http.RequestBuilder logInReq = Helpers.fakeRequest()
                .method("POST")
                .uri("/api/auth/users/login")
                .bodyJson(logInReqBody);

        // Get user token
        Result logInRes = route(application, logInReq);
        JsonNode logInResBody = utils.PlayResultToJson.convertResultToJson(logInRes);

        this.userAuthToken = logInResBody.get("token").asText();
        this.currentAuthToken = userAuthToken;

    }

    @When("ROLES - An admin adds an admin role to a user")
    public void ran_admin_adds_an_admin_role_to_a_user() {
        List<String> types = new ArrayList<>();
        types.add("ADMIN");
        JsonNode typesJson = Json.toJson(types);
        ObjectNode data = Json.newObject();
        data.set("roleTypes", typesJson);


        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PATCH)
                .uri("/api/users/" + this.userId + "/roles")
                .header("Authorization", this.currentAuthToken)
                .bodyJson(data);
        Result roleResult = route(application, request);
        Assert.assertEquals(200, roleResult.status());
    }

    @When("ROLES - A user adds an admin role to themselves")
    public void ra_user_adds_an_admin_role_to_themselves() {
        List<String> roles = new ArrayList<>();
        roles.add("ADMIN");
        JsonNode typesJson = Json.toJson(roles);
        ObjectNode data = Json.newObject();
        data.set("roleTypes", typesJson);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(PATCH)
                .uri("/api/users/" + this.userId + "/roles")
                .header("Authorization", this.currentAuthToken)
                .bodyJson(data);

        // Save status code for final step
        Result roleResult = route(application, request);
        this.statusResult = roleResult.status();
    }

    @When("ROLES - I request roles from the database")
    public void ri_request_roles_from_the_database() throws IOException  {
        // TODO - Can anyone request roles?
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/api/users/" + this.userId + "/roles")
                .header("Authorization", this.currentAuthToken);
        Result roleResult = route(application, request);
        Assert.assertEquals(200, (long) roleResult.status());

        // Save the response to be tested in the last step
        JsonNode roleBody = utils.PlayResultToJson.convertResultToJson(roleResult);
        this.userRoles = new ArrayList<>();
        for (JsonNode role : roleBody) {
            this.userRoles.add(role.get("roleType").asText());
        }
    }

    @Then("ROLES - The user has an admin role")
    public void rthe_user_has_role() {
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ADMIN");
        Assert.assertEquals(expected, this.userRoles);
    }

    @Then("ROLES - I receive a {int} status code")
    public void roles_I_receive_a_status_code(Integer int1) {
        System.out.println(this.userRoles);
        Assert.assertEquals(401, statusResult);
    }

    @Given("all roles are created")
    public void allRolesAreCreated() {
        Role admin = new Role(RoleType.ADMIN);
        Role superAdmin = new Role(RoleType.SUPER_ADMIN);
        Role traveller = new Role(RoleType.TRAVELLER);

        admin.save();
        superAdmin.save();
        traveller.save();
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
    }


}
