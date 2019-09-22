package actions;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.RoleType;
import models.User;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import util.Security;


public class Admin extends Action.Simple {

    private final Security security;

    /**
     * Inject the utility security class on initialisation.
     * @param security security helper class object.
     */
    @Inject
    public Admin(Security security) {
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

        if (this.security.checkRoleExists(user, RoleType.ADMIN) || this.security.checkRoleExists(user, RoleType.SUPER_ADMIN)) {
            return delegate.call(request);
        }

        JsonNode response = Json.newObject().put("error", "Unauthorized");
        return supplyAsync(() -> forbidden(response));
    }
}