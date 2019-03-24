package models;


import io.ebean.Model;

import javax.persistence.*;
import java.sql.Time;
import java.util.Date;

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
}
