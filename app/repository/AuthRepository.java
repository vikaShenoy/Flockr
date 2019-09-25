package repository;

import io.ebean.*;
import models.User;
import play.db.ebean.EbeanConfig;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;


/**
 * Class that performs operations on the database regarding authorization.
 */
public class AuthRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public AuthRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
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
        return supplyAsync(() -> ebeanServer.find(User.class)
                .select("*")
                .fetch("passports")              // contacts is a OneToMany path
                .fetch("travellerTypes")
                .fetch("nationalities")
                .fetch("roles")
                .where()
                .eq("token", token)
                .findOneOrEmpty(), executionContext);
    }

    /**
     * Gets a user by their credentials
     */
    public CompletionStage<Optional<User>> getUserByCredentials(String email) {
        return supplyAsync(() -> User.find
                .query()
                .fetch("passports")
                .fetch("nationalities")
                .fetch("travellerTypes")
                .fetch("roles")
                .where()
                .eq("email", email)
                .findOneOrEmpty(), executionContext);
    }

    /**
     * Gets a user by their email
     *
     * @param email The email of the user
     * @return The user (which may not exist)
     */
    public CompletionStage<Optional<User>> getUserByEmail(String email) {
        return supplyAsync(() -> ebeanServer.find(User.class)
                .select("userId")
                .where()
                .eq("email", email)
                .findOneOrEmpty(), executionContext);
    }
}
