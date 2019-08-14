package models;

import io.ebean.Finder;
import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * A type of destination that is linked to the destination object.
 */

@Entity
public class DestinationType extends Model {

    @Id
    private int destinationTypeId;

    private String destinationTypeName;

    public DestinationType(String destinationTypeName) {
        this.destinationTypeName = destinationTypeName;
    }

    public int getDestinationTypeId() {
        return destinationTypeId;
    }

    public void setDestinationTypeId(int destinationTypeId) {
        this.destinationTypeId = destinationTypeId;
    }

    public String getDestinationTypeName() {
        return destinationTypeName;
    }

    public void setDestinationTypeName(String destinationTypeName) {
        this.destinationTypeName = destinationTypeName;
    }

    public static final Finder<Integer, DestinationType> find = new Finder<>(DestinationType.class);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DestinationType)) {
            return false;
        }
        DestinationType destinationTypeToCompare = (DestinationType) obj;
        return this.destinationTypeId == destinationTypeToCompare.destinationTypeId;
    }
}


