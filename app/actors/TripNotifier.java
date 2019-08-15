package actors;

import akka.actor.ActorRef;
import models.Trip;
import models.User;

import java.util.Map;

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

    /*for (ActorRef actorRef : connectedUsers.keySet()) {
        User currentUser = connectedUsers.get(actorRef);

        /* For now, just send to current user that is not the user that has made the change
           until we have trip groups


        if (currentUser.equals(userThatEdited)) {
            actorRef.tell("update", actorRef);
        }

    }*/

    System.out.println("I am here");
    System.out.println(userMap.size());
    for (User user : userMap.keySet()) {
      System.out.println(user);
    }

    for (User user : trip.getUsers()) {
      System.out.println("Does userMap contain " + user.getFirstName() + "? :" + userMap.containsKey(user));
      System.out.println(!user.equals(userThatEdited) && userMap.containsKey(user));
      if (!user.equals(userThatEdited) && userMap.containsKey(user)) {
        ActorRef actorRef = userMap.get(user);
        System.out.println("Attempting to message " + user.getFirstName());
        actorRef.tell("update", actorRef);
      }
    }
  }
}
