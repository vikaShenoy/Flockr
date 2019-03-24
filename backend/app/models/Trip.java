package models;

import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Trip extends Model {

    @Id
    private int tripId;

    @OneToOne
    private User user;


    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<TripDestination> tripDestinations;


    public Trip(List<TripDestination> tripDestinations, User user) {
        this.tripDestinations = tripDestinations;
        this.user = user;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public List<TripDestination> getTripDestinations() {
        return tripDestinations;
    }

    public void setTripDestinations(List<TripDestination> tripDestinations) {
        this.tripDestinations = tripDestinations;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
