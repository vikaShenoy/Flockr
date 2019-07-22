package tasks;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class TasksCustomExecutionContext extends CustomExecutionContext {

    @Inject
    public TasksCustomExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "tasks-dispatcher");
    }
}