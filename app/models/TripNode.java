package models;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance
public abstract class TripNode extends Model {
    @Id
    private int id;

    @OneToMany(mappedBy="parent", cascade=CascadeType.PERSIST)
    protected List<TripNode> tripNodes = new ArrayList<>();

    @JsonIgnore
    @ManyToOne
    private TripNode parent;

    protected String name;

    protected TripNode(List<TripNode> tripNodes, String name) {
        this.tripNodes = tripNodes;
        this.name = name;
    }

    public TripNode() {
    }

    public abstract List<TripNode> getTripNodes();

    public abstract void addTripNodes(TripNode tripNode);

    public abstract void removeTripNode(TripNode tripNode);

    public abstract String getName();

    public abstract void setName(String name);

    public void setTripNodes(List<TripNode> tripNodes) {
        this.tripNodes = tripNodes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TripNode getParent() {
        return parent;
    }

    public void setParent(TripNode parent) {
        this.parent = parent;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripNode> find = new Finder<>(TripNode.class);
}
