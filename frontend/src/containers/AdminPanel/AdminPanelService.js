import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Make API call to delete the given users
 * @param {number[]} userIds the ids of the users we want to delete
 */
export async function deleteUsers(userIds) {
  const authToken = localStorage.getItem("authToken");
  const deleteUserPromises = userIds.map(userId => {
    const promise = superagent.delete(endpoint(`/users/${userId}`))
    .set("Authorization", authToken);
    return promise;
  });

  await Promise.all(deleteUserPromises);
}

/**
 * Undeletes users
 */
export async function undoDeleteUsers(userIds) {
  const authToken = localStorage.getItem("authToken");

  const undoDeleteUserPromises = userIds.map(userId => {
    const promise = superagent
      .put(endpoint(`/users/${userId}/undodelete`))
      .set("Authorization", authToken);
    return promise;
  });

  await Promise.all(undoDeleteUserPromises);
}

/**
 * Get user data for users, with a complete profile.
 * Allows admin to edit users.
 */
export async function getUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/users")).set("Authorization", authToken);
  return res.body;
}

/**
 * Get user data for all users, including those without a complete profile.
 * Used so admin can edit signed up users they created.
 */
export async function getAllUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/users/all")).set("Authorization", authToken);
  return res.body;
}

export async function patchUser(userId, body) {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent
    .patch(endpoint(`/users/${userId}`))
    .set("Authorization", authToken)
    .send(body);
  if (body.roles) {
    const res2 = await superagent
      .patch(endpoint(`/users/${userId}/roles`))
      .set("Authorization", authToken)
      .send(body);
  }
}

/**
 * Updates a user's roles
 * @param {number} selectedUserId userId of roles to update
 * @param {Array<string>} roleTypes roleType for user to have
 */
export async function updateRoles(selectedUserId, roleTypes) {
  const authToken = localStorage.getItem("authToken");
  await superagent
    .patch(endpoint(`/users/${selectedUserId}/roles`))
    .send({
      roles: roleTypes
    })
    .set("Authorization", authToken);
}

