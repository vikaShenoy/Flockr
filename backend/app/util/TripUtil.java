package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import models.Destination;
import models.Trip;
import models.TripDestination;
import models.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripUtil {
    public List<TripDestination> getTripDestinationsFromJson(JsonNode tripDestinationsJson) throws BadRequestException {
        List<TripDestination> tripDestinations = new ArrayList<>();

        if (tripDestinationsJson.size() < 2) {
            throw new BadRequestException("tripDestinationJson has to be smaller or equal to 2");
        }

        int index = 0;
        for (JsonNode tripDestinationJson : tripDestinationsJson) {
            int destinationId = tripDestinationJson.get("destinationId").asInt();

            // Check that destinations are not contiguous
            if (index > 0 && destinationId == tripDestinations.get(tripDestinations.size() - 1).getDestination().getDestinationId()) {
                throw new BadRequestException("Destinations are contiguous");
            }

            Date arrivalDate = new Date(tripDestinationJson.get("arrivalDate").asLong());
            int arrivalTime = tripDestinationJson.get("arrivalTime").asInt(-1);
            Date departureDate = new Timestamp(tripDestinationJson.get("departureDate").asLong());
            int departureTime = tripDestinationJson.get("departureTime").asInt(-1);

            Destination destination = new Destination(null, null, null, null, null, null, null, false);
            destination.setDestinationId(destinationId);

            TripDestination tripDestination = new TripDestination(destination, arrivalDate, arrivalTime, departureDate, departureTime);
            tripDestinations.add(tripDestination);

            index++;
        }

        return tripDestinations;
    }
}
