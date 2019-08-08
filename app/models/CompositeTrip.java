package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CompositeTrip extends TripNode {

    protected CompositeTrip(List<TripNode> tripNodes, String name) {
        super(tripNodes, name);
    }

    public CompositeTrip() {
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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, CompositeTrip> find = new Finder<>(CompositeTrip.class);
}
