package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ChatActor extends AbstractActor {

    /**
     * Instantiates a new websocket
     * @param out The client that sent the websocket
     * @return
     */
    public static Props props(ActorRef out) {
        return Props.create(ChatActor.class, out);
    }

    private final ActorRef out;

    public ChatActor(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> out.tell("I received your message: " + message, self()))
                .build();
    }

}
