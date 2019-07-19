package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import exceptions.FailedToSignUpException;
import exceptions.NotFoundException;
import exceptions.ServerErrorException;
import models.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import utils.FakeClient;
import utils.FakePlayClient;
import utils.PlayResultToJson;
import utils.TestState;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TreasureHuntControllerTest {

    @Inject
    private Application application;
    private User user;
    private User otherUser;
    private FakeClient fakeClient;
    private TreasureHunt treasureHunt;
    private Destination editedDestination;

    @Before
    public void setUp() throws ServerErrorException, IOException, FailedToSignUpException, NotFoundException {
        Map<String,String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");

        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);

        TestState.getInstance().setApplication(application);
        TestState.getInstance().setFakeClient(new FakePlayClient(application));

        fakeClient = TestState.getInstance().getFakeClient();
        user = fakeClient.signUpUser("Timmy", "Tester", "timmy@tester.com", "abc123");
        otherUser = fakeClient.signUpUser("Tammy", "Tester", "tammy@tester.com", "abc123");

        DestinationType destinationType = new DestinationType("city");
        Country country = new Country("Test Nation");
        District district = new District("Test District", country);
        Destination destination = new Destination("Test City", destinationType, district, 0.0, 0.0, country, user.getUserId(), true);
        editedDestination = new Destination("Edited Destination", destinationType, district, 0.0, 0.0, country, user.getUserId(), false);

        destinationType.save();
        country.save();
        district.save();
        editedDestination.save();
        destination.save();

        treasureHunt = new TreasureHunt("Test Hunt",
                user.getUserId(),
                destination.getDestinationId(),
                "Its a test",
                Date.from(Instant.now()),
                Date.from(Instant.now().plus(Duration.ofDays(30))));
        treasureHunt.save();
    }

    @After
    public void tearDown() {
        Application application = TestState.getInstance().getApplication();
        Helpers.stop(application);
        TestState.clear();
    }

    @Test
    public void editTreasureHuntGoodName() throws IOException {
        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        ObjectNode treasureHuntObject = Json.newObject();
        treasureHuntObject.put("treasureHuntName", "Edited Hunt");
        Result result = fakeClient.makeRequestWithToken("PUT", treasureHuntObject,
                "/api/treasurehunts/" + treasureHunt.getTreasureHuntId(), user.getToken());
        //Assert response code is correct.
        Assert.assertEquals(200, result.status());
        JsonNode jsonNode = PlayResultToJson.convertResultToJson(result);

        //Assert response is correct.
        Assert.assertTrue(jsonNode.has("treasureHuntName"));
        Assert.assertEquals("Edited Hunt", jsonNode.get("treasureHuntName").asText());

        //Assert database has been updated correctly.
        TreasureHunt editedTreasureHunt = TreasureHunt.find.byId(treasureHunt.getTreasureHuntId());
        Assert.assertNotNull(editedTreasureHunt);
        Assert.assertEquals("Edited Hunt", editedTreasureHunt.getTreasureHuntName());
    }

    @Test
    public void editTreasureHuntGoodDestinationId() throws IOException {

        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        ObjectNode treasureHuntObject = Json.newObject();
        treasureHuntObject.put("treasureHuntDestinationId", editedDestination.getDestinationId());
        Result result = fakeClient.makeRequestWithToken("PUT", treasureHuntObject,
                "/api/treasurehunts/" + treasureHunt.getTreasureHuntId(), user.getToken());
        //Assert response code is correct.
        Assert.assertEquals(200, result.status());
        JsonNode jsonNode = PlayResultToJson.convertResultToJson(result);

        //Assert response is correct.
        Assert.assertTrue(jsonNode.has("treasureHuntDestinationId"));
        Assert.assertEquals(editedDestination.getDestinationId(), jsonNode.get("treasureHuntDestinationId").asInt());

        //Assert database has been updated correctly.
        TreasureHunt editedTreasureHunt = TreasureHunt.find.byId(treasureHunt.getTreasureHuntId());
        Assert.assertNotNull(editedTreasureHunt);
        Assert.assertEquals(editedDestination.getDestinationId(), editedTreasureHunt.getTreasureHuntDestinationId());
    }

    @Test
    public void editTreasureHuntGoodOwnerId() throws IOException {

        FakeClient fakeClient = TestState.getInstance().getFakeClient();
        ObjectNode treasureHuntObject = Json.newObject();
        treasureHuntObject.put("ownerId", otherUser.getUserId());
        Result result = fakeClient.makeRequestWithToken("PUT", treasureHuntObject,
                "/api/treasurehunts/" + treasureHunt.getTreasureHuntId(), user.getToken());
        //Assert response code is correct.
        Assert.assertEquals(200, result.status());
        JsonNode jsonNode = PlayResultToJson.convertResultToJson(result);

        //Assert response is correct.
        Assert.assertTrue(jsonNode.has("treasureHuntDestinationId"));
        Assert.assertEquals(editedDestination.getDestinationId(), jsonNode.get("treasureHuntDestinationId").asInt());

        //Assert database has been updated correctly.
        TreasureHunt editedTreasureHunt = TreasureHunt.find.byId(treasureHunt.getTreasureHuntId());
        Assert.assertNotNull(editedTreasureHunt);
        Assert.assertEquals(editedDestination.getDestinationId(), editedTreasureHunt.getTreasureHuntDestinationId());
    }

    @Test
    public void deleteTreasureHuntGood() {

    }
}