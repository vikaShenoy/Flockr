package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserNationality extends Model {

    @Id
    private int userNationalityId;


    private int nationalityId;
    private int userId;

    public UserNationality(int nationalityId, int userId) {
        this.nationalityId = nationalityId;
        this.userId = nationalityId;

    }

    public int getUserNationalityId() {
        return userNationalityId;
    }

    public void setUserNationalityId(int userNationalityId) {
        this.userNationalityId = userNationalityId;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
