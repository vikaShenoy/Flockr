package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
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

            Gender gender1 = new Gender("Male");
            Gender gender2 = new Gender("Female");
            Gender gender3 = new Gender("Gender Diverse");

            gender1.save();
            gender2.save();
            gender3.save();

            passport1.save();
            passport2.save();

            nationality1.save();
            nationality2.save();

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
        System.out.println(token);
        Timestamp dateOfBirth = new Timestamp(637920534);

        // NOTE: new gender saved to database
        Gender gender = new Gender("Non Binary");
        gender.save();

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

        User user = new User(firstName, middleName, lastName, passwordHash, gender, email, nationalityList, travellerTypeList, dateOfBirth, passportList, token);
        user.save();
    }
}
