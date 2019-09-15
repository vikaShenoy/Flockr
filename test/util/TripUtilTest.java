package util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import exceptions.NotFoundException;

import java.util.*;

import models.TripComposite;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.test.Helpers;
import repository.UserRepository;
import utils.FakeClient;
import utils.TestState;

public class TripUtilTest {
    TripUtil util;
    JsonNode testData1;
    JsonNode testData2;
    JsonNode testData3;
    Application application;
    Set<TripComposite> tripComposites;

    @Before
    public void setUp() {
        Map<String, String> testSettings = new HashMap<>();
        testSettings.put("db.default.driver", "org.h2.Driver");
        testSettings.put("db.default.url", "jdbc:h2:mem:testdb;MODE=MySQL;");
        testSettings.put("play.evolutions.db.default.enabled", "true");
        testSettings.put("play.evolutions.db.default.autoApply", "true");
        testSettings.put("play.evolutions.db.default.autoApplyDowns", "true");
        application = Helpers.fakeApplication(testSettings);
        Helpers.start(application);

        util = new TripUtil();
        Date arrivalDate = new Date();
        Date departureDate = new Date();
        int arrivalTime = 100;
        int departureTime = 200;

        ObjectNode testNode1 = Json.newObject();
        ObjectNode testNode2 = Json.newObject();
        ObjectNode testNode3 = Json.newObject();
        testNode1.put("destinationId", 1);
        testNode1.put("arrivalDate", arrivalDate.toString());
        testNode1.put("departureDate", departureDate.toString());
        testNode1.put("arrivalTime", arrivalTime);
        testNode1.put("departureTime", departureTime);
        testNode1.put("nodeType", "TripDestinationLeaf");

        testNode2.put("destinationId", 1);
        testNode2.put("arrivalDate", arrivalDate.toString());
        testNode2.put("departureDate", departureDate.toString());
        testNode2.put("arrivalTime", arrivalTime);
        testNode2.put("departureTime", departureTime);
        testNode2.put("nodeType", "TripDestinationLeaf");

        testNode3.put("destinationId", 2);
        testNode3.put("arrivalDate", arrivalDate.toString());
        testNode3.put("departureDate", departureDate.toString());
        testNode3.put("arrivalTime", arrivalTime);
        testNode3.put("departureTime", departureTime);
        testNode3.put("nodeType", "TripDestinationLeaf");

        // Make a list
        ArrayNode testArray = Json.newArray();
        ArrayNode testArray3 = Json.newArray();
        testArray.add(testNode1);
        testData1 = testArray;

        // Data for second test with identical trip added
        testArray.add(testNode2);
        testData2 = testArray;

        // Data for third test, with two valid trips
        testArray3.add(testNode1);
        testArray3.add(testNode3);
        testData3 = testArray3;

        tripComposites = new HashSet<>();

    }

    /**
     * Should throw a bad request exception as there is only one trip destination in
     * the data list.
     */
    @Test
    public void getTripDestinationsFromJsonInsufficientDestinations() throws NotFoundException {
        try {
            util.getTripNodesFromJson(testData1, tripComposites);
            fail("Method should throw BadRequestException, as there is only 1 tripdest.");
        } catch(BadRequestException e) {
            assertTrue("Exception correctly thrown", true);
        }
    }

    /**
     * Should throw a bad request exception as there is a repeated trip destination in
     * the data list.
     */
    @Test
    public void getTripDestinationsFromJsonContiguousDestinations() throws NotFoundException {
        try {
            util.getTripNodesFromJson(testData2, tripComposites);
            fail("Method should throw BadRequestException, as there is a repeated tripDest.");
        } catch(BadRequestException e) {
            assertTrue("Exception correctly thrown", true);
        }
    }

    /**
     * Should throw no errors as the data is valid (2 different trip destinations)
     */
    @Test
    public void getTripDestinationsFromJsonValid() throws NotFoundException {
        try {
            util.getTripNodesFromJson(testData3, tripComposites);
            assertTrue("Valid data throws no errors", true);
        } catch(BadRequestException e) {
            fail("Method should throw no errors for valid data.");
        }
    }

    @After
    public void tearDown() {
        Helpers.stop(application);
        TestState.clear();
    }

}