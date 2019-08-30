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

/**
 * Get the chat with the specified users
 * @param {Array<Number>} users an array of user ids in the chat
 * @returns {Promise<Object>} the trip with the specified users, if it exists
 */
export async function getChatWithUsers(users) {
  const chats = await getChats();
  const chatsWithUsers = chats.filter(chat => chat.users.every(user => users.includes(user.userId)));

  if (chatsWithUsers.length === 0) {
    return Promise.reject("There are no chats with those users in it");
  } else {
    return chatsWithUsers[0];
  }
}

/**
 * Create a chat with the specified users
 * @param {Array<Number>} userIds the ids of the users to include in the group chat **exclude the id of the user
 * creating the chat**
 * @param {String} chatName the name of the chat
 * @returns {Promise<Object>} the created chat
 */
export async function createChat(userIds, chatName) {
  const token = localStorage.getItem("authToken");
  const res = await superagent.post(endpoint("/chats"))
      .send({
        name: chatName,
        userIds: userIds
      })
      .set("Authorization", token);
  return res.body;
}