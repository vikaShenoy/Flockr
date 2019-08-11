package tasks;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.api.*;

/**
 * Registers the tasks that need to be handled by the application.
 */
public class TasksController extends AbstractModule {


    @Override
    protected void configure() {
        bind(DeleteExpiredPhotosTask.class).asEagerSingleton();
        bind(DeleteExpiredUsersTask.class).asEagerSingleton();
        bind(DeleteExpiredDestinationsTask.class).asEagerSingleton();
        bind(DeleteExpiredDestinationProposal.class).asEagerSingleton();
        bind(CountrySyncTask.class).asEagerSingleton();
        bind(DeleteExpiredTripsTask.class).asEagerSingleton();
        bind(DeleteExpiredTreasureHunts.class).asEagerSingleton();
        bind(DeleteExpiredDestinationPhotos.class).asEagerSingleton();

        // Config conf = ConfigFactory.load();
        // String environment = conf.getString("environment_test");
        // System.out.println(environment);
        //System.out.println(Play.current().configuration().get());
        //if (environment.equals("dev")) {
        //bind(PopulateTask.class).asEagerSingleton();
        //}

        // you may add more tasks here
    }
}
