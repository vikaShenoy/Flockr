package models;

public enum RoleType {
    ADMIN, SUPER_ADMIN, TRAVELLER, TRIP_OWNER, TRIP_MANAGER, TRIP_MEMBER;

    public static boolean contains(String role) {
        for (RoleType roles : RoleType.values()) {
            if (roles.name().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.name();
    }
}