package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * Types travellers can give themselves on profile, to show which kind
 * of travel they like.
 */
@Entity
public class TravellerType extends Model {

    @ManyToMany
    private List<User> users;

    @Id
    private int travellerTypeId;

    private String travellerTypeName;

    /**
     * Create a new traveller type given the name
     * @param travellerTypeName the name of the new traveller type
     */
    public TravellerType(String travellerTypeName) {
        this.travellerTypeName = travellerTypeName;
    }

    public int getTravellerTypeId() {
        return travellerTypeId;
    }

    public String getTravellerTypeName() {
        return travellerTypeName;
    }

    public void setTravellerTypeId(int travellerTypeId) {
        this.travellerTypeId = travellerTypeId;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != getClass()) {
            return false;
        }

        TravellerType comparedTravellerType = (TravellerType) obj;
        return travellerTypeId == comparedTravellerType.travellerTypeId;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, TravellerType> find = new Finder<>(TravellerType.class);
}
