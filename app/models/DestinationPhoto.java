package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.SoftDelete;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class DestinationPhoto extends Model {

    @Id
    public int destinationPhotoId;

    @JsonIgnore
    @ManyToOne
    private Destination destination;

    @ManyToOne
    private PersonalPhoto personalPhoto;

    @JsonIgnore
    @SoftDelete
    @Column(name = "deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deleted;

    @JsonIgnore
    private Timestamp deletedExpiry;

    /**
     * Creates a new photo for a destination
     * @param destination The destination to add the photo to
     * @param personalPhoto The personal photo to add to the destination
     */
    public DestinationPhoto(Destination destination, PersonalPhoto personalPhoto)  {
        this.destination = destination;
        this.personalPhoto = personalPhoto;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setDeletedExpiry(Timestamp deletedExpiry) {
        this.deletedExpiry = deletedExpiry;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Destination getDestination() {
        return destination;
    }

    public int getDestinationPhotoId() {
        return destinationPhotoId;
    }

    public void setPersonalPhoto(PersonalPhoto personalPhoto) {
        this.personalPhoto = personalPhoto;
    }

    public PersonalPhoto getPersonalPhoto() {
        return personalPhoto;
    }

    public static final Finder<Integer, DestinationPhoto> find = new Finder<>(DestinationPhoto.class);
}
