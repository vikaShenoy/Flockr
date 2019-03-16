package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DestinationType extends Model {

    @Id
    private int destTypeId;

    private String destTypeName;

    public DestinationType(String destTypeName) {
        this.destTypeName = destTypeName;
    }

    public int getDestTypeId() {
        return destTypeId;
    }

    public void setDestTypeId(int destTypeId) {
        this.destTypeId = destTypeId;
    }

    public String getDestTypeName() {
        return destTypeName;
    }

    public void setDestTypeName(String destTypeName) {
        this.destTypeName = destTypeName;
    }
}
