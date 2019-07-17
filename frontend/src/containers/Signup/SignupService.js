import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Sign up a user.
 * @param {Object} user - The user to sign up
 * @param {string} user.firstName - The users first name
 * @param {string} user.lastName - The users last name
 * @param {string} user.email - The users email
 * @param {string} user.password - The users password
 * @returns {Promise<void>} - A Promise that doesn't doesn't resolve to anything
 * @throws Error if status was not 201
 */
export async function signup(user) {
  const response = await superagent.post(endpoint("/auth/users/signup"))
  .send(user);
  return response.body
}

/**
 * Check if the users email is already taken.
 * @param {string} email  - The users email
 * @returns {Promise<boolean>} True if the email is taken, false otherwise
 */
export async function emailTaken(email) {
  try {
    const res = await superagent.get(endpoint(`/auth/users/${email}/available`));
  } catch (e) {
    return true;
  }

  return false;
}

export const rules = {
  required: field => !!field || "Field is required",
  noNumbers: field => !/\d/.test(field) || "No Numbers Allowed",
  nonEmptyArray: field => field.length > 0 || "Please select at least 1"
};

/**
 * Update the user's information
 * @param {Number | String} userId the id of the user
 * @param {Object} basicInfo object containing info being updated, as per API spec
 */
export function updateBasicInfo(userId, basicInfo) {
  return superagent.patch(endpoint(`/users/${userId}`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send(basicInfo);
}