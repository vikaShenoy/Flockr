package actions;

import models.User;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Middleware to check if a user has completed their profile.
 * NOTE: needs to be called AFTER LoggedIn middleware
 */
public class ProfileCompleted extends Action.Simple {

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        if (!request.attrs().containsKey(ActionState.USER)) {
            return supplyAsync(() -> forbidden());
        }

        User user = request.attrs().get(ActionState.USER);

        if (user.profileCompleted()) {
            return delegate.call(request);
        } else {
            return supplyAsync(() -> forbidden());
        }
    }

}
