import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Get all destinations.
 */
export async function getDestinations() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/destinations")).set("Authorization", authToken);
  return res.body;
}

/**
 * Get a user's destinations.
 * @returns {Promise<*>} JSON response with destinations.
 */
export async function getYourDestinations() {
  const authToken = localStorage.getItem("authToken");
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/destinations`))
    .set("Authorization", authToken);
  return res.body;
}