package models;

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

    private Roles roleType;

    public int getRoleId() {
        return roleId;
    }

    public Roles getRoleType() {
        return roleType;
    }

    public Role(Roles roleType) {
        this.roleType = roleType;
    }
}

