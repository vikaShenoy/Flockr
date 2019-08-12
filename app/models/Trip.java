package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/** Model for trip. A trip is a planned journey between destinations. */
@Entity
public class Trip extends Model {

  @Id private int tripId;

  @ManyToMany()
  private List<User> users;

  private String tripName;

  @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
  private List<TripDestination> tripDestinations;

  @JsonIgnore
  @SoftDelete
  @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean deleted;

  @JsonIgnore private Timestamp deletedExpiry;

  /**
   * Constructor to create a new trip.
   *
   * @param tripDestinations list of TripDestinations which make up the trip.
   * @param users users who is going on the trip.
   * @param tripName name of the trip.
   */
  public Trip(List<TripDestination> tripDestinations, List<User> users, String tripName) {
    this.tripDestinations = tripDestinations;
    this.users = users;
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


  public List<User> getUsers() {
    return users;
  }


  public void setUsers(List<User> users) {
    this.users = users;
  }

  public String getTripName() {
    return tripName;
  }

  public void setTripName(String tripName) {
    this.tripName = tripName;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public void setDeletedExpiry(Timestamp deletedExpiry) {
    this.deletedExpiry = deletedExpiry;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public Timestamp getDeletedExpiry() {
    return deletedExpiry;
  }

  /** This is required by EBean to make queries on the database. */
  public static final Finder<Integer, Trip> find = new Finder<>(Trip.class);
}
