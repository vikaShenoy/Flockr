package modules.websocket;

import akka.actor.ActorRef;
import models.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines all authenticated connected users that are connected via websockets
 */
public class ConnectedUsers {
    private static ConnectedUsers instance;
    private Map<User, ActorRef> connectedUsers;


    /**
     * Creates connected users
     */
    private ConnectedUsers() {
        connectedUsers = new HashMap<>();
    }

    /**
     * Singleton getter for connected users
     * @return
     */
    public static ConnectedUsers getInstance() {
        if (instance == null) {
            instance = new ConnectedUsers();
        }

        return instance;
    }


    /**
     * Adds a connected users
     * @param user The user to add
     * @param out The websocket object
     */
    public void addConnectedUser(User user, ActorRef out) {
        connectedUsers.put(user, out);
    }

    /**
     * Removes a connected users by
     * @param user The user object
     */
    public void removeConnectedUser(User user) {
        connectedUsers.remove(user);
    }

    public Map<User, ActorRef> getConnectedUsers() {
        return connectedUsers;
    }

    /**
     * Clears all connected users
     */
    public void clear() {
        connectedUsers.clear();
    }
}
