import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Send a request to the internal controller to resample the database.
 */
export async function resample() {
  return superagent.post(endpoint("/internal/resample"))
}