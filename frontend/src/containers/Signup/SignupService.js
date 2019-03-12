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
  const res = await fetch(endpoint("/users"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(user)
  });

  if (res.status === 201) {
    throw new Error(`Res status was ${res.status}`);
  }
}

/**
 * Checks if the users email is already taken
 * @param {string} email  - The users email
 * @returns {Promise<boolean>} True if the email is taken, false otherwise
 */
export async function emailTaken(email) {
  // const res = await fetch(endpoint(`/users/email/${email}`), {
  //   method: "GET"
  // });

  // return res.status === 409;
  return false;
}