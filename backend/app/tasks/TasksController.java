package tasks;

import com.google.inject.AbstractModule;

/**
 * Registers the tasks that need to be handled by the application.
 */
public class TasksController extends AbstractModule {

    @Override
    protected void configure() {
        bind(CountrySyncTask.class).asEagerSingleton();
    }
}
