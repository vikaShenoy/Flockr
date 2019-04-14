package models;

public enum Roles {
    ADMIN("admin"), SUPER_ADMIN("super-admin");

    private String roleType;

    Roles(String roleType) {
        this.roleType = roleType;
    }

    public String getRoleType() {
        return roleType;
    }
}
