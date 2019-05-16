package models;

import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
public class PersonalPhoto extends Model {

    @ManyToOne
    private User user;

    @Id
    private int photoId;

    @Constraints.Required
    private boolean isPublic;

    @Constraints.Required
    private boolean isPrimary;

    @Constraints.Required
    private String filenameHash;

    public PersonalPhoto(String filenameHash, boolean isPublic, User user, boolean isPrimary) {
        this.filenameHash = filenameHash;
        this.isPublic = isPublic;
        this.user = user;
    }

    public int getPhotoId() {
        return photoId;
    }

    public User getUser() {
        return this.user;
    }

    public void setPermission(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhoto> find = new Finder<>(PersonalPhoto.class);
}
