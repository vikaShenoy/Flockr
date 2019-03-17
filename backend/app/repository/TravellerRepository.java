package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Passport;
import models.User;
import play.db.ebean.EbeanConfig;
import play.db.ebean.EbeanDynamicEvolutions;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Contains database calls for all things traveller related
 */
public class TravellerRepository {
   private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency injection
     * @param ebeanConfig ebean config to use
     * @param executionContext Context to run completion stages on
     */
    @Inject
    public TravellerRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Updates a users details
     * @param user The user to update
     * @return Nothing
     */
    public CompletionStage<Void> updateUser(User user) {
        return runAsync(() -> {
            user.update();
        });
    }

    /**
     * Gets a passport by it's ID
     * @param passportId The passport to get
     * @return The list of passports
     */
    public CompletionStage<Optional<Passport>> getPassportById(int passportId) {
        return supplyAsync(() -> {
            Optional<Passport> passport = Passport.find.query().
                    where().eq("passport_id", passportId).findOneOrEmpty();
            return passport;
        }, executionContext);
    }
}
