package tasks;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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
    bind(SetupTask.class).asEagerSingleton();

    Config conf = ConfigFactory.load();
    String environment = conf.getString("environment");
    boolean populateData = conf.getBoolean("populateData");

    // System.out.println(Play.current().configuration().get());
     if (environment.equals("dev") && populateData) {
       bind(PopulateTask.class).asEagerSingleton();
     }

    // you may add more tasks here

    //If you want to populate example User data uncomment the next line.
    bind(ExampleUserData.class).asEagerSingleton();
  }
}
