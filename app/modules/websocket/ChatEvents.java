package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import models.ChatGroup;
import models.Message;
import models.User;
import modules.websocket.frames.*;
import play.libs.Json;

/** A class for sending chat messages to chat groups. */
public class ChatEvents {

  private ConnectedUsers connectedUsers;

  public ChatEvents() {
    this.connectedUsers = ConnectedUsers.getInstance();
  }

  /**
   * Sends a message to all members of a chat group that are currently online.
   *
   * @param user the user sending the message.
   * @param group the group to send the message to.
   * @param message the message to be sent.
   */
  public void sendMessageToChatGroup(User user, ChatGroup group, String message, int messageId) {
    Frame frame = new ChatMessageFrame(group.getChatGroupId(), message, user, messageId);

    List<User> groupUsers = group.getUsers();
    ActorRef userWebsocket = connectedUsers.getSocketForUser(user);

    for (User currentUser : groupUsers) {
      if (connectedUsers.isUserConnected(currentUser) && !user.equals(currentUser)) {
        ActorRef receiverWebsocket = connectedUsers.getSocketForUser(currentUser);
        JsonNode frameJson = Json.toJson(frame);
        receiverWebsocket.tell(frameJson.toString(), userWebsocket);
      }
    }
  }

  /**
   * Broadcast a message to other users in a chat with a user who is going online
   *
   * @param userConnecting the user connecting.
   * @param chatGroups the chat groups for the user.
   */
  public void notifyConnect(User userConnecting, List<ChatGroup> chatGroups) {
    ActorRef userSocket = connectedUsers.getSocketForUser(userConnecting);
    Frame frame = new ConnectedFrame(userConnecting);

    notifyUsersFromChatGroups(userConnecting, chatGroups, frame);
  }

  /**
   * Notifies the users contained in a list of chat groups with a given frame.
   *
   * @param userNotifying the user sending the notification.
   * @param chatGroups the list of chat groups to extract users from.
   * @param frame the frame to send over the socket.
   */
  private void notifyUsersFromChatGroups(
      User userNotifying, List<ChatGroup> chatGroups, Frame frame) {
    Set<User> usersToNotify = new HashSet<>();
    ActorRef userSocket = connectedUsers.getSocketForUser(userNotifying);

    for (ChatGroup group : chatGroups) {
      for (User currentUser : group.getUsers()) {
        if (connectedUsers.isUserConnected(currentUser) && !userNotifying.equals(currentUser)) {
          usersToNotify.add(currentUser);
        }
      }
    }

    for (User user : usersToNotify) {
      ActorRef receiverWebSocket = connectedUsers.getSocketForUser(user);
      JsonNode frameJson = Json.toJson(frame);
      receiverWebSocket.tell(frameJson.toString(), userSocket);
    }
  }

  /**
   * Broadcast a message to other users in a chat with a user who is going offline
   *
   * @param userDisconnecting the user who is disconnecting
   */
  public void notifyDisconnect(User userDisconnecting, List<ChatGroup> chatGroups) {
    ActorRef userSocket = connectedUsers.getSocketForUser(userDisconnecting);
    Frame frame = new DisconnectedFrame(userDisconnecting);

    notifyUsersFromChatGroups(userDisconnecting, chatGroups, frame);
  }


  /**
   * Notifies the users contained in a list of chat groups with a given frame.
   *
   * @param userNotifying the user sending the notification.
   * @param chatGroup the chat group to extract users from.
   * @param frame the frame to send over the socket.
   */
  private void notifyUsersFromChatGroup(User userNotifying, ChatGroup chatGroup, Frame frame) {
    List<ChatGroup> chatGroups = new ArrayList<>();
    chatGroups.add(chatGroup);
    ActorRef socket = connectedUsers.getSocketForUser(userNotifying);
    notifyUsersFromChatGroups(userNotifying, chatGroups, frame);
  }


  /**
   * Broadcast a message to other users in a chat when a message has been deleted.
   *
   * @param user the user who deleted the message
   * @param message the message that was deleted
   */
  public void notifyChatMessageHasBeenDeleted(User user, Message message) {
    Frame frame = new DeletedChatMessageFrame(message);
    notifyUsersFromChatGroup(user, message.getChatGroup(), frame);
  }
}
