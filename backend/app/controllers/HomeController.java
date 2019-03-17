package controllers;


import actions.ActionState;
import actions.ProfileCompleted;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http;
import play.mvc.With;
import actions.LoggedIn;

/**
 * Manage a database of computers
 */
public class HomeController extends Controller {
    /**
     * Handle default path requests
     */

    @With(LoggedIn.class)
    public Result index(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);

        System.out.println("user is: " + user.getFirstName());
        return ok("Congrats! You are in the backend index, you probably shouldn't be querying this though!");
    }
}
            
