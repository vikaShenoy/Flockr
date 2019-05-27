package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class DestinationPhoto extends Model {
    @Id
    public int destinationPhotoId;

    @ManyToOne
    private Destination destination;

    @ManyToOne
    private PersonalPhoto personalPhoto;

    public DestinationPhoto(Destination destination, PersonalPhoto personalPhoto)  {
        this.destination = destination;
        this.personalPhoto = personalPhoto;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestinationPhotoId(int destinationPhotoId) {
        this.destinationPhotoId = destinationPhotoId;
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
