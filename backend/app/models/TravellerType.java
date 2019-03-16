package models;

/**
 * An enum to represent possible traveller types
 */
public enum TravellerType {
    GROUPIE (1), THRILLSEEKER (2), GAP_YEAR (3), FREQUENT_WEEKENDER (4), HOLIDAYMAKER (5), FUNCTIONAL_BUSINESS (6), BACKPACKER (7);

    private final int typeCode;

    TravellerType(int typeCode) {
        this.typeCode = typeCode;
    }

    public int getTravellerType() {
        return this.typeCode;
    }
}
