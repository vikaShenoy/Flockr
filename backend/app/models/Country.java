package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * A country that is linked to a destination.
 */

@Entity
public class Country extends Model {


    @Id
    private int countryId;

    private String countryName;

    public Country(String countryName) {
        this.countryName = countryName;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public static final Finder<Integer, Country> find = new Finder<>(Country.class);
}
