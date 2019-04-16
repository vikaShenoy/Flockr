package models;

public enum RoleType {
    ADMIN("admin"), SUPER_ADMIN("super-admin"), TRAVELLER("traveller");

    private String roleName;

    RoleType(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
