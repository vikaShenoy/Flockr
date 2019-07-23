package models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DestinationTest {

    DestinationType destinationType;
    District destinationDistrict;
    Double destinationLat;
    Double destinationLon;
    Country destinationCountry;
    Integer destinationOwner;
    boolean isPublic;
    TravellerType outdoorExplorer;
    TravellerType cityExplorer;
    List<TravellerType> travellerTypes;


    @Before
    public void setUp() throws Exception {
        destinationCountry = new Country("New Zealand", "NZL", true);
        destinationCountry.setCountryId(1);
        destinationType = new DestinationType("Backpacker");
        destinationType.setDestinationTypeId(2);
        destinationDistrict = new District("Ilam", destinationCountry);
        destinationDistrict.setDistrictId(1);
        destinationLat = 3.0;
        destinationLon = 45.0;
        travellerTypes = new ArrayList<>();
        outdoorExplorer = new TravellerType("Outdoor Explorer");
        outdoorExplorer.setTravellerTypeId(1);
        cityExplorer = new TravellerType("City Explorer");
        cityExplorer.setTravellerTypeId(2);

        travellerTypes.add(outdoorExplorer);
        travellerTypes.add(cityExplorer);
    }

    @Test
    public void twoDestinationsArePerfectlyEqual() {
        Destination destination1 = new Destination("England", destinationType,
                destinationDistrict, destinationLat, destinationLat, destinationCountry, destinationOwner, travellerTypes , isPublic);
        Destination destination2 = new Destination("England", destinationType,
                destinationDistrict, destinationLat, destinationLat, destinationCountry, destinationOwner, travellerTypes, isPublic);

        Assert.assertEquals(destination1, destination2);
    }

    /**
     * Check the equality fails when the destinations being compared have different types.
     */
    @Test
    public void twoDestinationsAreUnequal() {
        DestinationType type = new DestinationType("Attraction");
        Destination destination1 = new Destination("Colosseum", destinationType,
                destinationDistrict, destinationLat, destinationLon, destinationCountry, destinationOwner, travellerTypes, isPublic);
        Destination destination2 = new Destination("Colosseum", type,
                destinationDistrict, destinationLat, destinationLon, destinationCountry, destinationOwner, travellerTypes, isPublic);

        Assert.assertNotEquals(destination1, destination2);
    }

    /**
     * Destinations are considered "equal" if they have the same name, type and district.
     * This test ensures they can have differences in other properties (lat, lon).
     */
    @Test
    public void twoDestinationsMeetEquality() {
        Double lat1 = 10.0;
        Double lon1 = 10.0;
        Double lat2 = 20.0;
        Double lon2 = 20.0;
        Destination destination1 = new Destination("Big Ben", destinationType, destinationDistrict,
                lat1, lon1, destinationCountry, destinationOwner, travellerTypes, isPublic);
        Destination destination2 = new Destination("Big Ben", destinationType, destinationDistrict,
                lat2, lon2, destinationCountry, destinationOwner, travellerTypes, isPublic);

        Assert.assertEquals(destination1, destination2);
    }

    /**
     * There is the possibility of destinations being created with a country
     * different to the destination's district country.
     * This test ensures that the equality is false in that scenario.
     */
    @Test
    public void twoDestinationsHaveDifferentCountries() {
        Country country1 = new Country("India", "IND", true);
        Country country2 = new Country("China", "CHN", true);
        country2.setCountryId(5);
        Destination destination1 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, country1, destinationOwner, travellerTypes, isPublic);
        Destination destination2 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, country2, destinationOwner, travellerTypes, isPublic);

        Assert.assertNotEquals(destination1, destination2);
    }

    /**
     * Checks when two destinations have different travellerTypes, the will not be equal
     */
    public void twoDestinationsHaveDifferentTravellerTypes() {
        List<TravellerType> travellerTypes1 = new ArrayList<>();
        travellerTypes1.add(outdoorExplorer);
        List<TravellerType> travellerTypes2 = new ArrayList<>();
        travellerTypes2.add(cityExplorer);


        Destination destination1 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, destinationCountry, destinationOwner, travellerTypes1, isPublic);
        Destination destination2 = new Destination("Atlantis", destinationType, destinationDistrict,
                destinationLat, destinationLon, destinationCountry, destinationOwner, travellerTypes2, isPublic);

        Assert.assertNotEquals(destination1, destination2);
    }
}