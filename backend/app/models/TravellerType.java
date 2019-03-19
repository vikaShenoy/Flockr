package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.rmi.MarshalledObject;
import java.util.List;

@Entity
public class TravellerType extends Model {




    @ManyToMany
    private List<User> users;

    @Id
    private int travellerTypeId;

    private String travellerTypeName;

    /**
     * This is required by EBean to make queries on the database
     */
    public static final Finder<Integer, TravellerType> find = new Finder<>(TravellerType.class);
}
