package models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class DestinationProposal {

    @ManyToOne
    private Destination destination;

    @Id
    private int destinationProposalId;

    @ManyToMany(cascade=CascadeType.PERSIST)
    private List<TravellerType> travellerTypes;

}
