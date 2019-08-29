package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import models.ChatGroup;
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
  public void sendMessageToChatGroup(User user, ChatGroup group, String message) {
    Frame frame = new ChatMessageFrame(group.getChatGroupId(), message, user);

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
   * Broadcast a silent message to other users in a chat with a user who is going online
   *
   * @param userConnecting the user connecting.
   * @param chatGroups the chat groups for the user.
   */
  public void notifyConnect(User userConnecting, List<ChatGroup> chatGroups) {
    ActorRef userSocket = connectedUsers.getSocketForUser(userConnecting);
    Frame frame = new ConnectedFrame(userConnecting);

    notifyUsersFromChatGroups(userConnecting, chatGroups, userSocket, frame);
  }

  /**
   * Notifies the users contained in a list of chat groups with a given frame.
   *
   * @param userNotifying the user sending the notification.
   * @param chatGroups the list of chat groups to extract users from.
   * @param userSocket the socket of the connected user.
   * @param frame the frame to send over the socket.
   */
  private void notifyUsersFromChatGroups(
      User userNotifying, List<ChatGroup> chatGroups, ActorRef userSocket, Frame frame) {
    Set<User> usersToNotify = new HashSet<>();

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
   * Broadcast a silent message to other users in a chat with a user who is going offline
   *
   * @param userDisconnecting the user who is disconnecting
   */
  public void notifyDisconnect(User userDisconnecting, List<ChatGroup> chatGroups) {
    ActorRef userSocket = connectedUsers.getSocketForUser(userDisconnecting);
    Frame frame = new DisconnectedFrame(userDisconnecting);

    notifyUsersFromChatGroups(userDisconnecting, chatGroups, userSocket, frame);
  }
}
