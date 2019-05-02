package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class PersonalPhotos extends Model {

    @ManyToMany
    private List<User> users;

    @Id
    private int photoId;

    private String fileNameHash;

    /**
     * Create a new photo with the given hashed filename
     * @param fileNameHash the Hashed file name
     */
    public PersonalPhotos(String fileNameHash) { this.fileNameHash = fileNameHash; }

    public int getPhotoId() { return photoId; }

    public String getFileName() { return fileNameHash; }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    /**
     * This function allows EBean to make queries on the database
     */
    public static final Finder<Integer, PersonalPhotos> find = new Finder<>(PersonalPhotos.class);
}
