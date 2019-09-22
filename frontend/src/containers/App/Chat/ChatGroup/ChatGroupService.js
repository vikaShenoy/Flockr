import superagent from "superagent";
import { endpoint } from "../../../../utils/endpoint";

/**
 * Sends a message to a group chat
 *
 * @param {Number} chatGroupId the id of the chat group.
 * @param {string} message The message to send.
 * @return {Object}
 */
export async function sendMessage(chatGroupId, message) {
  const sendMessageUrl = `/chats/${chatGroupId}/message`;
  const res = await superagent.post(endpoint(sendMessageUrl))
    .send({ message })
    .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Gets messages in a chat
 *
 * @param {Number} chatGroupId The chat group to get the messages from
 * @param {Number} offset the index to start results from.
 * @param {Number} limit the max number of messages to get.
 * @returns {Array<Object>} The array of chat messages
 */
export async function getChatMessages(chatGroupId, offset, limit) {
  let getMessagesUrl = `/chats/${chatGroupId}/messages`;
  if (offset && limit) {
      getMessagesUrl += `?offset=${offset}&limit=${limit}`
  }
  const res = await superagent.get(endpoint(getMessagesUrl))
    .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get all the connected users for a specific chat group.
 * @param {Number} chatGroupId the id of the chat group
 */
export async function getConnectedUsers(chatGroupId) {
  const authToken = localStorage.getItem('authToken');
  const res = await superagent.get(endpoint(`/chats/${chatGroupId}/onlineUsers`))
    .set("Authorization", authToken);

  return res.body;
}