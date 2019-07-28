package tasks;

import com.google.inject.AbstractModule;

/**
 * Registers the tasks that need to be handled by the application.
 */
public class TasksController extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeleteExpiredPhotosTask.class).asEagerSingleton();
        bind(DeleteExpiredUsersTask.class).asEagerSingleton();
        bind(DeleteExpiredDestinationsTask.class).asEagerSingleton();
        bind(CountrySyncTask.class).asEagerSingleton();
        bind(DeleteExpiredTreasureHunts.class).asEagerSingleton();
        // you may add more tasks here
    }
}
