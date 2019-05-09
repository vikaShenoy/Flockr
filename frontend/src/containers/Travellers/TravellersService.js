import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Get a list of travellers.
 * @return json data for all travellers.
 */
export async function getTravellers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/users"))
    .set("Authorization", authToken);
  return res.body;
}