package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
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
            Integer arrivalTime = tripDestinationJson.get("arrivalTime").asInt();
            Date departureDate = new Timestamp(tripDestinationJson.get("departureDate").asLong());
            Integer departureTime = tripDestinationJson.get("departureTime").asInt();

            Destination destination = new Destination(null, null, null, null, null, null, -1, new ArrayList<>(), false);
            destination.setDestinationId(destinationId);

            TripDestination tripDestination = new TripDestination(destination, arrivalDate, arrivalTime, departureDate, departureTime);
            tripDestinations.add(tripDestination);

            index++;
        }

        return tripDestinations;
    }

    /**
     * Gets users from userIDS
     * @param userIdsJson
     * @throws exceptions.ForbiddenRequestException if userID is in json
     * @return the list of users
     */
    public List<User> getUsersFromJson(JsonNode userIdsJson, User user) throws ForbiddenRequestException, NotFoundException{
        List<User> users = new ArrayList<>();

        for (JsonNode userIdJson : userIdsJson) {
            int currentUserId = userIdJson.asInt();
            if (currentUserId == user.getUserId()) {
                throw new ForbiddenRequestException("You cannot add yourself to a trip");
            }

            User currentUser = User.find.byId(currentUserId);

            if (currentUser == null) {
                throw new NotFoundException("User not found");
            }

            users.add(currentUser);
        }

        // Add own user to user IDS
        users.add(user);

        return users;
    }
}
