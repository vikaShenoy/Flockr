import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Logs the user in
 * @param {string} email  - Users email
 * @param {string} password  - Users password
 * @throws Error if status code was not 200
 */ 
export async function login(email, password) {
  const res = await superagent.post(endpoint("/auth/users/login"))
  .send({
    email: email,
    password: password
  });
  return res.body;
}

