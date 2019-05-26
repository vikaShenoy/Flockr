package models;

import javax.persistence.*;

@Entity
public class DestinationPhoto {
    @Id
    public int destinationPhotoId;

    @ManyToOne
    private Destination destination;

    @ManyToOne
    private PersonalPhoto personalPhoto;
}
