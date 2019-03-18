package models;

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

    private String nationalityCountry;

    public Nationality(String nationalityCountry) {
        this.nationalityCountry = nationalityCountry;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public String getNationalityCountry() {
        return nationalityCountry;
    }

    public void setNationalityCountry(String nationalityCountry) {
        this.nationalityCountry = nationalityCountry;
    }
}

