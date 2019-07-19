package repository;

import models.TreasureHunt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Contains all database interaction associated with treasure hunts.
 */
public class TreasureHuntRepository {

    final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency Injection
     *
     * @param executionContext Context to run the completion stages on.
     */
    @Inject
    public TreasureHuntRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Method to save changes to a treasure hunt in the database.
     *
     * @param treasureHunt the modified treasure hunt object.
     * @return the async method to run which updates the treasure hunt.
     */
    public CompletionStage<TreasureHunt> modifyTreasureHunt(TreasureHunt treasureHunt) {
        return supplyAsync(() -> {
            treasureHunt.save();
            return treasureHunt;
        }, executionContext);
    }

    /**
     * Method to delete a treasure hunt from the database.
     *
     * @param treasureHunt the treasure hunt object.
     * @return the async method to run which updates the treasure hunt.
     */
    public CompletionStage<Boolean> removeTreasureHunt(TreasureHunt treasureHunt) {
        return supplyAsync(treasureHunt::delete, executionContext);
    }

    /**
     * Method to get a treasure hunt from the database using an id.
     *
     * @param treasureHuntId the id of the treasure hunt.
     * @return the optional object containing null or the treasure hunt object.
     */
    public CompletionStage<Optional<TreasureHunt>> getTreasureHuntById(int treasureHuntId) {
        return supplyAsync(() -> TreasureHunt.find.query().where()
                .eq("treasure_hunt_id", treasureHuntId).findOneOrEmpty());
    }

    /**
     * Method to get a users treasure hunts from the database using their user ID
     * @param userId the id of the user
     * @return List of treasure hunts
     */

    public CompletionStage<List<TreasureHunt>> getTreasureHuntsByUserId(int userId) {
        return supplyAsync(() -> {
            List<TreasureHunt> treasureHunts = TreasureHunt.find.query().where()
                    .eq("owner_user_id", userId).findList();
            return treasureHunts;
        }, executionContext);
    }
}



