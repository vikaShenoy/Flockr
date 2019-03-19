package models;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

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

    public Role(String roleType) {
        this.roleType = roleType;
    }


}
