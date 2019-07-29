import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Gets all destinations
 */
export async function getDestinations() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/destinations")).set("Authorization", authToken);
  return res.body;
}

export async function getYourDestinations() {
  const authToken = localStorage.getItem("authToken");
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/destinations`))
    .set("Authorization", authToken);
  return res.body;
}