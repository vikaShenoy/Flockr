
package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Role;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class RoleRepository {
    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency injection
     * @param ebeanConfig the Ebean configuration to use
     * @param executionContext The context to run completion stages on
     */
    @Inject
    public RoleRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * A function that gets the list of all the valid roles.
     * @return the list of all the Roles
     */
    public CompletionStage<List<Role>> getAllRoles() {
        return supplyAsync(() -> {
            return Role.find.query().findList();
        }, executionContext);
    }

    /**
     * Function that gets the roles of a specific user from the database and returns them as a list in an async
     * function.
     * @param userId int the id of the user
     * @return CompletionStage&ltList&ltRole&gt&gt
     */
    public CompletionStage<List<Role>> getUsersRoles(int userId) {
        return supplyAsync(() -> {
            return Role.find.query().where().eq("userId", userId).findList();
        }, executionContext);
    }
}