package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class Role extends Model {

    @ManyToMany
    private List<User> users;

    @Id
    private int roleId;

    private RoleType roleType;

    public int getRoleId() {
        return roleId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Role(RoleType roleType) {
        this.roleType = roleType;
    }

    /**
     * This is required by Ebean to make queries on the databse
     */
    public static final Finder<Integer, Role> find = new Finder<>(Role.class);
}

