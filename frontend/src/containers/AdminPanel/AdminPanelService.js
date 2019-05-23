import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";


/**
 * Make API call to delete the given users
 * @param {number[]} userIds the ids of the users we want to delete
 */
export async function deleteUsers(userIds) {
    const authToken = localStorage.getItem("authToken");
    throw new Error('API call to delete give users to be implemented');
}
/**
 * Get user data for users, with a complete profile.
 * Allows admin to edit users.
 */
export async function getUsers() {
    const authToken = localStorage.getItem("authToken");
    const res = await superagent.get(endpoint("/users"))
        .set("Authorization", authToken);
    return res.body;
}

/**
 * Get user data for all users, including those without a complete profile.
 * Used so admin can edit signed up users they created.
 */
export async function getAllUsers() {
    const authToken = localStorage.getItem("authToken");
    const res = await superagent.get(endpoint("/users/all"))
        .set("Authorization", authToken);
    return res.body;
}

export async function patchUser(userId, body) {
    const authToken = localStorage.getItem("authToken");
    const res = await superagent.patch(endpoint(`/users/${userId}`))
        .set("Authorization", authToken)
        .send(body);
    if (body.roles) {
        const res2 = await superagent.patch(endpoint(`/users/${userId}/roles`))
            .set("Authorization", authToken)
            .send(body);
    }
}