package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Trip {
    private ArrayList<TripDestination> tripDestinations;

    private int tripUserId;

    private int tripId;

    public ArrayList<TripDestination> getTripDestinations() {
        return tripDestinations;
    }

    public int getTripUserId() {
        return tripUserId;
    }

    public int getTripId() {
        return tripId;
    }

    public Trip(@JsonProperty("tripDestinations") ArrayList<TripDestination> tripDestinations, @JsonProperty("tripUserId") int tripUserId, @JsonProperty("tripId") int tripId) {
        this.tripId = tripId;
        this.tripUserId = tripUserId;
        this.tripDestinations = tripDestinations;
    }
}
