package controllers;

import actions.ActionState;
import models.Passport;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import actions.LoggedIn;
import play.mvc.With;
import repository.TravellerRepository;


import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Contains all endpoints associated with travellers
 */
public class TravellerController extends Controller {

    private final TravellerRepository travellerRepository;
    private HttpExecutionContext httpExecutionContext;

    @Inject
    public TravellerController(TravellerRepository travellerRepository, HttpExecutionContext httpExecutionContext) {
        this.travellerRepository = travellerRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    @With(LoggedIn.class)
    public Result addPassport(int travellerid, Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        int passportId = request.body().asJson().get("passportId").asInt();

//        return travellerRepository.getPassportById(passportId)
//        .thenApplyAsync((passports) -> {
//            System.out.println(passports);
//            return ok();
//        }, httpExecutionContext.current());



        return ok();
    }


}
