import { endpoint } from "../../utils/endpoint";

/**
 * Logs the user in
 * @param {string} email  - Users email
 * @param {string} password  - Users password
 * @returns {Promise<void>} - Promise which resolves to nothing
 * @throws Error if status code was not 200
 */ 
export async function login(email, password) {
  const res = await fetch(endpoint("/users/login"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({email, password})
  });  

  if (res.status !== 200) {
    throw new Error(`Res status was: ${res.status}`);
  }
}

