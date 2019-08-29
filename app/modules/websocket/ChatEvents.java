package modules.websocket;


import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import models.ChatGroup;
import models.User;
import modules.websocket.frames.*;
import play.libs.Json;

/**
 * A class for sending chat messages to chat groups.
 */
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

    for (User currentUser: groupUsers) {
      if (connectedUsers.isUserConnected(currentUser) && !user.equals(currentUser)) {
        ActorRef receiverWebsocket = connectedUsers.getSocketForUser(currentUser);
        JsonNode frameJson = Json.toJson(frame);
        receiverWebsocket.tell(frameJson.toString(), userWebsocket);
      }
    }
  }


  /**
   * Broadcast a silent message to other users in a chat with a user who is going offline
   * @param userDisconnecting the user who is disconnecting
   */
  public void notifyDisconnect(User userDisconnecting, List<ChatGroup> chatGroups) {
    ActorRef userSocket = connectedUsers.getSocketForUser(userDisconnecting);
    Frame frame = new DisconnectedFrame(userDisconnecting);

    for (ChatGroup group : chatGroups) {
      for (User currentUser : group.getUsers()) {
        if (connectedUsers.isUserConnected(currentUser) && !userDisconnecting.equals(currentUser)) {
          ActorRef receiverWebsocket = connectedUsers.getSocketForUser(currentUser);
          JsonNode frameJson = Json.toJson(frame);
          receiverWebsocket.tell(frameJson.toString(), userSocket);
        }
      }
    }
  }

}
