package steps;

import com.fasterxml.jackson.databind.node.ArrayNode;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import models.*;
import org.junit.Assert;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import util.Security;
import utils.PlayResultToJson;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.route;

public class searchTravellerFilterSteps {

    private Result result;
    private ArrayNode array;

    @And("^full users with the following information exist:$")
    public void fullUsersWithTheFollowingInformationExist(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        for (Map<String,String> row: rows) {
            User user = createUser(row);
            Assert.assertNotEquals(0, user.getUserId());
        }
    }

    /**
     * Helper function for tests to create a user from the strings representing it's
     * parameters.
     *
     * @param row the row of the data table containing the users details
     * @return the User object.
     */
    private User createUser(Map<String,String> row) {
        String firstName = row.get("firstName");
        String middleName = row.get("middleName");
        String lastName = row.get("lastName");
        Security security = new Security();
        String passwordHash = security.hashPassword(row.get("password"));
        String gender = row.get("gender");
        String email = row.get("email");
        List<Nationality> nationalities = Nationality.find.query().where().eq("nationality_name", row.get("nationality")).findList();
        List<TravellerType> travellerTypes = TravellerType.find.query().where().eq("traveller_type_name", row.get("travellerType")).findList();
        List<Passport> passports = Passport.find.query().where().eq("passport_country", row.get("passport")).findList();
        Timestamp dateOfBirth = new Timestamp(Integer.parseInt(row.get("dateOfBirth")));
        List<Role> roles = Role.find.query().where().eq("role_type", row.get("role")).findList();
        String token = "";

        User user = new User(firstName, middleName, lastName, passwordHash, gender, email, nationalities, travellerTypes, dateOfBirth, passports, roles, token);
        user.save();

        return user;
    }

    @When("I want all types of nationalities from the database")
    public void iRequestNationalitiesFromTheDatabase() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/nationalities");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the {int} nationality id")
    public void iSearchTravellersWithTheNationalityId(Integer nationalityId) throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&nationality=" + nationalityId);
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Male")
    public void iSearchTravellersWithTheGenderMale() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Male");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Female")
    public void iSearchTravellersWithTheGenderFemale() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Female");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the gender Other")
    public void iSearchTravellersWithTheGenderOther() throws IOException {
        User user = TestState.getInstance().getUser(0);
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&gender=Other");
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @When("I search travellers with the traveller type ID {int}")
    public void iSearchTravellersWithTheTravellerTypeID(Integer travellerTypeId) throws IOException {
        User user = TestState.getInstance().getUser(0);

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method("GET")
                .header("authorization", user.getToken())
                .uri("/api/users/search?ageMin=1143441273223&ageMax=-2075388926777&travellerType=" + travellerTypeId.toString());
        Application application = TestState.getInstance().getApplication();
        this.result = route(application, request);
        this.array = (ArrayNode) PlayResultToJson.convertResultToJson(result);

        Assert.assertTrue(array.size() > 0);
        Assert.assertEquals(200, result.status());
    }

    @Then("I get a list of all nationalities as follows:")
    public void iGetAListOfAllNationalitiesAsFollows(DataTable dataTable) {

        List<Map<String, String>> expectedResults = dataTable.asMaps();
        for (int i = 0; i < expectedResults.size(); i++) {
            Assert.assertEquals(Integer.parseInt(expectedResults.get(i).get("nationalityId")), this.array.get(i).get("nationalityId").asInt());
            Assert.assertEquals(expectedResults.get(i).get("nationalityName"), this.array.get(i).get("nationalityName").asText());
        }
    }

    @Then("I get the following [{string}] emails")
    public void iGetTheFollowing(String email) {
        Assert.assertEquals(email, this.array.get(0).get("email").asText());
    }
}
