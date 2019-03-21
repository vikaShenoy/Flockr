package models;


import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Date;

@Entity
public class TripDestination extends Model {

    @Id
    private int tripDestinationId;

    @OneToOne
    private Destination destination;

    private Date tripDestArrival;
    private Date tripDestDeparture;

    public TripDestination(Destination destination, Date tripDestArrival, Date tripDestDeparture) {
        this.destination = destination;
        this.tripDestArrival = tripDestArrival;
        this.tripDestDeparture = tripDestDeparture;
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

    public Date getTripDestArrival() {
        return tripDestArrival;
    }

    public void setTripDestArrival(Date tripDestArrival) {
        this.tripDestArrival = tripDestArrival;
    }

    public Date getTripDestDeparture() {
        return tripDestDeparture;
    }

    public void setTripDestDeparture(Date tripDestDeparture) {
        this.tripDestDeparture = tripDestDeparture;
    }
}
