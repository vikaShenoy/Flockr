package controllers;

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

            passport1.save();
            passport2.save();

            return ok();
        });
    }
}
