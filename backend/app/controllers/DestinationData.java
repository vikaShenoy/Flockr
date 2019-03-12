package controllers;

import models.destinations.DestinationType;
import play.data.validation.Constraints;

/**
 * A form processing DTO that maps to the new destination form.
 *
 * Using a class specifically for form binding reduces the chances
 * of a parameter tampering attack and makes code clearer, because
 * you can define constraints against the class.
 */
public class DestinationData {

    @Constraints.Required
    private String name;

    @Constraints.Required
    private DestinationType destinationType;

    @Constraints.Required
    private String district;

    @Constraints.Min(-90)
    @Constraints.Max(90)
    @Constraints.Required
    private Double latitude;

    @Constraints.Required
    @Constraints.Min(-180)
    @Constraints.Max(180)
    private Double longitude;

    @Constraints.Required
    private String country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
