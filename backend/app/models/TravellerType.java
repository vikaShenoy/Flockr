package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.rmi.MarshalledObject;
import java.util.List;

@Entity
public class TravellerType extends Model {


    @Id
    private int travellerTypeId;

    @ManyToMany
    private List<User> users;

    private String travellerTypeName;

    public TravellerType(String travellerTypeName) {
        this.travellerTypeName = travellerTypeName;
    }

    public int getTravellerTypeId() {
        return travellerTypeId;
    }

    public void setTravellerTypeId(int travellerTypeId) {
        this.travellerTypeId = travellerTypeId;
    }

    public String getTravellerTypeName() {
        return travellerTypeName;
    }

    public void setTravellerTypeName(String travellerTypeName) {
        this.travellerTypeName = travellerTypeName;
    }
}
