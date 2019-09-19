import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";


/**
 * Deletes a user
 * @param {number} userId The ID of the user to delete
 */
export async function deleteUser(userId) {
    const authToken = localStorage.getItem("authToken");
    await superagent.delete(endpoint(`/users/${userId}`))
    .set("Authorization", authToken);
}
/**
 * Make API call to delete the given users
 * @param {number[]} userIds the ids of the users we want to delete
 */
export async function deleteUsers(userIds) {
  const deleteUserPromises = userIds.map(userId => deleteUser(userId));
  await Promise.all(deleteUserPromises);
}

/**
 * Undoes the deletion of a user
 * @param {number} userId the ID of the user to undelete
 */
export async function undoDeleteUser(userId) {
  const authToken = localStorage.getItem("authToken");
    await superagent
      .put(endpoint(`/users/${userId}/undodelete`))
      .set("Authorization", authToken);
}

/**
 * Un-deletes users (soft delete functionality).
 */
export async function undoDeleteUsers(userIds) {
  const undoDeleteUserPromises = userIds.map(userId => undoDeleteUser(userId));
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

/**
 * Get users from the search endpoint with offset for lazy loading.
 * @param queries query params like offset, limit, search terms.
 * @returns {Promise<Array>} list of users.
 */
export async function getMoreUsers(queries) {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/users/search")).
      query(queries).
      set("Authorization", authToken);
  return res.body;

}


/**
 * Patch user to edit their details.
 * @param userId user to edit.
 * @param body new user data.
 * @returns {Promise<void>}
 */
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

