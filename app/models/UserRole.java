package models;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Linking class to show the association between users and roles.
 */
@Entity
public class UserRole extends Model {

    @Id
    private int userRoleId;
    private int roleId;
    private int userId;

    /**
     * Create a new user role.
     * @param roleId id of the role the user has.
     * @param userId id of the user who has the role.
     */
    public UserRole(int roleId, int userId) {
        this.roleId = roleId;
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
