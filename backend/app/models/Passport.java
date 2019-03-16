package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Passport extends Model {

    @Id
    private int passportId;

    private String passportCountry;

    public Passport(String passportCountry) {
        this.passportCountry = passportCountry;
    }

}
