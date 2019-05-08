
package repository;

import models.Role;
import models.RoleType;
import models.User;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class RoleRepository {
    private final DatabaseExecutionContext executionContext;

    /**
     * Dependency injection
     * @param ebeanConfig the Ebean configuration to use
     * @param executionContext The context to run completion stages on
     */
    @Inject
    public RoleRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * A function that gets the list of all the valid roles.
     * @return the list of all the RoleType
     */
    public CompletionStage<List<Role>> getAllRoles() {
        return supplyAsync(() -> Role.find.query().findList(), executionContext);
    }

    /**
     * Function that gets the roles of a specific user from the database and returns them as a list in an async
     * function.
     * @param userId int the id of the user
     * @return CompletionStage&ltList&ltRole&gt&gt
     */
    public CompletionStage<User> getUser(int userId) {
        return supplyAsync(() -> User.find.byId(userId), executionContext);
    }

    public CompletionStage<Role> getRole(RoleType roleType) {
        return  supplyAsync(() -> Role.find.query().where().eq("role_type", roleType.name()).findOne(), executionContext);
    }
}