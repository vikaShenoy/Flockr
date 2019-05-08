import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Send a request to the backend server to log out the current logged in user,
 * by passing their auth token which will be set to null.
 */
export function logout() {
  return superagent.post(endpoint("/auth/travellers/logout"))
  .set("Authorization", localStorage.getItem("authToken"));
}