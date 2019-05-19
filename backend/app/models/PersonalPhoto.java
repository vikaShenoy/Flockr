package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * A personal photo that is linked to a user
 */
@Entity
public class PersonalPhoto extends Model {

    @JsonIgnore
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

    /**
     * Constructor
     * @param filenameHash the hashed filename of the photo
     * @param isPublic true if the photo is public, otherwise photo is private
     * @param user the user that the photo is associated to
     * @param isPrimary true if the photo is the primary photo of the user, otherwise false
     */
    public PersonalPhoto(String filenameHash, boolean isPublic, User user, boolean isPrimary) {
        this.filenameHash = filenameHash;
        this.isPublic = isPublic;
        this.user = user;
        this.isPrimary = isPrimary;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public int getPhotoId() {
        return photoId;
    }

    public String getFilenameHash() {return this.filenameHash;}

    public User getUser() {
        return this.user;
    }

    public void setPublic(boolean isPublic) {
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

    public void setFilenameHash(String filenameHash) {
        this.filenameHash = filenameHash;
    }

    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhoto> find = new Finder<>(PersonalPhoto.class);
}
