package modules.websocket;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import models.TripComposite;
import models.User;
import modules.websocket.frames.TripUpdatedFrame;
import play.libs.Json;

/** Notifies users of any changes to a trip (trip name, trip destinations or messages) */
public class TripNotifier {

  private ConnectedUsers connectedUsers;

  public TripNotifier() {
    this.connectedUsers = ConnectedUsers.getInstance();

  }

  /**
   * Notifies all peers that are currently connected about trip updates
   *
   * @param userThatEdited The user that edited the trip
   * @param trip The trip that was edited
   */
  public void notifyTripUpdate(User userThatEdited, TripComposite trip) {
    for (User user : trip.getUsers()) {
      if (!user.equals(userThatEdited) && connectedUsers.isUserConnected(user)) {
        ActorRef actorRef = connectedUsers.getSocketForUser(user);
        JsonNode frameJson = Json.toJson(new TripUpdatedFrame(trip));

        actorRef.tell(frameJson.toString(), actorRef);
      }
    }
  }
}
