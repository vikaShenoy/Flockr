package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import models.TripComposite;
// import models.TripComposite;
import models.TripComposite;
import models.User;
import modules.websocket.frames.ConnectedFrame;
import modules.websocket.frames.PingMapFrame;
import play.libs.Json;

/**
 * Notifies users of a trip when a map ping event is triggered.
 */
public class PingMapNotifier {
  private ConnectedUsers connectedUsers;
  private PingMapFrame pingMapFrame;
  private TripComposite tripNode;

  public PingMapNotifier(PingMapFrame pingMapFrame, TripComposite tripNode) {
    this.connectedUsers = ConnectedUsers.getInstance();
    this.pingMapFrame = pingMapFrame;
    this.tripNode = tripNode;
  }

  /**
   * Notifies all users on the trip of the map ping event.
   *
   * @param user the user that the ping event originated from.
   */
  public void notifyUsers(User user) {
    Set<User> usersToNotify = getUsersToNotify(user);

    ActorRef userWebSocket = connectedUsers.getSocketForUser(user);
    JsonNode userJson = Json.toJson(user);

    for (User userToNotify : usersToNotify) {
      ActorRef receiverWebSocket = this.connectedUsers.getSocketForUser(userToNotify);

      JsonNode frameJson = Json.toJson(pingMapFrame);
      receiverWebSocket.tell(frameJson.toString(), userWebSocket);
    }
  }

  /**
   * Gets a set containing users to notify of this map ping event.
   *
   * @return a set of the users to notify.
   */
  private Set<User> getUsersToNotify(User user) {
    Set<User> usersToNotify = new HashSet<>();

    for (User currentUser : this.tripNode.getUsers()) {

      if (this.connectedUsers.isUserConnected(currentUser) && !user.equals(currentUser)) {
        usersToNotify.add(currentUser);
      }
    }
    return usersToNotify;
  }
}
