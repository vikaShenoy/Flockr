package models;

import io.ebean.Finder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

@Entity
public class TripComposite extends TripNode {

  /**
   * This is required by EBean to make queries on the database.
   */
  public static final Finder<Integer, TripComposite> find = new Finder<>(TripComposite.class);
  private String name;

    public TripComposite(List<User> users, String name) {
        this.users = users;
        this.name = name;
    }

   public TripComposite(List<TripNode> tripNodes, List<User> users, String name) {
        super(tripNodes);
        this.users = users;
        this.name = name;
    }

    public TripComposite(int tripNodeId) {
        super(tripNodeId);
    }

  @ManyToMany()
  private List<User> users;

  public TripComposite() {
    this.tripNodes = new ArrayList<>();
  }

  @Override
  public List<TripNode> getTripNodes() {
    return this.tripNodes;
  }

  @Override
  public void addTripNode(TripNode tripNode) {
    this.tripNodes.add(tripNode);
  }

  @Override
  public void removeTripNode(TripNode tripNode) {
    this.tripNodes.remove(tripNode);
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Date getArrivalDate() {
    for (TripNode node : this.tripNodes) {
      Date arrival = node.getArrivalDate();
      if (arrival != null) {
        return arrival;
      }
      Date departure = node.getDepartureDate();
      if (departure != null) {
        return departure;
      }
    }
    return null;
  }

  @Override
  public Integer getArrivalTime() {
    for (TripNode node : this.tripNodes) {
      Date arrival = node.getArrivalDate();
      if (arrival != null) {
        return node.getArrivalTime();
      }
      Date departure = node.getDepartureDate();
      if (departure != null) {
        return node.getDepartureTime();
      }
    }
    return null;
  }

  @Override
  public Date getDepartureDate() {
    List<TripNode> reverse = new ArrayList<>(this.tripNodes);
    Collections.reverse(reverse);
    for (TripNode node : reverse) {
      Date departure = node.getDepartureDate();
      if (departure != null) {
        return departure;
      }
      Date arrival = node.getArrivalDate();
      if (arrival != null) {
        return arrival;
      }
    }
    return null;
  }

  @Override
  public Integer getDepartureTime() {
    List<TripNode> reverse = new ArrayList<>(this.tripNodes);
    Collections.reverse(reverse);
    for (TripNode node : reverse) {
      Date departure = node.getDepartureDate();
      if (departure != null) {
        return node.getDepartureTime();
      }
      Date arrival = node.getArrivalDate();
      if (arrival != null) {
        return node.getArrivalTime();
      }
    }
    return null;
  }

  @Override
  public String getNodeType() {
    return "TripComposite";
  }

  @Override
  public Destination getDestination() {
    return null;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public void addUser(User user) {
    if (!users.contains(user)) {
      users.add(user);
    }
  }

  public void removeUser(User user) {
    users.remove(user);
  }

  @Override
  public String toString() {
    return "TripComposite{" +
            "name='" + name + '\'' +
            ", users=" + users +
            ", tripNodeId=" + tripNodeId +
            ", userRoles=" + userRoles +
            ", tripNodes=" + tripNodes +
            '}';
  }
}
