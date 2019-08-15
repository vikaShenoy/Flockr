package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import models.Trip;
import models.User;
import modules.websocket.frames.ConnectedFrame;
import modules.websocket.frames.DisconnectedFrame;
import modules.websocket.frames.Frame;
import play.libs.Json;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Notifies users when user's disconnect and connect
 */
public class ConnectionStatusNotifier {


  private ConnectedUsers connectedUsers;
  private Map<User, ActorRef> userMap;

  public ConnectionStatusNotifier() {
    this.connectedUsers = ConnectedUsers.getInstance();
    this.userMap = this.connectedUsers.getConnectedUsers();
  }

     /**
   * Notifies that a user has been connected to all to all users that are going
   * on a trip with the user
   * @param user The user that is connected
   * @param trips The trips that the user is apart of
   */
  public void notifyConnectedUser(User user, List<Trip> trips) {
    notifyConnectionStatus(user, trips, ConnectionStatus.CONNECTED);
  }

  /**
   * Notifies that a user has either connected or disconnected to all users that they share a trip
   * with
   * @param user The user that connected or disconnected
   * @param trips The trip that the user is apart of
   */
  public void notifyConnectionStatus(User user, List<Trip> trips, ConnectionStatus connectionStatus) {
   Set<User> usersToNotify = new HashSet<>();

    for (Trip trip : trips) {
      for (User currentUser: trip.getUsers()) {
        if (!user.equals(currentUser)) {
          usersToNotify.add(currentUser);
        }
      }
    }


    ActorRef userWebsocket = userMap.get(user);
    JsonNode userJson = Json.toJson(user);

    for (User userToNofiy : usersToNotify) {
      ActorRef receiverWebsocket = userMap.get(userToNofiy);

      if (connectionStatus == ConnectionStatus.CONNECTED) {
          JsonNode frameJson = Json.toJson(new ConnectedFrame(user));
          receiverWebsocket.tell(frameJson.toString(), userWebsocket);
      } else {
          JsonNode frameJson = Json.toJson(new DisconnectedFrame(user));
          receiverWebsocket.tell(frameJson.toString(), userWebsocket);
      }
    }
  }

  /**
   * Notify that a user has been disconnected to all users that are going
   * on a trip with the user
   * @param user The user that disconnected
   * @param trips The trips that the user is apart of
   */
  public void notifyDisconnectedUser(User user, List<Trip> trips) {
    notifyConnectionStatus(user, trips, ConnectionStatus.DISCONNECTED);
  }
}
