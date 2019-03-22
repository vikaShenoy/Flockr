package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.*;
import play.libs.Json;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.ok;

public class InternalController {
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

            gender1.save();
            gender2.save();
            gender3.save();

            passport1.save();
            passport2.save();

            nationality1.save();
            nationality2.save();
            nationality3.save();

            destinationType1.save();
            destinationType2.save();



            Destination destination1 = new Destination("Burning Man",destinationType1, district1, 12.1234,12.1234,country1 );
            Destination destination2 = new Destination("Brisbane City",destinationType2, district2, 11.1234,11.1234,country2 );

            destination1.save();
            destination2.save();

            ObjectNode json = Json.newObject();
            json.put("message", "Success resampling the database");
            return ok(json);
        });
    }
}
