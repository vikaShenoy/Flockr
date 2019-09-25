package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import models.TravellerType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Utility class for helping with destinations.l
 */
public class DestinationUtil {

    /**
     * Transforms a json of traveller type ids into a list of traveller types.
     *
     * @param travellerTypeIdsNode json node of traveller type id's.
     * @param allTravellerTypes list of all traveller types.
     * @return the list of traveller types.
     */
    public List<TravellerType> transformTravellerTypes(JsonNode travellerTypeIdsNode, List<TravellerType> allTravellerTypes) {
        List<TravellerType> travellerTypes = new ArrayList<>();

        for (JsonNode travellerTypeIdNode : travellerTypeIdsNode) {
            int travellerTypeId = travellerTypeIdNode.asInt();
            TravellerType currentTravellerType = null;

            for (TravellerType travellerType : allTravellerTypes) {
                if (travellerType.getTravellerTypeId() == travellerTypeId) {
                    currentTravellerType = travellerType;
                }
            }


            if (currentTravellerType == null) {
                throw new CompletionException(new BadRequestException("Could not find traveller type"));
            } else if (travellerTypes.contains(currentTravellerType)) {
                throw new CompletionException(new BadRequestException("Cannot contain duplicate ID's"));
            } else {
                travellerTypes.add(currentTravellerType);
            }
        }

        return travellerTypes;
    }
}
