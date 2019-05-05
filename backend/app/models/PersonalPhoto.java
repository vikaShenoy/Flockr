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
    private String filenameHash;


    /**
     * Create a new photo with the given hashed filename
     *
     * @param filenameHash the Hashed file name
     */
    public PersonalPhoto(String filenameHash, boolean isPublic) {
        this.filenameHash = filenameHash;
        this.isPublic = isPublic;
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


    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhoto> find = new Finder<>(PersonalPhoto.class);
}
