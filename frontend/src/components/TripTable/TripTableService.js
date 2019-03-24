import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Gets all destinations
 */
export async function getDestinations() {
  const res = await superagent.get(endpoint("/destinations"));
  return res.body;
}