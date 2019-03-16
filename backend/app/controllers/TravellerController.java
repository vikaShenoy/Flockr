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
    public Result loginTraveller(Http.Request request) {
        return ok("You have reached the login traveller endpoint");
    }

    public Result handleTravellerLoginSubmission(Http.Request request) {
        // TODO: get these from request
        String email = "email@me.com";
        String password = "wow_verySecure";

        List<Traveller> selectedTraveller = Traveller.find.query().where().like("emailAddress", email)
                .like("password", password).findList();
        if (selectedTraveller.size() != 1) {
            return ok("BAD LOGIN");
        } else return ok(play.libs.Json.toJson(selectedTraveller));

    }

    public Result createTraveller(Http.Request request) {
        // TODO: implement create traveller
        return ok("You have reached the end point to create a traveller");
    }

    public Result handleCreateTravellerSubmission(Http.Request request) {
        String firstName = "Felipe";
        String middleName = "Rafael";
        String lastName = "Araneda Ruiz";
        String password = "wow_muchSecurity";
        String gender = "Male";
        Date birthday = new Date();
        String emailAddress = "me@email.com";
        String nationalities = "I am very nationality";
        String passports = "ALL the passports";

        Traveller traveller = new Traveller(firstName, middleName, lastName, password, gender, emailAddress, nationalities, birthday, passports);

        List<Traveller> selectedTraveller = Traveller.find.query().where().like("emailAddress", emailAddress).findList();
        if (selectedTraveller.size() == 0 ) {
            traveller.save();
            selectedTraveller = Traveller.find.query().where().like("emailAddress", emailAddress)
                    .like("password", password).findList();
            return ok(play.libs.Json.toJson(selectedTraveller));
        } else
            return ok("Traveller Exists");
    }


}
