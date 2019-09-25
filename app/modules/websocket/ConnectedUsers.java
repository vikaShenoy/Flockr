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
    private Map<User, ActorRef> currentlyConnectedUsers;


    /**
     * Creates connected users
     */
    private ConnectedUsers() {
        currentlyConnectedUsers = new HashMap<>();
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
        currentlyConnectedUsers.put(user, out);
    }


    /**
     * Removes a connected users by
     * @param user The user object
     */
    public void removeConnectedUser(User user) {
        currentlyConnectedUsers.remove(user);
    }


    /**
     * Checks if a user is currently connected.
     *
     * @param user the user to check.
     * @return true if the user is connected.
     */
    public boolean isUserConnected(User user) {
        return currentlyConnectedUsers.containsKey(user);
    }


    public Map<User, ActorRef> getCurrentlyConnectedUsers() {
        return currentlyConnectedUsers;
    }


    /**
     * Clears all connected users
     */
    public void clear() {
        currentlyConnectedUsers.clear();
    }


    /**
     * Get the websocket for the already connected user
     * NOTE: the user must be connected (can use a checked method to ensure this)
     * @param user the connected user
     * @return the websocket for the user
     */
    public ActorRef getSocketForUser(User user) {
        return currentlyConnectedUsers.get(user);
    }
}
