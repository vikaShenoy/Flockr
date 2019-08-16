package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Trip;
import models.User;
import modules.websocket.frames.ConnectedFrame;
import modules.websocket.frames.DisconnectedFrame;
import modules.websocket.frames.Frame;
import modules.websocket.frames.TripUpdatedFrame;
import play.libs.Json;

import java.util.*;

enum ConnectionStatus {
  CONNECTED,
  DISCONNECTED
};

/** Notifies users of any changes to a trip (trip name, trip destinations or messages) */
public class TripNotifier {

  private ConnectedUsers connectedUsers;
  private Map<User, ActorRef> userMap;

  public TripNotifier() {
    this.connectedUsers = ConnectedUsers.getInstance();
    this.userMap = this.connectedUsers.getConnectedUsers();

  }

  /**
   * Notifies all peers that are currently connected about trip updates
   *
   * @param userThatEdited The user that edited the trip
   * @param trip The trip that was edited
   */
  public void notifyTripUpdate(User userThatEdited, Trip trip) {

    for (User user : trip.getUsers()) {
      if (!user.equals(userThatEdited) && userMap.containsKey(user)) {
        ActorRef actorRef = userMap.get(user);
        JsonNode frameJson = Json.toJson(new TripUpdatedFrame(trip));
        actorRef.tell(frameJson.toString(), actorRef);
      }
    }
  }
}
