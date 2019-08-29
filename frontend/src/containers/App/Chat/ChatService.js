import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";


/**
 * Gets all chats that are associated with a user
 */
export async function getChats() {
  const res = await superagent.get(endpoint("/chats"))
    .set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}