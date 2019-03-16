package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TripDestination {

    public int tripDestinationId;
    private String location;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;

    public TripDestination(@JsonProperty("location") String location, @JsonProperty("startDate") String startDate, @JsonProperty("startTime") String startTime, @JsonProperty("endDate") String endDate, @JsonProperty("endTime") String endTime, @JsonProperty("tripDestinationId") int tripDestinationId) {
        this.tripDestinationId = tripDestinationId;
        this.location = location;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.tripDestinationId = tripDestinationId;
    }



    public String getLocation() {
        return location;
    }

    public int getTripDestinationId() {
        return tripDestinationId;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
