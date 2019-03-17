package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Passport extends Model {

    @ManyToMany
    private List<User> users;

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
