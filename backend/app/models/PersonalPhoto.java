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
     * @param user the user that the photo is associated to
     * @param isPublic true if the photo is public, otherwise photo is private
     * @param isPrimary true if the photo is the primary photo of the user, otherwise false
     */
    public PersonalPhoto(String filenameHash, User user, boolean isPublic, boolean isPrimary) {
        this.filenameHash = filenameHash;
        this.isPublic = isPublic;
        this.user = user;
        this.isPrimary = isPrimary;
    }

    public boolean getIsPublic() {
        return this.isPublic;
    }
    public int getPhotoId() {
        return photoId;
    }

    public String getFileNameHash() {return this.filenameHash;}

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
