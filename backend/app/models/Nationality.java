package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Nationality a user can have.
 */
@Entity
public class Nationality extends Model {

    @Id
    private int nationalityId;

    @ManyToMany
    private List<User> users;

    @OneToOne(cascade = CascadeType.ALL)
    private Country nationalityCountry;

    private String nationalityName;

    /**
     * Constructor.
     * @param nationalityName country the nationaltiy is from.
     */
    public Nationality(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    @Override
    public String toString() {
        return "Nationality{" +
                "nationalityId=" + nationalityId +
                ", nationalityName='" + nationalityName + '\'' +
                '}';
    }

    public Country getNationalityCountry() {
        return nationalityCountry;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    public void setNationalityCountry(Country nationalityCountry) {
        this.nationalityCountry = nationalityCountry;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, Nationality> find = new Finder<>(Nationality.class);
}

