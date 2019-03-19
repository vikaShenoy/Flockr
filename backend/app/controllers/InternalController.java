package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Gender;
import models.Nationality;
import models.Passport;
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

            gender1.save();
            gender2.save();
            gender3.save();

            passport1.save();
            passport2.save();

            nationality1.save();
            nationality2.save();

            ObjectNode json = Json.newObject();
            json.put("message", "Success resampling the database");
            return ok(json);
        });
    }
}
