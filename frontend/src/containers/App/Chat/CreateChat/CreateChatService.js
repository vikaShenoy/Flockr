import superagent from "superagent";
import { endpoint } from "../../../../utils/endpoint";

/**
 * Gets a list of all users.
 * @returns {Promise<*>} the JSON body containing all user objects.
 */
export async function getUsers() {
  const res = await superagent.get(endpoint("/users"))
    .set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

/**
 * Create a new group chat.
 * @param chatName name of the group chat.
 * @param userIds ids of the users to belong to the chat.
 * @returns {Promise<*>} response from backend.
 */
export async function createGroupChat(chatName, userIds) {
  const body = {
    "name": chatName,
    "userIds": userIds,
  };
  const res = await superagent.post(endpoint("/chats"))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(body);
  return res.body;
}