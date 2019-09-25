package actions;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.CompletionStage;
import models.RoleType;
import models.User;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import util.Security;


public class Admin extends Action.Simple {

    /**
     * The call to check if the user is logged in
     * @param request Request object to get authorization token from
     * @return The next middleware/controller if successful, returns unauthorized if the token was invalid,
     */
    @Override
    public CompletionStage<Result> call(Http.Request request) {
        // Get user from loggedIn middleware
        User user = request.attrs().get(ActionState.USER);

        if (Security.checkRoleExists(user, RoleType.ADMIN) || Security.checkRoleExists(user, RoleType.SUPER_ADMIN)) {
            return delegate.call(request);
        }

        JsonNode response = Json.newObject().put("error", "Unauthorized");
        return supplyAsync(() -> forbidden(response));
    }
}