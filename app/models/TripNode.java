package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.JsonIgnore;
import io.ebean.annotation.SoftDelete;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance
public abstract class TripNode extends Model {
    @Id
    private int tripNodeId;

    @OneToMany(mappedBy="parent", cascade=CascadeType.PERSIST)
    protected List<TripNode> tripNodes = new ArrayList<>();

    @JsonIgnore
    @ManyToOne
    private TripNode parent;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @SoftDelete
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private Timestamp deletedExpiry;

    /**
     * Constructor to create a new trip.
     * @param tripNodes list of TripDestinations which make up the trip.
     */
    protected TripNode(List<TripNode> tripNodes) {
        this.tripNodes = tripNodes;
    }

    public TripNode() {
    }

    public abstract List<TripNode> getTripNodes();

    public abstract void addTripNodes(TripNode tripNode);

    public abstract void removeTripNode(TripNode tripNode);

    public abstract String getName();

    public abstract Date getArrivalDate();

    public abstract Integer getArrivalTime();

    public abstract Date getDepartureDate();

    public abstract Integer getDepartureTime();

    public void setTripNodes(List<TripNode> tripNodes) {
        this.tripNodes = tripNodes;
    }

    public int getTripNodeId() {
        return tripNodeId;
    }

    public void setTripNodeId(int id) {
        this.tripNodeId = id;
    }

    public TripNode getParent() {
        return parent;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Timestamp getDeletedExpiry() {
        return deletedExpiry;
    }

    public void setDeletedExpiry(Timestamp deletedExpiry) {
        this.deletedExpiry = deletedExpiry;
    }

    public void setParent(TripNode parent) {
        this.parent = parent;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripNode> find = new Finder<>(TripNode.class);
}
