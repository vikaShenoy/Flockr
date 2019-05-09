package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import java.util.Date;

import static org.junit.Assert.*;

public class TripUtilTest {
    TripUtil util;
    JsonNode testData1;
    JsonNode testData2;
    JsonNode testData3;

    @Before
    public void setUp() throws Exception {
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

        testNode2.put("destinationId", 1);
        testNode2.put("arrivalDate", arrivalDate.toString());
        testNode2.put("departureDate", departureDate.toString());
        testNode2.put("arrivalTime", arrivalTime);
        testNode2.put("departureTime", departureTime);

        testNode3.put("destinationId", 2);
        testNode3.put("arrivalDate", arrivalDate.toString());
        testNode3.put("departureDate", departureDate.toString());
        testNode3.put("arrivalTime", arrivalTime);
        testNode3.put("departureTime", departureTime);

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
    }

    /**
     * Should throw a bad request exception as there is only one trip destination in
     * the data list.
     */
    @Test
    public void getTripDestinationsFromJsonInsufficientDestinations() {
        try {
            util.getTripDestinationsFromJson(testData1);
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
    public void getTripDestinationsFromJsonContiguousDestinations() {
        try {
            util.getTripDestinationsFromJson(testData2);
            fail("Method should throw BadRequestException, as there is a repeated tripDest.");
        } catch(BadRequestException e) {
            assertTrue("Exception correctly thrown", true);
        }
    }

    /**
     * Should throw no errors as the data is valid (2 different trip destinations)
     */
    @Test
    public void getTripDestinationsFromJsonValid() {
        try {
            util.getTripDestinationsFromJson(testData3);
            assertTrue("Valid data throws no errors", true);
        } catch(BadRequestException e) {
            fail("Method should throw no errors for valid data.");
        }
    }
}