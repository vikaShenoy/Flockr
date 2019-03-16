package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserTravellerTypes extends Model {

    @Id
    private int userTravellerTypeId;
    private int travellerTypeId;
    private int userId;

    public UserTravellerTypes (int travellerTypeId, int userId) {
        this.travellerTypeId = travellerTypeId;
        this.userId = userId;
    }

    public int getTravellerTypeId() {
        return travellerTypeId;
    }

    public void setTravellerTypeId(int travellerTypeId) {
        this.travellerTypeId = travellerTypeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
