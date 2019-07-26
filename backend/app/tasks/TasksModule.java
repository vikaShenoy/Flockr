package tasks;

import com.google.inject.AbstractModule;

public class TasksModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeleteExpiredPhotos.class).asEagerSingleton();
        bind(DeleteExpiredDestinationProposal.class).asEagerSingleton();
        //TODO: add more for other model types.
    }
}