package controllers.TripControllerTests;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TripControllerTestUtil {

    /**
     * Helper function for giving the test a user a trip role.
     * @param user user to set role for.
     * @param trip the trip to set role for.
     * @param roleType role to give the test user.
     */
    static void setUserTripRole(User user, TripComposite trip, RoleType roleType) {
        List<User> tripUsers = new ArrayList<>();
        List<UserRole> tripUserRoles = new ArrayList<>();
        tripUsers.add(user);
        trip.setUsers(tripUsers);
        Role userTripRole = Role.find.query().where().eq("role_type", roleType.toString()).findOne();
        UserRole userRole = new UserRole(user, userTripRole);
        userRole.save();
        tripUserRoles.add(userRole);
        trip.setUserRoles(tripUserRoles);
        trip.save();
    }

    static ObjectNode createTripJsonBody(String name, List<ObjectNode> tripNodes, Map<User, String> users) {
        ObjectNode body = Json.newObject();
        ArrayNode userJson = Json.newArray();
        ArrayNode tripNodeJson = Json.newArray();
        tripNodes.forEach(tripNodeJson::add);

        for (Map.Entry<User, String> user : users.entrySet()) {
            ObjectNode userObject = Json.newObject();
            userObject.put("userId", user.getKey().getUserId());
            userObject.put("role", user.getValue());
            userJson.add(userObject);
        }
        body.put("name", name);
        body.putArray("userIds").addAll(userJson);
        body.putArray("tripNodes").addAll(tripNodeJson);

        return body;
    }
}
