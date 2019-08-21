package modules.websocket.frames;

import models.User;

/**
 * Websocket frame sent when a user disconnects
 */
public class DisconnectedFrame implements Frame {

    private User user;

    public DisconnectedFrame(User user)  {
        this.user = user;
    }


    public User getUser() {
        return user;
    }

    @Override
    public String getType() {
        return "disconnected";
    }
}
