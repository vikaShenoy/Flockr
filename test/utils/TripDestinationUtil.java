package utils;

import java.util.Date;

public class TripDestinationUtil {
    private Date arrivalDate;
    private Date departureDate;
    private int arrivalTime;
    private int departureTime;
    private int destinationId;

    public TripDestinationUtil(Date arrivalDate, Date departureDate, int arrivalTime, int departureTime, int destinationId) {
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.departureDate = departureDate;
        this.destinationId = destinationId;
    }
}
