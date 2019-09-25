package util;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.BadRequestException;
import models.TravellerType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

public class DestinationUtil {

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
