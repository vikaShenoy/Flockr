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
    private String ISOCode;

    private boolean isValid;

    public Country(String countryName, String ISOCode, boolean isValid) {
        this.countryName = countryName;
        this.ISOCode = ISOCode;
        this.isValid = isValid;
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

    public String getISOCode() {
        return ISOCode;
    }

    public void setISOCode(String ISOCode) {
        this.ISOCode = ISOCode;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
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
        boolean sameCountryId = this.getCountryId() == countryToCompare.getCountryId();
        return sameCountryId;
    }

    @Override
    public int hashCode() {
        return this.countryId;
    }
}
