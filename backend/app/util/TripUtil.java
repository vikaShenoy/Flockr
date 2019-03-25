package util;

import com.fasterxml.jackson.databind.JsonNode;
import models.Destination;
import models.Trip;
import models.TripDestination;
import models.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripUtil {
    public List<TripDestination> getTripDestinationsFromJson(JsonNode tripDestinationsJson) {
        List<TripDestination> tripDestinations = new ArrayList<>();
        for (JsonNode tripDestinationJson : tripDestinationsJson) {
            int destinationId = tripDestinationJson.get("destinationId").asInt();
            Date arrivalDate = new Date(tripDestinationJson.get("arrivalDate").asLong());
            int arrivalTime = tripDestinationJson.get("arrivalTime").asInt(-1);
            Date departureDate = new Timestamp(tripDestinationJson.get("departureDate").asLong());
            int departureTime = tripDestinationJson.get("departureTime").asInt(-1);

            Destination destination = new Destination(null, null, null, null, null, null);
            destination.setDestinationId(destinationId);

            TripDestination tripDestination = new TripDestination(destination, arrivalDate, arrivalTime, departureDate, departureTime);
            tripDestinations.add(tripDestination);
        }

        return tripDestinations;
    }
}
