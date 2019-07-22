package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DestinationProposal extends Model {
    @Id
    private int destinationProposalId;

    @ManyToOne(cascade=CascadeType.PERSIST)
    private Destination destination;

    @ManyToMany(cascade=CascadeType.ALL)
    private List<TravellerType> travellerTypes;

    public DestinationProposal(Destination destination, List<TravellerType> travellerTypes) {
        this.destination = destination;
        this.travellerTypes = travellerTypes;
    }

    public List<TravellerType> getTravellerTypes() {
        return travellerTypes;
    }

    public int getDestinationProposalId() {
        return destinationProposalId;
    }

    public Destination getDestination() {
        return destination;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, DestinationProposal> find = new Finder<>(DestinationProposal.class);

}
