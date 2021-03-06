package models;

import io.ebean.Finder;
import io.ebean.Model;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Types travellers can give themselves on profile, to show which kind
 * of travel they like.
 */
@Entity
public class TravellerType extends Model {

  @Override
  public String toString() {
    return "TravellerType{" +
        "users=" + users +
        ", destinations=" + destinations +
        ", travellerTypeId=" + travellerTypeId +
        ", travellerTypeName='" + travellerTypeName + '\'' +
        '}';
  }

    @ManyToMany
    private List<User> users;

    @ManyToMany
    private List<Destination> destinations;

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
        if (!(obj instanceof TravellerType)) {
            return false;
        }
        TravellerType comparedTravellerType = (TravellerType) obj;
      return travellerTypeId == comparedTravellerType.getTravellerTypeId();
    }

  @Override
  public int hashCode() {
    return Objects.hash(travellerTypeId, travellerTypeName);
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, TravellerType> find = new Finder<>(TravellerType.class);
}
