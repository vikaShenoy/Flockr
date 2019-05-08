package repository;

import io.ebean.*;
import models.User;
import play.db.ebean.EbeanConfig;
import play.db.ebean.EbeanDynamicEvolutions;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Repository methods for authentication
 */
public class AuthRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;
    private final EbeanDynamicEvolutions ebeanDynamicEvolutions;

    @Inject
    public AuthRepository(EbeanConfig ebeanConfig, EbeanDynamicEvolutions ebeanDynamicEvolutions, DatabaseExecutionContext executionContext) {
        this.ebeanDynamicEvolutions = ebeanDynamicEvolutions;
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Add a new user
     *
     * @param user The user to add
     * @return The added user
     */
    public CompletionStage<User> insert(User user) {
        return supplyAsync(() -> {
            user.save();
            return user;
        }, executionContext);
    }

    /**
     * Gets a user by their auth token
     *
     * @param token The token to find the user by
     * @return The user
     */
    public CompletionStage<Optional<User>> getByToken(String token) {
        return supplyAsync(() -> {
            Optional<User> user = ebeanServer.find(User.class)
                    .select("*")
                    .fetch("passports")              // contacts is a OneToMany path
                    .fetch("travellerTypes")
                    .fetch("nationalities")
                    .where()
                    .eq("token", token)
                    .findOneOrEmpty();
            return user;
        }, executionContext);
    }

    /**
     * Gets a user by their credentials
     */
    public CompletionStage<Optional<User>> getUserByCredentials(String email, String hashedPassword) {
        return supplyAsync(() -> {
            Optional<User> user = User.find
                    .query()
                    .fetch("passports")
                    .fetch("nationalities")
                    .fetch("travellerTypes")
                    .where()
                    .eq("email", email)
                    .findOneOrEmpty();
            return user;
        }, executionContext);
    }

    /**
     * Gets a user by their email
     *
     * @param email The email of the user
     * @return The user (which may not exist)
     */
    public CompletionStage<Optional<User>> getUserByEmail(String email) {
        return supplyAsync(() -> {
            Optional<User> user = ebeanServer.find(User.class)
                    .select("userId")
                    .where()
                    .eq("email", email)
                    .findOneOrEmpty();
            return user;
        }, executionContext);
    }
}
