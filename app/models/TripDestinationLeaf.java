package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TripDestinationLeaf extends TripNode {

    public TripDestinationLeaf(List<TripNode> tripNodes, String name) {
        super(tripNodes, name);
    }

    public TripDestinationLeaf() {
        this.tripNodes = new ArrayList<>();
    }

    @Override
    public List<TripNode> getTripNodes() {
        return this.tripNodes;
    }

    @Override
    public void addTripNodes(TripNode tripNode) {
        return;
    }

    @Override
    public void removeTripNode(TripNode tripNode) {
        return;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripDestinationLeaf> find = new Finder<>(TripDestinationLeaf.class);
}
