package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * Roles that users inside the system can take to alter
 * their permissions. E.g. an admin can edit user trips.
 */
@Entity
public class Role extends Model {

    @ManyToMany
    private List<User> users;

    @Id
    private int roleId;

    private String roleType;

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleType='" + roleType + '\'' +
                '}';
    }

    public int getRoleId() {
        return roleId;
    }

    public Role(RoleType roleType) {
        this.roleType = roleType.name();
    }

    public String getRoleType() {
        return roleType;
    }

    /**
     * This is required by Ebean to make queries on the databse
     */
    public static final Finder<Integer, Role> find = new Finder<>(Role.class);

    /**
     * Constructor.
     * @param roleType type of role.
     */
    public Role(String roleType) {
        this.roleType = roleType;
    }
}
