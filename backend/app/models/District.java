package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * A district within a country that is linked destination.
 */
@Entity
public class District extends Model {


    @Id
    private int districtId;

    private String districtName;

    @ManyToOne
    private Country country;

    public District (String districtName, Country country) {
        this.districtName = districtName;
        this.country = country;
    }

    public String getDistrictName() {
        return districtName;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public static final Finder<Integer, District> find = new Finder<>(District.class);

}
