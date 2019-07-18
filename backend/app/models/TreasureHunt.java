package models;

import exceptions.NotFoundException;
import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.Optional;

@Entity
public class TreasureHunt extends Model {

    @Id
    private int treasureHuntId;

    private String treasureHuntName;

    @ManyToOne(cascade = CascadeType.ALL)
    private User owner;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL)
    private Destination treasureHuntDestination;

    private String riddle;

    private Date startDate;

    private Date endDate;


    public TreasureHunt(String treasureHuntName, int treasureHuntDestinationId, String riddle,
                        Date startDate, Date endDate) throws NotFoundException {
        this.treasureHuntName = treasureHuntName;
        setTreasureHuntDestinationId(treasureHuntDestinationId);
        this.riddle = riddle;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getTreasureHuntId() {
        return treasureHuntId;
    }

    public void setTreasureHuntId(int treasureHuntId) {
        this.treasureHuntId = treasureHuntId;
    }

    public String getTreasureHuntName() {
        return treasureHuntName;
    }

    public void setTreasureHuntName(String treasureHuntName) {
        this.treasureHuntName = treasureHuntName;
    }

    public int getTreasureHuntDestinationId() {
        return treasureHuntDestination.getDestinationId();
    }

    public void setTreasureHuntDestinationId(int treasureHuntDestinationId) throws NotFoundException {
        Optional<Destination> dest = Destination.find.query().where().eq("destination_id", treasureHuntDestinationId).findOneOrEmpty();
        if (!dest.isPresent()) {
            throw new NotFoundException("Destination not found.");
        }
        this.treasureHuntDestination = dest.get();
    }

    public String getRiddle() {
        return riddle;
    }

    public void setRiddle(String riddle) {
        this.riddle = riddle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getOwnerId() {
        return owner.getUserId();
    }

    /**
     * Setter for the owner of the treasure hunt.
     *
     * @param ownerId the id of the owner.
     * @throws NotFoundException
     */
    public void setOwnerId(int ownerId) throws NotFoundException {
        Optional<User> optionalUser = User.find.query().where().eq("user_id", ownerId).findOneOrEmpty();
        if (!optionalUser.isPresent()) {
            throw new NotFoundException("This user does not exist");
        }
        this.owner = optionalUser.get();
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, TreasureHunt> find = new Finder<>(TreasureHunt.class);
}
