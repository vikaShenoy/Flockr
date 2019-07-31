package actions;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import repository.AuthRepository;
import javax.inject.Inject;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Middleware to check if a user is logged in.
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
            Optional<String> optionalToken = request.getHeaders().get("Authorization");
            String token = optionalToken.isPresent() ? optionalToken.get() : request.getQueryString("Authorization");


            if (token == null) {
                JsonNode response = Json.newObject().put("error", "Unauthorized");
                return supplyAsync(() -> unauthorized(response));
            }
            return authRepository.getByToken(token)
            .thenCompose(user -> {
               if (!user.isPresent()) {
                   JsonNode response = Json.newObject().put("error", "Unauthorized");
                   return supplyAsync(() -> unauthorized(response));
               }

               return delegate.call(request.addAttr(ActionState.USER, user.get()));
            });
    }
}
