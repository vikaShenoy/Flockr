package modules.websocket.frames;

import models.User;

/**
 * Websocket frame for when a user connects
 */
public class ConnectedFrame implements Frame {

    private User user;

    public ConnectedFrame(User user)  {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getType() {
        return "connected";
    }
}
