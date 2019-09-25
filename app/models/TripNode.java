package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
@Inheritance
public abstract class TripNode extends Model {
    @Id
    protected int tripNodeId;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<UserRole> userRoles = new ArrayList<>();


    @ManyToMany(mappedBy = "parents", cascade=CascadeType.PERSIST)
    protected List<TripNode> tripNodes = new ArrayList<>();

    @JsonIgnore
    @ManyToMany
    // Ebean does not support @ManyToMany on the same entity without manually setting the columns
    @JoinTable(name = "trip_node_parent", joinColumns = @JoinColumn(name = "trip_node_child_id"), inverseJoinColumns = @JoinColumn(name = "trip_node_parent_id"))
    private List<TripNode> parents;


    @JsonIgnore
    @SoftDelete
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

  @JsonIgnore
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

    public TripNode(int tripNodeId) {
        this.tripNodeId = tripNodeId;
    }

    public abstract List<TripNode> getTripNodes();

    public abstract void addTripNode(TripNode tripNode);

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

    public List<TripNode> getParents() {
        return parents;
    }

    public void setParents(List<TripNode> parents) {
        this.parents = parents;
    }

    public void addParent(TripNode parent) {
        this.parents.add(parent);
    }

    public void removeParent(TripNode parent) {
        this.parents.remove(parent);
    }

    public abstract String getNodeType();

    public abstract Destination getDestination();

    public abstract List<User> getUsers();

    public void addUserRole(UserRole userRole) {
        this.userRoles.add(userRole);
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripNode> find = new Finder<>(TripNode.class);
}
