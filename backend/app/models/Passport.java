package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Passport extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Id
    private int passportId;

    private String passportCountry;

    public Passport(String passportCountry) {
        this.passportCountry = passportCountry;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, Passport> find = new Finder<>(Passport.class);
}
