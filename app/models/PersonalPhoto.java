package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;
import play.data.validation.Constraints;
import play.libs.Json;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

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

    @JsonProperty("isPublic")
    @Constraints.Required
    private boolean isPublic;

    @JsonProperty("isPrimary")
    @Constraints.Required
    private boolean isPrimary;

    @Constraints.Required
    private String filenameHash;

    @Constraints.Required
    private String thumbnailName;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DestinationPhoto> destinationPhotos;

    @SoftDelete
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @JsonIgnore
    private Timestamp deletedExpiry;

    /**
     * Constructor
     * @param filenameHash the hashed filename of the photo
     * @param isPublic true if the photo is public, otherwise photo is private
     * @param user the user that the photo is associated to
     * @param isPrimary true if the photo is the primary photo of the user, otherwise false
     * @param thumbnailName the filename of the thumbnail
     */
    public PersonalPhoto(String filenameHash, boolean isPublic, User user, boolean isPrimary, String thumbnailName) {
        this.filenameHash = filenameHash;
        this.isPublic = isPublic;
        this.user = user;
        this.isPrimary = isPrimary;
        this.thumbnailName = thumbnailName;
    }

    public Timestamp getDeletedExpiry() {
        return deletedExpiry;
    }

    public void setDeletedExpiry(Timestamp deletedExpiry) {
        this.deletedExpiry = deletedExpiry;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public int getPhotoId() {
        return photoId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getFilenameHash() {return this.filenameHash;}

    public User getUser() {
        return this.user;
    }

    public int getOwnerId() {
        return user.getUserId();
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

    public String getThumbnailName() {
        return thumbnailName;
    }

    public void setThumbnailName(String thumbnailName) {
        this.thumbnailName = thumbnailName;
    }

    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhoto> find = new Finder<>(PersonalPhoto.class);
}
