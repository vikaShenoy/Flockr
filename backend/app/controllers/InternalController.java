package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.Nationality;
import models.Passport;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import util.Security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

public class InternalController {

    /**
     * Populate the database with data
     * @return a JSON response if the popoulation was successful
     */
    public CompletionStage<Result> resample() {
        return supplyAsync(() -> {
            Passport passport1 = new Passport("NZ");
            Passport passport2 = new Passport("Australia");

            Nationality nationality1 = new Nationality("New Zealand");
            Nationality nationality2 = new Nationality("Australia");
            Nationality nationality3 = new Nationality("Afghanistan");

            DestinationType destinationType1 = new DestinationType("Event");
            DestinationType destinationType2 = new DestinationType("City");

            Country country1 = new Country("United States of America");
            Country country2 = new Country("Australia");

            District district1 = new District("Black Rock City", country1);
            District district2 = new District("New Farm", country2);

            country1.save();
            country2.save();

            district1.save();
            district2.save();

            passport1.save();
            passport2.save();

            nationality1.save();
            nationality2.save();
            nationality3.save();

            destinationType1.save();
            destinationType2.save();

            Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234, country1 );
            Destination destination2 = new Destination("Brisbane City",destinationType2, district2, 11.1234,11.1234, country2 );

            destination1.save();
            destination2.save();

            TravellerType travellerType1 = new TravellerType("Groupie");
            travellerType1.save();

            TravellerType travellerType2 = new TravellerType("Thrillseeker");
            travellerType2.save();

            TravellerType travellerType3 = new TravellerType("Gap year");
            travellerType3.save();

            TravellerType travellerType4 = new TravellerType("Frequent weekender");
            travellerType4.save();

            TravellerType travellerType5 = new TravellerType("Holidaymaker");
            travellerType5.save();

            TravellerType travellerType6 = new TravellerType("Functional/business");
            travellerType6.save();

            TravellerType travellerType7 = new TravellerType("Backpacker");
            travellerType7.save();

            generateMockUser();

            ObjectNode json = Json.newObject();
            json.put("message", "Success resampling the database");
            return ok(json);
        });
    }


    /**
     * Create a user and save to database.
     * NOTE: also creates a passport, nationality, traveller type and gender in the database
     */
    public void generateMockUser() {
        Security security = new Security();

        String firstName = "Luis";
        String middleName = "Sebastian";
        String lastName = "Ruiz";
        String password = "so-secure";
        String passwordHash = security.hashPassword(password);
        String email = "luis@gmail.com";
        String token = security.generateToken();
        Timestamp dateOfBirth = new Timestamp(637920534);

        // NOTE: new nationality saved to database
        Nationality nationality = new Nationality("Peru");
        nationality.save();

        List<Nationality> nationalityList = new ArrayList<>();
        nationalityList.add(nationality);

        // NOTE: new traveller type saved to database
        TravellerType travellerType = new TravellerType("Backpacker");
        travellerType.save();

        List<TravellerType> travellerTypeList = new ArrayList<>();
        travellerTypeList.add(travellerType);

        // NOTE: 2 new passports saved to database
        Passport peruPassport = new Passport("Peru");
        Passport boliviaPassport = new Passport("Bolivia");
        peruPassport.save();
        boliviaPassport.save();

        List<Passport> passportList = new ArrayList<>();
        passportList.add(peruPassport);
        passportList.add(boliviaPassport);

        User user = new User(firstName, middleName, lastName, passwordHash, "Male", email, nationalityList, travellerTypeList, dateOfBirth, passportList, token);
        user.save();
    }
}
