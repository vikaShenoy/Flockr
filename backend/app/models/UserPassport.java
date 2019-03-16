package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class UserPassport extends Model {

    @Id
    private int userPassportId;
    private int passportId;
    private int userId;

    public UserPassport(int passportId, int userId) {
        this.passportId = passportId;
        this.userId = userId;
    }

    public long getPassportId() {
        return passportId;
    }

    public void setPassportId(int passportId) {
        this.passportId = passportId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

