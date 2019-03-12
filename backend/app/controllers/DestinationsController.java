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
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * Manage front end actions related to destinations
 */
public class DestinationsController extends Controller {
    private FormFactory formFactory;
    private Form<DestinationData> createDestinationForm;
    private HttpExecutionContext httpExecutionContext;

    /**
     * Make a new DestinationsController and inject the form factory to it
     * @param formFactory the form factory being injected
     */
    @Inject
    public DestinationsController(FormFactory formFactory) {
        this.formFactory = formFactory;
        this.createDestinationForm = this.formFactory.form(DestinationData.class);
    }

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

//        List<Destination> destinations = com.google.common.collect.Lists.newArrayList(
//                new Destination("Niagara Falls", DestinationType.NATURAL_ATTRACTION, "Ontario", 43.085094, 	-79.079559, "Canada"),
//                new Destination("Torres del Paine", DestinationType.NATURAL_ATTRACTION, "Magallanes y Antartica Chilena", -51.000000, -73.094, "Chile")
//        );
//        return ok(views.html.destinations.listAllDestinations.render(asScala(destinations), request));
    }


    /**
     * Render the page that allows the user to create a new destination
     * @param request the request directed here by the routes file
     * @return the rendered page that allows the user to create a new destination
     */
    public Result createDestination(Http.Request request) {
        return ok(views.html.destinations.createDestination.render(this.createDestinationForm, request));
    }


    /**
     * Handle form submissions for new destinations and confirm to the user whether the destination was added or not
     * @param request the request directed here by the routes file
     * @return an inditcation to the user of whether their new destination was created or not
     */
    public Result handleCreateDestinationSubmission(Http.Request request) {
        final Form<DestinationData> boundForm = this.createDestinationForm.bindFromRequest(request);
        DestinationData destinationData = boundForm.get();
        Destination destination = new Destination(destinationData);
        // TODO: add this new destination to data persistence
        destination.save();

        return ok("Server received destination " + destination.getName());
    }
}
