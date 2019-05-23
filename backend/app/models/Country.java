package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;


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

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, Country> find = new Finder<>(Country.class);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Country)) {
            return false;
        }
        Country countryToCompare = (Country) obj;
        return this.countryName.equals(countryToCompare.getCountryName());
    }
}
