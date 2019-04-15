package controllers;

import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.RoleRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;

public class RoleController {

    private final RoleRepository roleRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public RoleController(RoleRepository roleRepository, HttpExecutionContext httpExecutionContext) {
        this.roleRepository = roleRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * A function that gets a list of all the possible roles that are available in the
     * database
     * @param request The http request
     * @return CompletionStage<Result> the completion function to be
     * called on completion.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getAllRoles(Http.Request request) {
        return roleRepository.getAllRoles()
                .thenApplyAsync((roles) -> {
                    JsonNode rolesJson = Json.toJson(roles);
                    return ok(rolesJson);
                }, httpExecutionContext.current());
    }
}
