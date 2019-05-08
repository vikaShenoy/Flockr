package actions;

import io.ebean.EbeanServer;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import models.User;
import repository.AuthRepository;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Middleware to check if a user is logged in
 */
public class LoggedIn extends Action.Simple {
    private final AuthRepository authRepository;

    @Inject
    public LoggedIn(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * The call to check if the user is logged in
     * @param request Request object to get authorization token from
     * @return The next middleware/controller if successful, returns unauthorized if the token was invalid,
     */
    @Override
    public CompletionStage<Result> call(Http.Request request) {
            Optional<String> token = request.getHeaders().get("Authorization");

            if (!token.isPresent()) {
                return supplyAsync(() -> unauthorized("You are unauthorized"));
            }
            return authRepository.getByToken(token.get())
            .thenCompose((user) -> {
               if (!user.isPresent()) {
                   return supplyAsync(() -> unauthorized("You are unauthorized"));
               }

               return delegate.call(request.addAttr(ActionState.USER, user.get()));
            });
    }
}
