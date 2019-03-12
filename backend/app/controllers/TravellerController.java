package controllers;

import models.Gender;
import models.Traveller;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import javax.inject.Inject;
import static play.mvc.Results.ok;

import java.util.Date;
import java.util.List;

/**
 * Manage front end actions related to travellers
 */
public class TravellerController {
    private FormFactory formFactory;
    private final Form<TravellerData> travellerForm;
    private final Form<TravellerLoginData> travellerLoginDataForm;
    private HttpExecutionContext httpExecutionContext;


    /**
     * Create a new TravellerController and inject the FormFactory into it
     * @param formFactory
     */
    @Inject
    public TravellerController(FormFactory formFactory) {
        this.formFactory = formFactory;
        this.travellerForm = this.formFactory.form(TravellerData.class);
        this.travellerLoginDataForm = this.formFactory.form(TravellerLoginData.class);
    }

    /**
     * Render the page that allows a traveller to login
     * @param request the request directed here by the routes file
     * @return the rendered page that allows a traveller to login
     */
    public Result loginTraveller(Http.Request request) {
        return ok(views.html.loginTraveller.render(this.travellerLoginDataForm, request));
    }

    /**
     * Handle form submission for when a traveller logs in
     * @param request the request directed here by the routes file
     * @return user data as json if successful or error message if no data found.
     */
    public Result handleTravellerLoginSubmission(Http.Request request) {
        Form<TravellerLoginData> boundForm = this.travellerLoginDataForm.bindFromRequest(request);
        TravellerLoginData travellerLoginData = boundForm.get();



        List<Traveller> selectedTraveller = Traveller.find.query().where().like("emailAddress", travellerLoginData.getEmailAddress())
                .like("password",travellerLoginData.getPassword()).findList();
        if (selectedTraveller.size() != 1) {
            return ok("BAD LOGIN");
        } else return ok(play.libs.Json.toJson(selectedTraveller));

    }

    /**
     * Front end method to render a page to create a new traveller.
     * @param request the request directed here by the routes file
     * @return the rendered page to create a new traveller
     */
    public Result createTraveller(Http.Request request) {
        return ok(views.html.createTraveller.render(this.travellerForm, request));
    }

    /**
     * Front end method to process new traveller form submissions
     * @param request the request directed here by the routes file
     * @return a rendered page confirming whether the user has been created
     */
    public Result handleCreateTravellerSubmission(Http.Request request) {
        final Form<TravellerData> boundForm = this.travellerForm.bindFromRequest(request);
        TravellerData travellerData = boundForm.get();
        String firstName = travellerData.getFirstName();
        String middleName = travellerData.getMiddleName();
        String lastName = travellerData.getLastName();
        String password = travellerData.getPassword();
        String gender = travellerData.getGender();
        Date birthday = travellerData.getBirthday();
        String emailAddress = travellerData.getEmailAddress();
        String nationalities = travellerData.getNationalities();
        String passports = travellerData.getPassports();

        Traveller traveller = new Traveller(travellerData);

        List<Traveller> selectedTraveller = Traveller.find.query().where().like("emailAddress", travellerData.getEmailAddress()).findList();
        if (selectedTraveller.size() == 0 ) {
            traveller.save();
            selectedTraveller = Traveller.find.query().where().like("emailAddress", travellerData.getEmailAddress())
                    .like("password",travellerData.getPassword()).findList();
            return ok(play.libs.Json.toJson(selectedTraveller));
        } else
            return ok("Traveller Exists");


    }


}
