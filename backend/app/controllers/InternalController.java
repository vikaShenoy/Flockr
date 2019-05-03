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
            Passport passport1 = new Passport("NZ");
            Passport passport2 = new Passport("Australia");
            Passport passport3 = new Passport("Peru");
            Passport passport4 = new Passport("Bolivia");

            Nationality nationality1 = new Nationality("New Zealand");
            Nationality nationality2 = new Nationality("Australia");
            Nationality nationality3 = new Nationality("Afghanistan");
            Nationality nationality4 = new Nationality("Peru");

            DestinationType destinationType1 = new DestinationType("Event");
            DestinationType destinationType2 = new DestinationType("City");

            Country country1 = new Country("United States of America");
            Country country2 = new Country("Australia");

            District district1 = new District("Black Rock City", country1);
            District district2 = new District("New Farm", country2);

            Role admin = new Role(RoleType.ADMIN);
            Role superAdmin = new Role(RoleType.SUPER_ADMIN);
            Role traveller = new Role(RoleType.TRAVELLER);

            admin.save();
            superAdmin.save();
            traveller.save();

            country1.save();
            country2.save();

            district1.save();
            district2.save();

            passport1.save();
            passport2.save();
            passport3.save();
            passport4.save();

            nationality1.save();
            nationality2.save();
            nationality3.save();
            nationality4.save();

            destinationType1.save();
            destinationType2.save();

            Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234, country1 );
            Destination destination2 = new Destination("Brisbane City",destinationType2, district2, 11.1234,11.1234, country2 );

            destination1.save();
            destination2.save();

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

            List<Role> superAdminRoles = new ArrayList<>();
            superAdminRoles.add(superAdmin);

            List<Role> adminRoles = new ArrayList<>();
            adminRoles.add(admin);

            User user1 = generateMockUser("Luis", "Sebastian", "Ruiz", "so-secure", "luis@gmail.com", "Male", "some-token", nationalityList, travellerTypeList, passports, superAdminRoles);
            nationalityList.remove(0);
            nationalityList.add(nationality3);
            travellerTypeList.remove(0);
            travellerTypeList.add(travellerType2);
            passports.remove(1);
            passports.remove(0);
            passports.add(passport3);
            generateMockUser("Peter", "", "Andre", "in-your-town", "p.andre@hotmail.com", "Other", "token-token", nationalityList, travellerTypeList, passports, adminRoles);
            nationalityList.remove(0);
            nationalityList.add(nationality1);
            travellerTypeList.remove(0);
            travellerTypeList.add(travellerType1);
            passports.remove(0);
            passports.add(passport1);
            generateMockUser("Steven", "middle", "Austin", "stone-cold", "stoney-steve@gmail.com", "Female", "big-token", nationalityList, travellerTypeList, passports, new ArrayList<>());

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
    private User generateMockUser(String firstName, String middleName, String lastName, String password, String email, String gender, String token, List<Nationality> nationalities, List<TravellerType> travellerTypes, List<Passport> passports, List<Role> roles) {
        Security security = new Security();

        String passwordHash = security.hashPassword(password);
        Timestamp dateOfBirth = new Timestamp(637920534);

        User user = new User(firstName, middleName, lastName, passwordHash, gender, email, nationalities, travellerTypes, dateOfBirth, passports, roles, token);
        user.save();

        return user;
    }
}
