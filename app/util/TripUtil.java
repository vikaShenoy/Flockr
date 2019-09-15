package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import exceptions.BadRequestException;
import exceptions.ForbiddenRequestException;
import exceptions.NotFoundException;
import java.util.concurrent.CompletionStage;
import models.*;
import repository.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TripUtil {

    @Inject
    private UserRepository userRepository;

    /**
     * Gets trip to update from from list of trip objects based on ID
     * @param tripNodeJson JSON representation of trip node
     * @param trips Set of all possible trips
     * @return Object representation of trip node
     * @throws NotFoundException Gets thrown when a trip isn't found
     */
    private TripComposite getTripToUpdate(JsonNode tripNodeJson, Set<TripComposite> trips) throws NotFoundException {
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
     * Given the user ids from the request, return a list of users with those ids
     * @param userIdsJson a JSON array with user ids
     * @throws exceptions.ForbiddenRequestException if the own user's id is in the JSON
     * @return the list of users
     */
    public List<User> getUsersFromJson(JsonNode userIdsJson, User user) throws ForbiddenRequestException, NotFoundException{
        List<User> users;
        List<Integer> userIds = new ArrayList<>();

        // parse user if from JSON, add user id to the list of user id integers
        for (JsonNode userIdJson : userIdsJson) {
            int currentUserId = userIdJson.get("userId").asInt();
            userIds.add(currentUserId);
        }

        users = userRepository.getUsersWithIds(userIds);

        if (users.contains(user)) {
            throw new ForbiddenRequestException("You cannot add yourself to a trip");
        }

        if (users.contains(null)) {
            throw new NotFoundException("User not found");
        }

        // Add own user to list of users
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
            int currentUserId =  userIdJson.get("userId").asInt();


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
