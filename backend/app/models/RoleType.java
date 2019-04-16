package models;

public enum RoleType {
    ADMIN, SUPER_ADMIN;

    public static boolean contains(String role) {
        for (RoleType roles : RoleType.values()) {
            if (roles.name().equals(role)) {
                return true;
            }
        }
        return false;
    }
}