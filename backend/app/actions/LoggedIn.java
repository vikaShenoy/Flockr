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


public class AuthActions extends Action.Simple {
    private final AuthRepository authRepository;

    @Inject
    public AuthActions(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }


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

                return delegate.call(request);
            });
    }



//        .thenCompose((user) -> {
//            if (user == null) {
//                return unauthorized();
//            }
//        })
//        .thenApplyAsync(reuest -> reuest);
}
