package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import models.Nationality;
import models.Passport;
import play.libs.Json;
import play.mvc.Result;
import util.Security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
            String australia = "Australia";

            Passport passport1 = new Passport("NZ");
            Passport passport2 = new Passport(australia);
            Passport passport3 = new Passport("Peru");
            Passport passport4 = new Passport("Bolivia");

            Nationality nationality1 = new Nationality("New Zealand");
            Nationality nationality2 = new Nationality(australia);
            Nationality nationality3 = new Nationality("Afghanistan");
            Nationality nationality4 = new Nationality("Peru");

            DestinationType destinationType1 = new DestinationType("Event");
            DestinationType destinationType2 = new DestinationType("City");

            Country country1 = new Country("United States of America");
            Country country2 = new Country(australia);

            District district1 = new District("Black Rock City", country1);
            District district2 = new District("New Farm", country2);

            destinationType1.save();
            destinationType2.save();

            country1.save();
            country2.save();

            district1.save();
            district2.save();

            Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234, country1 );
            Destination destination2 = new Destination("Brisbane City",destinationType2, district2, 11.1234,11.1234, country2 );

            destination1.save();
            destination2.save();

            passport1.save();
            passport2.save();
            passport3.save();
            passport4.save();

            nationality1.save();
            nationality2.save();
            nationality3.save();
            nationality4.save();

            List<TripDestination> tripDestinations = new ArrayList<>();
            tripDestinations.add(new TripDestination(destination1, new Date(), 450, new Date(), 550));
            tripDestinations.add(new TripDestination(destination2, new Date(), 34, new Date(), 23));


            TravellerType travellerType1 = new TravellerType("Groupies");
            TravellerType travellerType2 = new TravellerType("Thrillseeker");
            TravellerType travellerType3 = new TravellerType("Gap Year");
            TravellerType travellerType4 = new TravellerType("Frequent Weekender");
            TravellerType travellerType5 = new TravellerType("Holidaymaker");
            TravellerType travellerType6 = new TravellerType("Functional/Business");
            TravellerType travellerType7 = new TravellerType("Backpacker");

            travellerType1.save();
            travellerType2.save();
            travellerType3.save();
            travellerType4.save();
            travellerType5.save();
            travellerType6.save();
            travellerType7.save();

            List<Nationality> nationalityList = new ArrayList<>();
            nationalityList.add(nationality4);

            List<TravellerType> travellerTypeList = new ArrayList<>();
            travellerTypeList.add(travellerType7);

            List<Passport> passports = new ArrayList<>();
            passports.add(passport3);
            passports.add(passport4);

            ArrayList<String> userStrings1 = new ArrayList<>();
            userStrings1.add("Luis");
            userStrings1.add("Sebastian");
            userStrings1.add("Ruiz");
            userStrings1.add("so-secure");
            userStrings1.add("luis@gmail.com");
            userStrings1.add("Male");
            userStrings1.add("some-token");

            ArrayList<String> userStrings2 = new ArrayList<>();
            userStrings2.add("Peter");
            userStrings2.add("");
            userStrings2.add("Andre");
            userStrings2.add("in-your-town");
            userStrings2.add("p.andre@hotmail.com");
            userStrings2.add("Other");
            userStrings2.add("token-token");

            ArrayList<String> userStrings3 = new ArrayList<>();
            userStrings3.add("Steven");
            userStrings3.add("middle");
            userStrings3.add("Austin");
            userStrings3.add("stone-cold");
            userStrings3.add("stoney-steve@gmail.com");
            userStrings3.add("Female");
            userStrings3.add("big-token");

            User user1 = generateMockUser(userStrings1, nationalityList, travellerTypeList, passports);

            nationalityList.remove(0);
            nationalityList.add(nationality3);
            travellerTypeList.remove(0);
            travellerTypeList.add(travellerType2);
            passports.remove(1);
            passports.remove(0);
            passports.add(passport3);
            generateMockUser(userStrings2, nationalityList, travellerTypeList, passports);
            nationalityList.remove(0);
            nationalityList.add(nationality1);
            travellerTypeList.remove(0);
            travellerTypeList.add(travellerType1);
            passports.remove(0);
            passports.add(passport1);
            generateMockUser(userStrings3, nationalityList, travellerTypeList, passports);

            Trip trip = new Trip(tripDestinations, user1, "My trip name");

            trip.save();

            ObjectNode json = Json.newObject();
            json.put("message", "Success resampling the database");
            return ok(json);
        });
    }


    /**
     * Create a user and save to database.
     * NOTE: also creates a passport, nationality, traveller type and gender in the database
     */
    private User generateMockUser(ArrayList<String> userStrings, List<Nationality> nationalities, List<TravellerType> travellerTypes, List<Passport> passports) {
        Security security = new Security();

        String passwordHash = security.hashPassword(userStrings.get(3));
        Timestamp dateOfBirth = new Timestamp(637920534);

        User user = new User(userStrings.get(0), userStrings.get(1), userStrings.get(2), passwordHash, userStrings.get(5), userStrings.get(4), nationalities, travellerTypes, dateOfBirth, passports, userStrings.get(6));
        user.save();

        return user;
    }
}
