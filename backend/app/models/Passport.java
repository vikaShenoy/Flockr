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

    private Country country;

    /**
     * Constructor.
     * @param passportCountry country the passport is for.
     */
    public Passport(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    public int getPassportId() {
        return passportId;
    }

    public void setPassportId(int passportId) {
        this.passportId = passportId;
    }

    public String getPassportCountry() {
        return passportCountry;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return country;
    }

    /**
     * This is required by EBean to make queries on the database.
     */
    public static final Finder<Integer, Passport> find = new Finder<>(Passport.class);
}
