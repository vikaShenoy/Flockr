package models;


import io.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * TripDestinations are a combination of destinations and the date/time at which
 * a user arrives and leaves the destination as part of their trip.
 */
@Entity
public class TripDestination extends Model {

    @Id
    private int tripDestinationId;

    @ManyToOne
    @JoinColumn
    private Trip trip;

    @ManyToOne
    @JoinColumn
    private Destination destination;

    private Date arrivalDate;
    private int arrivalTime;
    private Date departureDate;
    private int departureTime;

    /**
     * Constructor to create a new trip destination.
     * @param destination the destination the user is visiting as part of their trip.
     * @param arrivalDate date on which this destination is arrived at as part of the trip.
     * @param arrivalTime time at which this destination is arrived at as part of the trip.
     * @param departureDate date on which this destination is left as part of the trip.
     * @param departureTime time on which this destination is left as part of the trip.
     */
    public TripDestination(Destination destination, Date arrivalDate, int arrivalTime, Date departureDate, int departureTime) {
        this.destination = destination;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
    }

    public int getTripDestinationId() {
        return tripDestinationId;
    }

    public void setTripDestinationId(int tripDestinationId) {
        this.tripDestinationId = tripDestinationId;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }
}
