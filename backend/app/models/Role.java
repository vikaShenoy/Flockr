package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Roles that users inside the system can take to alter
 * their permissions. E.g. an admin can edit user trips.
 */
@Entity
public class Role extends Model {

    @Id
    private int roleId;

    private String roleType;

    public int getRoleId() {
        return roleId;
    }

    public String getRoleType() {
        return roleType;
    }

    /**
     * Constructor.
     * @param roleType type of role.
     */
    public Role(String roleType) {
        this.roleType = roleType;
    }


}
