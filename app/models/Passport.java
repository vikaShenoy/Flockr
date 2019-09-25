package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Model for passports the user can add to their profile.
 */
@Entity
public class Passport extends Model {

    @ManyToMany
    private List<User> users;

    @Id
    private int passportId;

    private String passportCountry;

    @OneToOne(cascade = CascadeType.ALL)
    private Country country;

    /**
     * Constructor.
     * @param passportCountry country the passport is for.
     */
    public Passport(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    public void setPassportCountry(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    public int getPassportId() {
        return passportId;
    }

    public void setPassportId(int passportId) {
        this.passportId = passportId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getPassportCountry() {
        return passportCountry;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, Passport> find = new Finder<>(Passport.class);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Passport)) {
            return false;
        }
        Passport passportToCompare = (Passport) obj;
        return this.getPassportId() == passportToCompare.getPassportId();
    }

    @Override
    public int hashCode() {
        return this.passportId;
    }
}
