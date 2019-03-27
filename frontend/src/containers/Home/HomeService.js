import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

export async function resample() {
  return superagent.post(endpoint("/internal/resample"))
}