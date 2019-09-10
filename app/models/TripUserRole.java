package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


@Entity
public class TripUserRole extends Model {

    @Id
    private int tripUserRoleId;

    @ManyToOne
    private User user;

    @ManyToOne
    private TripNode trip;

    @ManyToOne
    private Role role;

    public TripUserRole(User user, TripNode trip, Role role) {
        this.user = user;
        this.trip = trip;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TripNode getTrip() {
        return trip;
    }

    public void setTrip(TripNode trip) {
        this.trip = trip;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
