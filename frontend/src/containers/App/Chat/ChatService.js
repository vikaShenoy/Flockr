import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Gets all users that match the given search parameter
 * @param name or partial name to search for
 * @returns {Array} Returns a list of users
 */
export async function getUsers(name) {
    const authToken = localStorage.getItem("authToken");
    const res = await superagent.get(endpoint(`/users/search`)).query({name: name})
        .set("Authorization", authToken);

    return res.body;
}

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

/**
 * Send a request to backend to edit a group chat.
 * @param chatId id of the chat to edit.
 * @param chatName group chat name.
 * @param userIds ids of the users who are in the group chat.
 * @returns {Promise<*>} response from put request.
 */
export async function editChat(chatId, chatName, userIds) {
  const token = localStorage.getItem("authToken");
  const res = await superagent.put(endpoint(`/chats/${chatId}`))
      .set("Authorization", token)
      .send({
        name: chatName,
        userIds: userIds
      });
  return res.body;
}

/**
 * Send a request to the backend to delete a chat group.
 * @param chatGroupId id of the chat to delete.
 * @returns {Promise<*>} response from server.
 */
export async function deleteChat(chatGroupId) {
  const token = localStorage.getItem("authToken");
  const res = await superagent.delete(endpoint(`/chats/${chatGroupId}`))
      .set("Authorization", token);
  return res.body;
}