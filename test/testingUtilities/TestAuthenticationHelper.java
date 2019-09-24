package testingUtilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.FailedToLoginException;
import exceptions.FailedToSignUpException;
import exceptions.ServerErrorException;
import io.cucumber.datatable.DataTable;
import models.Role;
import models.RoleType;
import models.User;
import org.junit.Assert;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

/**
 * Used to help with authentication when testing.
 */
public class TestAuthenticationHelper {


    /**
     * A helper method for creating and authenticating a user for running tests.
     * The user object is placed in the TestState for retrieval by multiple test classes.
     *
     * @param dataTable a DataTable with the users details
     */
    public static void theFollowingUsersExists(DataTable dataTable) {
        TestState testState = TestState.getInstance();
        List<Map<String, String>> list = dataTable.asMaps(String.class, String.class);
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            String plainTextPassword = row.get("password");

            // sign up a user
            JsonNode signUpReqBody = Json.toJson(row);
            try {
                User user = testState.getFakeClient().signUpUser(signUpReqBody);
                Assert.assertNotEquals(0, user.getUserId());

                user = testState.getFakeClient().loginMadeUpUser(user, plainTextPassword);
                if (row.get("isAdmin") != null && row.get("isAdmin").equals("true")) {
                    List<Role> roles = new ArrayList<>();
                    Role adminRole = Role.find.query().where().eq("role_type", RoleType.ADMIN.toString()).findOne();
                    roles.add(adminRole);
                    user.setRoles(roles);
                    user.save();
                }

                Assert.assertNotEquals("", user.getToken());
                testState.addUser(user);
            } catch (IOException | FailedToSignUpException | ServerErrorException | FailedToLoginException e) {
                e.printStackTrace();
                Assert.fail(Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
