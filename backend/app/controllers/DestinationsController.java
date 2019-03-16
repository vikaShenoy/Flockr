package controllers;

import models.destinations.Destination;
import models.destinations.DestinationType;
import play.core.j.HttpExecutionContext;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * Manage front end actions related to destinations
 */
public class DestinationsController extends Controller {
    /**
     * Show all destinations to the user
     * @param request the request made through the routes file
     * @return the rendered Play template showing all destinations
     */
    public Result listAllDestinations(Http.Request request) {
        // TODO: implement getting all destinations from database
        List<Destination> destinationList = Destination.find.all();
        if (destinationList.size() != 0 ) {
            return ok(play.libs.Json.toJson(destinationList));
        } else
            return ok("No Destinations");
    }


    /**
     * Render the page that allows the user to create a new destination
     * @param request the request directed here by the routes file
     * @return the rendered page that allows the user to create a new destination
     */
    public Result createDestination(Http.Request request) {
        return ok("You have reached the create destinations endpoint");
    }


    /**
     * Handle form submissions for new destinations and confirm to the user whether the destination was added or not
     * @param request the request directed here by the routes file
     * @return an inditcation to the user of whether their new destination was created or not
     */
    public Result handleCreateDestinationSubmission(Http.Request request) {
        String name = "Ilam Gardens";
        DestinationType destinationType = DestinationType.NATURAL_ATTRACTION;
        String disctrict = "Canterbury";
        Double latitude = 50.34;
        Double longitude = 32.0901;
        String country = "New Zealand";

        Destination destination = new Destination(name, destinationType, disctrict, latitude, longitude, country);
        // TODO: add this new destination to data persistence
        destination.save();

        return ok("Server received destination " + destination.getName());
    }
}
