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

/**
 * A class for sending chat messages to chat groups.
 */
public class ChatEvents {


  private ConnectedUsers connectedUsers;
  private Map<User, ActorRef> userMap;

  public ChatEvents() {
    this.connectedUsers = ConnectedUsers.getInstance();
    this.userMap = this.connectedUsers.getConnectedUsers();
  }


  /**
   * Sends a message to all members of a chat group that are currently online.
   *
   * @param user the user sending the message.
   * @param group the group to send the message to.
   * @param message the message to be sent.
   */
  public void sendMessageToChatGroup(User user, ChatGroup group, String message) {
    Frame frame = new ChatMessageFrame(group.getChatGroupId(), message, user.getUserId());

    List<User> groupUsers = group.getUsers();
    ActorRef userWebsocket = userMap.get(user);

    for (User currentUser: groupUsers) {
      if (connectedUsers.isUserConnected(currentUser) && !user.equals(currentUser)) {
        ActorRef receiverWebsocket = userMap.get(currentUser);
        JsonNode frameJson = Json.toJson(frame);
        receiverWebsocket.tell(frameJson.toString(), userWebsocket);
      }
    }
  }

}
