package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
