package actors;

import akka.actor.ActorRef;
import models.Trip;
import models.User;

import java.util.Map;

/**
 * Notifies users of any changes to a trip (trip name, trip destinations or messages)
 */
public class TripNotifier {

    /**
     * Notifies all peers that are currently connected about trip updates
     * @param userThatEdited The user that edited the trip
     * @param trip The trip that was edited
     * @param connectedUsers The map of currently connected users
     */
    public void notifyTripUpdate(User userThatEdited, Trip trip, Map<ActorRef, User> connectedUsers) {
        for (ActorRef actorRef : connectedUsers.keySet()) {
            User currentUser = connectedUsers.get(actorRef);

            /* For now, just send to current user that is not the user that has made the change
               until we have trip groups
             */

            if (currentUser.equals(userThatEdited)) {
                actorRef.tell("update", actorRef);
            }

        }
    }
}
