package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import models.Gender;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TravellerRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import play.libs.concurrent.HttpExecutionContext;


public class TravellerController extends Controller {
    private final TravellerRepository travellerRepository;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public TravellerController(TravellerRepository travellerRepository, HttpExecutionContext httpExecutionContext) {
        this.travellerRepository = travellerRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    @With(LoggedIn.class)
    public CompletionStage<Result> updateTraveller(int travellerId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();

        User user = request.attrs().get(ActionState.USER);

        System.out.println(user.getFirstName());
        System.out.println(user.getGender().getGenderId());

        if (jsonBody.has("firstName")) {
            user.setFirstName(jsonBody.get("firstName").asText());
        }

        if (jsonBody.has("middleName")) {
            user.setMiddleName(jsonBody.get("middleName").asText());
        }

        if (jsonBody.has("lastName")) {
            user.setLastName(jsonBody.get("firstName").asText());
        }

//        if (jsonBody.has("dateOfBirth")) {
//            user.setDateOfBirth(jsonBody.get("middleName").asText());
//        }

        if (jsonBody.has("genderId")) {
            Gender gender = new Gender(null);
            gender.setGenderId(jsonBody.get("genderId").asInt());
            user.setGender(gender);
        }

            return supplyAsync(() -> {
                travellerRepository.updateUser(user);
                return ok();
            }, httpExecutionContext.current());


    }
}
