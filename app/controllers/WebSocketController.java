package controllers;

import actors.WebSocketActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import play.libs.F;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import repository.AuthRepository;
import static java.util.concurrent.CompletableFuture.supplyAsync;


import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Handles all websocket creation related functionality
 */
public class WebSocketController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final AuthRepository authRepository;


    /**
     * Creates a new websocket controller
     * @param actorSystem The system to use when creating websockets
     * @param materializer Executes the actors
     * @param authRepository authentication repository
     */
    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, AuthRepository authRepository) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.authRepository = authRepository;
    }

    /**
     * Handles and authenticates a new client websocket request
     * @return A websocket or a forbidden response
     */
    public WebSocket socket() {
        return WebSocket.Text.acceptOrResult(request -> {
             String authToken = request.getQueryString("Authorization");

             return authRepository.getByToken(authToken)
                     .thenApplyAsync(user -> {
                         if (!user.isPresent()) {
                             return F.Either.<Result, Flow<String, String, ?>>Left(unauthorized());
                         }

                         return F.Either.Right(ActorFlow.actorRef((actorRef) -> Props.create(WebSocketActor.class, actorRef, user.get()), actorSystem, materializer));
                     });
        });
    }


}
