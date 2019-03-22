package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Passport;
import models.Nationality;
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
     * Gets a user/traveller by their ID
     * @param userId The ID of the user to get
     * @return the user object
     */
    public CompletionStage<Optional<User>> getUserById(int userId) {
        return supplyAsync(() -> {
            Optional<User> user = User.find.query().
                    where().eq("user_id", userId).findOneOrEmpty();
            return user;
        }, executionContext);
    }

    /**
     * A function that gets the list of all the valid passports.
     * @return the list of all the Passports
     */
    public CompletionStage<List<Passport>> getAllPassports() {
        return supplyAsync(() -> {
            List<Passport> passports = Passport.find.query().findList();
            return passports;
        }, executionContext);
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

    /**
     * Gets a list of all nationalities
     * @return <b>List</b> of nationalities
     */
    public CompletionStage<List<Nationality>> getAllNationalities() {
        return supplyAsync(() -> {
            List<Nationality> nationalities = Nationality.find.query().findList();
            System.out.println(nationalities.get(0));
            return nationalities;
        }, executionContext);
    }

    /**
     * Gets a nationality by it's ID
     * @param nationalityId The nationality to get
     * @return The list of nationalities
     */
    public CompletionStage<Optional<Nationality>> getNationalityById(int nationalityId) {
        return supplyAsync(() -> {
            Optional<Nationality> nationality = Nationality.find.query().
                    where().eq("nationality_id", nationalityId).findOneOrEmpty();
            return nationality;
        }, executionContext);
    }
}
