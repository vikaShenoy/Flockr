package models.photos;

import io.ebean.Model;
import javax.persistence.Id;

/**
 * Represents a photo in the TravelEA system.
 */
public class Photo extends Model {

    /**
     * The id of the photo in the database.
     */
    @Id
    private int photoId;

    /**
     * Whether the photo should be visible by anyone.
     */
    private boolean isPublic;

    /**
     * Whether this is the user's primary photo; used for profile picture, thumbnails, etc
     */
    private boolean isPrimary;

    /**
     * The filename of the photo e.g. "162465431.jpg"
     */
    private String filename;

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
