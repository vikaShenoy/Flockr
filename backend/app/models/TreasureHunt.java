package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class TreasureHunt extends Model {

    @Id
    private int treasureHuntId;

    private String treasureHuntName;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL)
    private Destination treasureHuntDestination;

    private String riddle;

    private Date startDate;

    private Date endDate;

    public TreasureHunt(String treasureHuntName, Destination treasureHuntDestination, String riddle,
                        Date startDate, Date endDate) {
        this.treasureHuntName = treasureHuntName;
        this.treasureHuntDestination = treasureHuntDestination;
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

    public Destination getTreasureHuntDestination() {
        return treasureHuntDestination;
    }

    public void setTreasureHuntDestination(Destination treasureHuntDestination) {
        this.treasureHuntDestination = treasureHuntDestination;
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
    }y

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, TreasureHunt> find = new Finder<>(TreasureHunt.class);
}
