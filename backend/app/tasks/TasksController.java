package tasks;

import com.google.inject.AbstractModule;

public class TasksController extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeleteExpiredPhotosTask.class).asEagerSingleton();
        bind(DeleteExpiredUsersTask.class).asEagerSingleton();
        //TODO: add more for other model types.
    }
}