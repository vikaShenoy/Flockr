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

    public PersonalPhoto getPersonalPhoto() {
        return personalPhoto;
    }

    public static final Finder<Integer, DestinationPhoto> find = new Finder<>(DestinationPhoto.class);

}
