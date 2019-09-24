package modules.websocket;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.reflections.util.ConfigurationBuilder.build;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.User;
import modules.websocket.frames.PingMapFrame;
import play.libs.Json;
import repository.ChatRepository;
import repository.TripRepository;

public class WebSocket extends AbstractActor {

  private final TripRepository tripRepository;
  private final ChatRepository chatRepository;
  private final ActorRef out;
  private ConnectionStatusNotifier connectionStatusNotifier = new ConnectionStatusNotifier();
  private User user;

  /**
   * Creates a new websocket and adds user to connected users
   *
   * @param out the websocket object
   * @param user the user that owns the websocket
   * @param tripRepository the trip repository
   * @param chatRepository the chat repository
   */
  @Inject
  public WebSocket(
      ActorRef out, User user, TripRepository tripRepository, ChatRepository chatRepository) {
    ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
    this.out = out;
    this.user = user;
    this.tripRepository = tripRepository;
    this.chatRepository = chatRepository;
    connectedUsers.addConnectedUser(user, out);
    // Notify everyone that you are in a trip in that you are now online
    notifyTripConnected();
    notifyChatsConnected();
  }

  /**
   * Defines how to create a new websocket. Passes the user prop so we can identify what user owns
   * what websocket
   *
   * @param out The client that sent the websocket
   * @param tripRepository the trip repository
   * @param user the user that owns the websocket
   * @param chatRepository the chat repository
   */
  public static Props props(
      ActorRef out, User user, TripRepository tripRepository, ChatRepository chatRepository) {
    return Props.create(WebSocket.class, out, user, tripRepository, chatRepository);
  }

  /**
   * Notifies to all users that have trips with the connected user that they are connected
   *
   * @return CompletionStage to run the task in the background
   */
  private CompletionStage<Void> notifyTripConnected() {
    return runAsync(
        () ->
            tripRepository
                .getTripsByUserId(user.getUserId())
                .thenAcceptAsync(
                    trips -> connectionStatusNotifier.notifyConnectedUser(user, trips)));
  }


  /**
   * Notifies to all users that have group chats with the connected user that they are connected.
   *
   * @return CompletionStage to run the task in the background.
   */
  private CompletionStage<Void> notifyChatsConnected() {
    return runAsync(
        () ->
            chatRepository
                .getChatsByUserId(user.getUserId())
                .thenAcceptAsync(
                    chatGroups -> {
                      ChatEvents chatEvents = new ChatEvents();
                      chatEvents.notifyConnect(user, chatGroups);
                    }));
  }

  /**
   * Gets called when a websocket has been closed on the client. Removes the current websocket from
   * connected users
   */
  @Override
  public void postStop() {
    tripRepository
        .getTripsByUserId(user.getUserId())
        .thenAcceptAsync(
            trips -> {
              ConnectedUsers connectedUsers = ConnectedUsers.getInstance();
              connectedUsers.removeConnectedUser(user);
              connectionStatusNotifier.notifyDisconnectedUser(user, trips);
            });
    chatRepository
        .getChatsByUserId(user.getUserId())
        .thenAcceptAsync(
            chatGroups -> {
              ChatEvents chatEvents = new ChatEvents();
              chatEvents.notifyDisconnect(user, chatGroups);
            });
  }

  /**
   * Accepts the websocket messages and notifies the users that are connected to the sent ping
   * message
   */
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(
            String.class,
            messageFrame -> {
              JsonNode message = Json.parse(messageFrame);
              if (message.get("type").asText().equals("ping-map")) {
                ObjectMapper objectMapper = new ObjectMapper();
                PingMapFrame pingMapFrame = objectMapper.treeToValue(message, PingMapFrame.class);
                tripRepository
                    .getTripByIds(pingMapFrame.getTripNodeId(), user.getUserId())
                    .thenAcceptAsync(
                        trip -> {
                          if (trip.isPresent()) {
                            PingMapNotifier pingMapNotifier =
                                new PingMapNotifier(pingMapFrame, trip.get());
                            pingMapNotifier.notifyUsers(user);
                          } else {
                            out.tell("Map ping failed, trip not found", self());
                          }
                        });
              }
            })
        .build();
  }
}
