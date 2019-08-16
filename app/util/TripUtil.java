package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import models.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TripUtil {
    /**
     * Gets trip to update from from list of trip objects based on ID
     * @param tripNodeJson JSON representation of trip node
     * @param trips Set of all possible trips
     * @return Object representation of trip node
     * @throws NotFoundException Gets thrown when a trip isn't found
     */
    public TripComposite getTripToUpdate(JsonNode tripNodeJson, Set<TripComposite> trips) throws NotFoundException {
        int tripNodeId = tripNodeJson.get("tripNodeId").asInt();
        TripComposite tripComposite = null;

        for (TripComposite currentCompositeTrip : trips) {
            if (tripNodeId == currentCompositeTrip.getTripNodeId()) {
                tripComposite = currentCompositeTrip;
            }
        }

        if (tripComposite == null) {
            throw new NotFoundException("Trip not found");
        }

        return tripComposite;
    }

    /**
     * Converts tripNodes from json to objects
     * @param tripNodesJson JSON representation of trip nodes
     * @param trips List of all trips to filter out of
     * @return Object representation of trip notes
     * @throws BadRequestException when there are less than 2 destinations or destinations are contiguous
     * @throws NotFoundException Gets thrown when a trip is not found
     */
    public List<TripNode> getTripNodesFromJson(JsonNode tripNodesJson, Set<TripComposite> trips) throws BadRequestException, NotFoundException {
        List<TripNode> tripNodes = new ArrayList<>();

        if (tripNodesJson.size() < 2) {
            throw new BadRequestException("trip nodes has to be larger or equal to 2");
        }



        if (checkContiguousDestinations(tripNodesJson)) {
            throw new BadRequestException("Destinations cannot be contiguous");
        }

        for (JsonNode tripNodeJson : tripNodesJson) {
            if (tripNodeJson.get("nodeType").asText().equals("TripComposite")) {
                TripComposite tripComposite = getTripToUpdate(tripNodeJson, trips);
                tripNodes.add(tripComposite);
            } else {
                int destinationId = tripNodeJson.get("destinationId").asInt();
                Date arrivalDate = !tripNodeJson.has("arrivalDate") ? null : new Date(tripNodeJson.get("arrivalDate").asLong());
                Integer arrivalTime = !tripNodeJson.has("arrivalTime") ? null : tripNodeJson.get("arrivalTime").asInt();
                Date departureDate = !tripNodeJson.has("departureDate") ? null : new Timestamp(tripNodeJson.get("departureDate").asLong());
                Integer departureTime = !tripNodeJson.has("departureTime") ? null : tripNodeJson.get("departureTime").asInt();
                Destination destination = Destination.find.byId(destinationId);
                TripDestinationLeaf tripDestination = new TripDestinationLeaf(destination, arrivalDate, arrivalTime, departureDate, departureTime);
                tripNodes.add(tripDestination);
            }
        }
        return tripNodes;
    }

    /**
     * Check the array of trip nodes to find whether it contains contiguous destinations.
     * @param tripNodes JsonNode containing an array of TripNode json objects.
     * @return true if contiguous destinations are present, false otherwise.
     */
    public boolean checkContiguousDestinations(JsonNode tripNodes) {
        int lastDestinationId = 0;
        int currentDestinationId;
        for (JsonNode tripNode : tripNodes) {
            String nodeType = tripNode.get("nodeType").asText();
            if (nodeType.equals("TripDestinationLeaf")) {
                currentDestinationId = tripNode.get("destinationId").asInt();
                if (currentDestinationId == lastDestinationId) {
                    return true;
                }
                lastDestinationId = currentDestinationId;
            }
        }
        return false;
    }

    /**
     * Gets users from userIDS
     * @param userIdsJson
     * @throws exceptions.ForbiddenRequestException if userID is in json
     * @return the list of users
     */
    public List<User> getUsersFromJson(JsonNode userIdsJson, User user, List<User> allUsers) throws ForbiddenRequestException, NotFoundException{
        List<User> users = new ArrayList<>();

        for (JsonNode userIdJson : userIdsJson) {
            int currentUserId = userIdJson.asInt();
            if (currentUserId == user.getUserId()) {
                throw new ForbiddenRequestException("You cannot add yourself to a trip");
            }

            User currentUser = null;

            for (User potentialUser : allUsers)  {
                if (currentUserId == potentialUser.getUserId()) {
                    currentUser = potentialUser;
                    break;
                }
            }


            if (currentUser == null) {
                throw new NotFoundException("User not found");
            }

            users.add(currentUser);
        }

        // Add own user to user IDS
        users.add(user);

        return users;
    }

    /**
     * Gets users from user IDS for editing a trip
     * @return the list of users
     */
    public List<User> getUsersFromJsonEdit(JsonNode userIdsJson, List<User> allUsers) throws NotFoundException,
            ForbiddenRequestException {
        // Used in the case where a user is editing a trip node and doesn't send any user ids.
        // Not ideal but this is the simplest way to handle it.
        if (userIdsJson == null) {
            return null;
        }

        List<User> users = new ArrayList<>();

        for (JsonNode userIdJson : userIdsJson) {
            int currentUserId = userIdJson.asInt();

            User currentUser = null;

            for (User potentialUser : allUsers)  {
                if (currentUserId == potentialUser.getUserId()) {
                    currentUser = potentialUser;
                    break;
                }
            }

            if (currentUser == null) {
                throw new NotFoundException("User not found");
            }

            users.add(currentUser);
        }

        if (users.size() == 0) {
            throw new ForbiddenRequestException("You cannot have no users in a group trip");
        }

        return users;
    }
}
