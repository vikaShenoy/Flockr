package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class Nationality extends Model {
    @Id
    private int nationalityId;

    @ManyToMany
    private List<User> users;

    private String nationalityName;

    public Nationality(String nationalityCountry) {
        this.nationalityName = nationalityCountry;
    }

    @Override
    public String toString() {
        return "Nationality{" +
                "nationalityId=" + nationalityId +
                ", nationalityName='" + nationalityName + '\'' +
                '}';
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

