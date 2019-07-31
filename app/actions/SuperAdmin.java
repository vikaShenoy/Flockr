package actions;

import models.RoleType;
import models.User;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import util.Security;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;


public class SuperAdmin extends Action.Simple {

    private final Security security;

    @Inject
    public SuperAdmin(Security security) {
        this.security = security;
    }

    /**
     * The call to check if the user is logged in
     * @param request Request object to get authorization token from
     * @return The next middleware/controller if successful, returns unauthorized if the token was invalid,
     */
    @Override
    public CompletionStage<Result> call(Http.Request request) {
        // Get user from loggedIn middleware
        User user = request.attrs().get(ActionState.USER);

        if (this.security.checkRoleExists(user, RoleType.SUPER_ADMIN)) {
            return delegate.call(request);
        }

        return supplyAsync(() -> unauthorized("You are unauthorized"));
    }
}


