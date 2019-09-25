package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import java.sql.Timestamp;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class DestinationProposal extends Model {
    @Id
    private int destinationProposalId;

    @ManyToOne(cascade=CascadeType.PERSIST)
    private Destination destination;

    @ManyToMany(cascade=CascadeType.ALL)
    private List<TravellerType> travellerTypes;

    @SoftDelete
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @JsonIgnore
    private Timestamp deletedExpiry;

    @JsonIgnore
    @ManyToOne
    private User user;


    public DestinationProposal(Destination destination, List<TravellerType> travellerTypes, User user) {
        this.destination = destination;
        this.travellerTypes = travellerTypes;
        this.user = user;
    }

    public void setDeletedExpiry(Timestamp deletedExpiry) {
        this.deletedExpiry = deletedExpiry;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TravellerType> getTravellerTypes() {
        return travellerTypes;
    }

  public void setTravellerTypes(List<TravellerType> travellerTypes) {
    this.travellerTypes = travellerTypes;
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
