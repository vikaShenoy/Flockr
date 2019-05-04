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
    private String filenameHash;

    @Constraints.Required
    private boolean isPrimary;

    /**
     * Create a new photo with the given hashed filename
     *
     * @param filenameHash the Hashed file name
     */
    public PersonalPhoto(String filenameHash, boolean isPrimary) {
        this.filenameHash = filenameHash;
        this.isPrimary = isPrimary;
    }

    public int getPhotoId() {
        return photoId;
    }

    public String getFilenameHash() {
        return filenameHash;
    }

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public User getUser() {
        return this.user;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhoto> find = new Finder<>(PersonalPhoto.class);
}
