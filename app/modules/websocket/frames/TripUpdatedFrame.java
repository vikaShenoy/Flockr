package modules.websocket.frames;


import models.Trip;

/**
 * Web socket frame for when a trip is updated
 */
public class TripUpdatedFrame implements Frame {

    private Trip trip;

    public TripUpdatedFrame(Trip trip) {
        this.trip = trip;
    }

    @Override
    public String getType() {
        return "tripUpdated";
    }

    public Trip getTrip() {
        return trip;
    }
}
