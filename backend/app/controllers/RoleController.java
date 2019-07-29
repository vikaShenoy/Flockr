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

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

/**
 * Controller to handle user role endpoints.
 */
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
     * @param request Http request object
     * @return CompletionStage<Result>
     *     - 200 with all roles if successful.
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getAllRoles(Http.Request request) {
        return roleRepository.getAllRoles()
                .thenApplyAsync(roles -> {
                    JsonNode rolesJson = Json.toJson(roles);
                    return ok(rolesJson);
                }, httpExecutionContext.current());
    }

    /**
     * Function that requests a list of all of a users roles from the role repository and returns an async function.
     * @param userId int the id of the user
     * @param request the http Request
     * @return
     * - 200 with roles if ok
     * - 404 not found if the user can't be found in db
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getUsersRoles(int userId, Http.Request request) {
        return roleRepository.getUser(userId)
                .thenApplyAsync(user -> {
                    if (user != null) {
                        JsonNode rolesJson = Json.toJson(user.getRoles());
                        return ok(rolesJson);
                    } else {
                        return notFound();
                    }
                }, httpExecutionContext.current());
    }


}