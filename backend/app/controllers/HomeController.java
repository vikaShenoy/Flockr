package controllers;


import play.mvc.Controller;
import play.mvc.Result;

/**
 * Manage a database of computers
 */
public class HomeController extends Controller {
    /**
     * Handle default path requests
     */
    public Result index() {
      return ok(views.html.index.render());
    }
}
            
