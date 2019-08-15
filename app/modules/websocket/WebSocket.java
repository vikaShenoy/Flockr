package modules.websocket;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.User;
import repository.TripRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.runAsync;

public class WebSocket extends AbstractActor {

    private ConnectionStatusNotifier connectionStatusNotifier = new ConnectionStatusNotifier();
    private final TripRepository tripRepository;
    /**
     * Defines how to create a new websocket. Passes the user prop so we can
     * identify what user owns what websocket
     * @param out The client that sent the websocket
     * @return
     */
    public static Props props(ActorRef out, User user, TripRepository tripRepository) {
        return Props.create(WebSocket.class, out, user, tripRepository);
    }

    private final ActorRef out;
    private User user;



    /**
     * Creates a new websocket and adds user to connected users
     * @param out The websocket object
     * @param user The user that owns the websocket
     */
    @Inject
    public WebSocket(ActorRef out, User user, TripRepository tripRepository) {
        ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
        this.out = out;
        this.user = user;
        this.tripRepository = tripRepository;
        connectedUsers.addConnectedUser(user, out);

        // Notify everyone that you are in a trip in that you are now online
        notifyTripConnected();

    }

    /**
     * Notifies to all users that have trips with the connected user that they are connected
     * @return CompletionStage to run the task in the background
     */
    private CompletionStage<Void> notifyTripConnected() {
        return runAsync(() -> {
            tripRepository.getTripsByIds(user.getUserId())
                    .thenApplyAsync(trips -> {
                        connectionStatusNotifier.notifyConnectedUser(user, trips);
                        return null;
                    });

        });
    }

    /**
     * Gets called when a websocket has been closed on the client. Removes the current websocket
     * from connected users
     */
    public void postStop() {
        tripRepository.getTripsByIds(user.getUserId())
                .thenApplyAsync(trips -> {
                    System.out.println("I have start notified disconnected users");
                    ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
                    connectedUsers.removeConnectedUser(user);
                    connectionStatusNotifier.notifyDisconnectedUser(user, trips);
                    return null;
                });
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
