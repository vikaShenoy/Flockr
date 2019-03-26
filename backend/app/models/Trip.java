package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Trip extends Model {

    @Id
    private int tripId;

    @ManyToOne
    private User user;

    private String tripName;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<TripDestination> tripDestinations;


    public Trip(List<TripDestination> tripDestinations, User user, String tripName) {
        this.tripDestinations = tripDestinations;
        this.user = user;
        this.tripName = tripName;
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

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public static final Finder<Integer, Trip> find = new Finder<>(Trip.class);
}
