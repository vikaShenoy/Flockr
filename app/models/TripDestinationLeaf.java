package models;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class TripDestinationLeaf extends TripNode {

    @ManyToOne
    @JoinColumn
    private Destination destination;

    private Date arrivalDate;
    private Integer arrivalTime;
    private Date departureDate;
    private Integer departureTime;

    public TripDestinationLeaf(Destination destination, Date arrivalDate, Integer arrivalTime, Date departureDate, Integer departureTime) {
        super(new ArrayList<>());
        this.destination = destination;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
    }

    public TripDestinationLeaf() {
        super(new ArrayList<>());
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
        return this.destination.getDestinationName();
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    @Override
    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public Integer getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Integer arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    @Override
    public Integer getDepartureTime() {
        return departureTime;
    }

    @Override
    public Class getNodeType() {
        return this.getClass();
    }

    public void setDepartureTime(Integer departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, TripDestinationLeaf> find = new Finder<>(TripDestinationLeaf.class);
}
