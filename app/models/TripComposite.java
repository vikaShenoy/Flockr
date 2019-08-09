package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
public class TripComposite extends TripNode {

    private String name;

    @ManyToMany()
    private List<User> users;

    public TripComposite(List<TripNode> tripNodes, List<User> users, String name) {
        super(tripNodes);
        this.users = users;
        this.name = name;
    }

    public TripComposite(int tripNodeId) {
        this.setTripNodeId(tripNodeId);
    }

    public TripComposite() {
        this.tripNodes = new ArrayList<>();
    }

    @Override
    public List<TripNode> getTripNodes() {
        return this.tripNodes;
    }

    @Override
    public void addTripNodes(TripNode tripNode) {
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

    @Override
    public Date getArrivalDate() {
        for (TripNode node: this.tripNodes)  {
            Date arrival = node.getArrivalDate();
            if (arrival != null) return arrival;
            Date departure = node.getDepartureDate();
            if (departure != null) return departure;
        }
        return null;
    }

    @Override
    public Integer getArrivalTime() {
        for (TripNode node: this.tripNodes)  {
            Date arrival = node.getArrivalDate();
            if (arrival != null) return node.getArrivalTime();
            Date departure = node.getDepartureDate();
            if (departure != null) return node.getDepartureTime();
        }
        return null;
    }

    @Override
    public Date getDepartureDate() {
        List<TripNode> reverse = new ArrayList<>(this.tripNodes);
        Collections.reverse(reverse);
        for (TripNode node: reverse)  {
            Date departure = node.getDepartureDate();
            if (departure != null) return departure;
            Date arrival = node.getArrivalDate();
            if (arrival != null) return arrival;
        }
        return null;
    }

    @Override
    public Integer getDepartureTime() {
        List<TripNode> reverse = new ArrayList<>(this.tripNodes);
        Collections.reverse(reverse);
        for (TripNode node: reverse)  {
            Date departure = node.getDepartureDate();
            if (departure != null) return node.getDepartureTime();
            Date arrival = node.getArrivalDate();
            if (arrival != null) return node.getArrivalTime();
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripComposite> find = new Finder<>(TripComposite.class);
}
