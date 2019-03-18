package controllers;

import models.Gender;
import models.Nationality;
import models.Passport;
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

            Gender gender1 = new Gender("Male");
            Gender gender2 = new Gender("Female");
            Gender gender3 = new Gender("Non-Identifying");

            gender1.save();
            gender2.save();
            gender3.save();

            passport1.save();
            passport2.save();

            nationality1.save();
            nationality2.save();

            return ok();
        });
    }
}
