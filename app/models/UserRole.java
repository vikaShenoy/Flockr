package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Linking class to show the association between users and roles.
 */
@Entity
public class UserRole extends Model {

    @Id
    private int userRoleId;

    @ManyToOne
    private User user;

    @ManyToOne
    private Role role;

    /**
     * Create a new user role.
     * @param role id of the role the user has.
     * @param user id of the user who has the role.
     */
    public UserRole(User user, Role role) {
        this.role = role;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
