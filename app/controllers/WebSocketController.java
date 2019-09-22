package controllers;

import modules.websocket.TripNotifier;
import modules.websocket.WebSocket;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import play.libs.F;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import repository.AuthRepository;
import repository.ChatRepository;
import repository.TripRepository;

import static java.util.concurrent.CompletableFuture.supplyAsync;


import javax.inject.Inject;

/**
 * Handles all websocket creation related functionality
 */
public class WebSocketController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final AuthRepository authRepository;
    private final TripRepository tripRepository;
    private final ChatRepository chatRepository;


    /**
     * Creates a new websocket controller
     * @param actorSystem The system to use when creating websockets
     * @param materializer Executes the actors
     * @param authRepository authentication repository
     * @param chatRepository the chat repository
     */
    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, AuthRepository authRepository, TripNotifier tripNotifier, TripRepository tripRepository, ChatRepository chatRepository) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.authRepository = authRepository;
        this.tripRepository = tripRepository;
        this.chatRepository = chatRepository;
    }

    /**
     * Handles and authenticates a new client websocket request
     * @return A websocket or a forbidden response
     */
    public play.mvc.WebSocket socket() {
        return play.mvc.WebSocket.Text.acceptOrResult(request -> {
             String authToken = request.getQueryString("Authorization");

             return authRepository.getByToken(authToken)
                     .thenApplyAsync(user -> {
                         if (!user.isPresent()) {
                             return F.Either.<Result, Flow<String, String, ?>>Left(unauthorized());
                         }
                         return F.Either.Right(ActorFlow.actorRef(actorRef -> Props.create(WebSocket.class, actorRef, user.get(), tripRepository, chatRepository), actorSystem, materializer));
                     });
        });
    }


}
