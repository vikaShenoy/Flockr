// import axios from "axios";

import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";


/**
 * Signs up a user
 * @param {Object} user - The user to sign up
 * @param {string} user.firstName - The users first name
 * @param {string} user.lastName - The users last name
 * @param {string} user.email - The users email
 * @param {string} user.password - The users password
 * @returns {Promise<void>} - A Promise that doesn't doesn't resolve to anything
 * @throws Error if status was not 201
 */
export async function signup(user) {
  const response = await superagent.post(endpoint("/auth/travellers/signup"))
  .send(user);
  return response.body
}

/**
 * Checks if the users email is already taken
 * @param {string} email  - The users email
 * @returns {Promise<boolean>} True if the email is taken, false otherwise
 */
export async function emailTaken(email) {
  try {
    const res = await superagent.get(endpoint(`/auth/travellers/${email}/available`));
  } catch (e) {
    return true;
  }

  return false;
}
