import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

export function logout() {
  return superagent.post(endpoint("/auth/travellers/logout"))
  .set("Authorization", localStorage.getItem("authToken"));
}