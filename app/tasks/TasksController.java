package tasks;

import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/** Registers the tasks that need to be handled by the application. */
public class TasksController extends AbstractModule {

  private boolean populate = false; //NOTE: MUST HAVE INTERNET ENABLED !!!

  @Override
  protected void configure() {
    bind(SetupTask.class).asEagerSingleton();

    Config conf = ConfigFactory.load();
    String environment = conf.getString("environment");
    boolean populateData = conf.getBoolean("populateData");

    if (!environment.equals("test")) {
      bind(DeleteExpiredPhotosTask.class).asEagerSingleton();
      bind(DeleteExpiredUsersTask.class).asEagerSingleton();
      bind(DeleteExpiredDestinationsTask.class).asEagerSingleton();
      bind(DeleteExpiredDestinationProposal.class).asEagerSingleton();
      bind(CountrySyncTask.class).asEagerSingleton();
      bind(DeleteExpiredTripsTask.class).asEagerSingleton();
      bind(DeleteExpiredTreasureHunts.class).asEagerSingleton();
      bind(DeleteExpiredDestinationPhotos.class).asEagerSingleton();

      if (environment.equals("dev") && populateData) {
        bind(PopulateTask.class).asEagerSingleton();
      }

      // you may add more tasks here

      if (populate) {
        bind(ExampleUserPhotoData.class).asEagerSingleton();
        bind(ExampleUserData.class).asEagerSingleton();
        bind(ExampleDestinationDataTask.class).asEagerSingleton();
        bind(ExampleTripsDataTask.class).asEagerSingleton();
      }
    }
  }
}
