package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class Gender extends Model {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    private int genderId;

    private String genderName;

    public Gender(String genderName) {
        this.genderName = genderName;
    }

    public int getGenderId() {
        return genderId;
    }

    public void setGenderId(int genderId) {
        this.genderId = genderId;
    }

    public String getGenderName() {
        return genderName;
    }

    public void setGenderName(String genderName) {
        this.genderName = genderName;
    }

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, Gender> find = new Finder<>(Gender.class);
}

