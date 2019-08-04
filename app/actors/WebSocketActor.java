package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import models.User;

import javax.inject.Inject;

public class WebSocketActor extends AbstractActor {


    /**
     * Defines how to create a new websocket. Passes the user prop so we can
     * identify what user owns what websocket
     * @param out The client that sent the websocket
     * @return
     */
    public static Props props(ActorRef out, User user) {
        return Props.create(WebSocketActor.class, out, user);
    }

    private final ActorRef out;


    /**
     * Creates a new websocket and adds user to connected users
     * @param out The websocket object
     * @param user The user that owns the websocket
     */
    @Inject
    public WebSocketActor(ActorRef out, User user) {
        ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
        this.out = out;
        connectedUsers.addConnectedUser(user, out);

    }

    /**
     * Gets called when a websocket has been closed on the client. Removes the current websocket
     * from connected users
     */
    public void postStop() {
        ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
        connectedUsers.removeConnectedUser(out);

    }

    @Override
    /**
     * Gets called when a message has been received.
     */
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> out.tell("I received your message: " + message, self()))
                .build();
    }

}
