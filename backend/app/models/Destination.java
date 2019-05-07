package models;

import javax.persistence.*;

import io.ebean.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.List;


/**
 * A destination that a traveller can choose to go to.
 */
@Entity
public class Destination extends Model {


    @Id
    private int destinationId;

    private String destinationName;

    @ManyToOne
    private DestinationType destinationType;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL)
    private List<TripDestination> tripDestinations;

    @ManyToOne
    private District destinationDistrict;
    private Double destinationLat;
    private Double destinationLon;

    @ManyToOne
    private Country destinationCountry;

    /**
     * Constructor.
     * @param destinationName name of the destination
     * @param destinationType type of destination
     * @param destinationDistrict district the destination is in
     * @param destinationLat latitiude of the destination
     * @param destinationLon longitude of the destination
     * @param destinationCountry country the destination is in
     */
    public Destination(String destinationName, DestinationType destinationType, District destinationDistrict, Double destinationLat, Double destinationLon, Country destinationCountry) {
        this.destinationName = destinationName;
        this.destinationType = destinationType;
        this.destinationDistrict = destinationDistrict;
        this.destinationLat = destinationLat;
        this.destinationLon = destinationLon;
        this.destinationCountry = destinationCountry;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public District getDestinationDistrict() {
        return destinationDistrict;
    }

    public void setDestinationDistrict(District destinationDistrict) {
        this.destinationDistrict = destinationDistrict;
    }

    public Double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(Double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public Double getDestinationLon() {
        return destinationLon;
    }

    public void setDestinationLon(Double destinationLon) {
        this.destinationLon = destinationLon;
    }

    public Country getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(Country destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, Destination> find = new Finder<>(Destination.class);
}
