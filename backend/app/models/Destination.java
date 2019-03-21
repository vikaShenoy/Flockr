package models;

import javax.persistence.*;

import io.ebean.*;


/**
 * A destination that a traveller can choose to go to
 */
@Entity
public class Destination extends Model {

    @Id
    private int destId;

    private String destName;

    @OneToOne(cascade = CascadeType.PERSIST)
    private DestinationType destType;


    @OneToOne(cascade = CascadeType.PERSIST)
    private District destDistrict;
    private Double destLat;
    private Double destLon;

    @OneToOne(cascade = CascadeType.PERSIST)
    private Country destCountry;


    public Destination(String destName, DestinationType destType, District destDistrict, Double destLat, Double destLon, Country destCountry) {
        this.destName = destName;
        this.destType = destType;
        this.destDistrict = destDistrict;
        this.destLat = destLat;
        this.destLon = destLon;
        this.destCountry = destCountry;
    }

    public int getDestId() {
        return destId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public String getDestName() {
        return destName;
    }

    public void setDestName(String destName) {
        this.destName = destName;
    }

    public DestinationType getDestType() {
        return destType;
    }

    public void setDestType(DestinationType destType) {
        this.destType = destType;
    }

    public District getDestDistrict() {
        return destDistrict;
    }

    public void setDestDistrict(District destDistrict) {
        this.destDistrict = destDistrict;
    }

    public Double getDestLat() {
        return destLat;
    }

    public void setDestLat(Double destLat) {
        this.destLat = destLat;
    }

    public Double getDestLon() {
        return destLon;
    }

    public void setDestLon(Double destLon) {
        this.destLon = destLon;
    }

    public Country getDestCountry() {
        return destCountry;
    }

    public void setDestCountry(Country destCountry) {
        this.destCountry = destCountry;
    }

    public static final Finder<Long, Destination> find = new Finder<>(Destination.class);
}
