package controllers;

import actions.ActionState;
import actions.LoggedIn;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigException;
import models.Passport;
import models.User;
import models.Nationality;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.TravellerRepository;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.Date;
import java.text.SimpleDateFormat;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import play.libs.concurrent.HttpExecutionContext;

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

    /**
     * A function that retrieves a travellers details based on the traveller ID given
     * @param travellerId the traveller Id of the traveller to retrieve
     * @param request request Object
     * @return traveller details as a Json object
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> getTraveller(int travellerId, Http.Request request) {

        return travellerRepository.getUserById(travellerId)
                .thenApplyAsync((user) -> {
                    if (!user.isPresent()) {
                        return notFound();
                    }

                    JsonNode userAsJson = Json.toJson(user);
                    System.out.println(userAsJson);

                    return ok(userAsJson);

                }, httpExecutionContext.current());

    }

    /**
     * A function that updates a travellers details based on the traveller ID given.
     * @param travellerId the traveller ID
     * @param request Object to get the JSOn data
     * @return 200 status if update was successful, 500 otherwise
     */
    @With(LoggedIn.class)
    public CompletionStage<Result> updateTraveller(int travellerId, Http.Request request) {
        JsonNode jsonBody = request.body().asJson();


        User user = request.attrs().get(ActionState.USER);



        if (jsonBody.has("firstName")) {
            user.setFirstName(jsonBody.get("firstName").asText());
        }

        if (jsonBody.has("middleName")) {
            user.setMiddleName(jsonBody.get("middleName").asText());
        }

        if (jsonBody.has("lastName")) {
            user.setLastName(jsonBody.get("lastName").asText());
        }

        if (jsonBody.has("dateOfBirth")) {
            try {
                String incomingDate = jsonBody.get("dateOfBirth").asText();
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(incomingDate);
                System.out.println("Date stored in db for user is: " + date);
                user.setDateOfBirth(date);
            } catch (ParseException e) {
                System.out.println(e);
            }
        }

        if (jsonBody.has("gender")) {
            user.setGender(jsonBody.get("gender").asText());
        }

        if (jsonBody.has("nationalities")) {
            JsonNode arrNode = jsonBody.get("nationalities");
            ArrayList<Nationality> nationalities = new ArrayList<>();
            for (JsonNode id : arrNode) {

                Nationality nationality = new Nationality(null);
                nationality.setNationalityId(id.asInt());
                nationalities.add(nationality);

            }
            user.setNationalities(nationalities);
        }

        if (jsonBody.has("passports")) {
            JsonNode arrNode = jsonBody.get("passports");
            ArrayList<Passport> passports = new ArrayList<>();
            for (JsonNode id : arrNode) {

                Passport passport = new Passport(null);
                passport.setPassportId(id.asInt());
                passports.add(passport);

            }
            user.setPassports(passports);
        }

        if (jsonBody.has("gender")) {
            user.setGender(jsonBody.get("gender").asText());
        }

        return supplyAsync(() -> {
            travellerRepository.updateUser(user);
            return ok();
        }, httpExecutionContext.current());
    }

    /**
     * A function that gets a list of all the passports and returns a 200 ok code to the HTTP client
     * @param request Http.Request the HTTP request
     * @return a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getAllPassports(Http.Request request) {
        System.out.println(1);
        return travellerRepository.getAllPassports()
                .thenApplyAsync((passports) -> {
                    return ok(Json.toJson(passports));
                }, httpExecutionContext.current());
    }

    /**
     * A function that gets a list of all the nationalities and returns it with a 200 ok code to the HTTP client
     * @param request Http.Request the http request
     * @return a completion stage and a status code 200 if the request is successful, otherwise returns 500.
     */
    public CompletionStage<Result> getNationalities(Http.Request request) {
        return travellerRepository.getAllNationalities()
                .thenApplyAsync((nationalities) -> {
                    return ok(Json.toJson(nationalities));
                }, httpExecutionContext.current());
    }



    @With(LoggedIn.class)
    public CompletionStage<Result> getTravellers() {
        return travellerRepository.getTravellers()
                .thenApplyAsync(travellers -> {
                    JsonNode travellersJson = Json.toJson(travellers);
                    return ok(travellersJson);
                }, httpExecutionContext.current());
    }
}
