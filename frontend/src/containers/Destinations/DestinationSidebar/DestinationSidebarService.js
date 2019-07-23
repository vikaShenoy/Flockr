import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Get a list of all destinations (private and public) destinations
 * @returns {Promise<Array>} the users public and private destinations
 */
export async function getYourDestinations() {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/destinations`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}