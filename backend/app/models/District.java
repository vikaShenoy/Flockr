package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * A district within a country that is linked to destinations.
 */
@Entity
public class District extends Model {


    @Id
    private int districtId;

    private String districtName;

    @ManyToOne
    private Country country;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof District)) {
            return false;
        }
        District districtToCompare = (District) obj;
        boolean sameName = this.districtName.equals(districtToCompare.getDistrictName());
        boolean sameCountry = this.country.equals(districtToCompare.getCountry());
        return (sameName && sameCountry);
    }

    /**
     * Constructor.
     * @param districtName name of the district.
     * @param country country the district is in.
     */
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
