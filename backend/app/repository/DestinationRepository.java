package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.*;
import play.db.ebean.EbeanConfig;
import play.db.ebean.EbeanDynamicEvolutions;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DestinationRepository {
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency injection
     * @param ebeanConfig ebean config to use
     * @param executionContext Context to run completion stages on
     */
    @Inject
    public DestinationRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }



    /**
     * Gets a list of all destinations
     * @return <b>List</b> of destinations
     */
    public CompletionStage<List<Destination>> getDestinations() {
        return supplyAsync(() -> {
            List<Destination> destinations = Destination.find.query().findList();
            System.out.println(destinations.get(0));
            return destinations;
        }, executionContext);
    }


    /**
     * Gets a destination by it's ID
     * @param destinationId The ID of the destination to get
     * @return the destination object
     */
    public CompletionStage<Optional<Destination>> getDestinationById(int destinationId) {
        return supplyAsync(() -> {
            Optional<Destination> destination = Destination.find.query().
                    where().eq("dest_id", destinationId).findOneOrEmpty();
            return destination;
        }, executionContext);
    }


    /**
     * Inserts a destination into the database
     * @param destination the destination to be inserted
     * @return the destination object
     */
    public CompletionStage<Destination> insert(Destination destination) {
        return supplyAsync(() -> {
            destination.save();
            return destination;
        }, executionContext);
    }



}
