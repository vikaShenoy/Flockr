package modules.websocket.frames;


import models.TripComposite;
import models.TripComposite;

/**
 * Web socket frame for when a trip is updated
 */
public class TripUpdatedFrame implements Frame {

    private TripComposite trip;

    public TripUpdatedFrame(TripComposite trip) {
        this.trip = trip;
    }

    @Override
    public String getType() {
        return "tripUpdated";
    }

    public TripComposite getTrip() {
        return trip;
    }
}
