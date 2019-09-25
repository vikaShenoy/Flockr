package util;

import models.Country;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test that the class used when updating countries works as expected.
 */
public class CountrySchedulerTest {
    private final CountrySchedulerUtil countrySchedulerUtil = new CountrySchedulerUtil();

    @Test
    public void countriesCanBeAdded() {
        Map<String, Country> currentCountries = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country country1 = new Country("New Zealand", "NZL", true);
        Country country2 = new Country("Australia", "AUS", true);
        newCountries.put("NZL", country1);
        newCountries.put("AUS", country2);

        List<Country> countries = countrySchedulerUtil.getCountriesToSave(currentCountries, newCountries);
        Assert.assertTrue(countries.contains(country1));
        Assert.assertTrue(countries.contains(country2));
    }

    @Test()
    public void countriesCanBeInvalidated() {
        Map<String, Country> currentCountries = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country country1 = new Country("New Zealand", "NZL", true);
        Country country2 = new Country("Australia", "AUS", true);
        newCountries.put("NZL", country1);
        currentCountries.put("NZL", country1);
        // Australia should be invalidated
        currentCountries.put("AUS", country2);


        List<Country> countries = countrySchedulerUtil.getCountriesToSave(currentCountries, newCountries);
        Assert.assertEquals(1, countries.size());
        Assert.assertEquals(countries.get(0).getCountryName(), "Australia");
        Assert.assertEquals(countries.get(0).getISOCode(), "AUS");
        Assert.assertFalse(countries.get(0).getIsValid());
    }

    @Test()
    public void countryNamesCanBeUpdated() {
        Map<String, Country> currentCountries = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country country1 = new Country("New Zealand", "NZL", true);
        Country country2 = new Country("New Zealandia", "NZL", true);
        newCountries.put("NZL", country1);
        currentCountries.put("NZL", country2);


        List<Country> countries = countrySchedulerUtil.getCountriesToSave(currentCountries, newCountries);
        Assert.assertEquals(1, countries.size());
        Assert.assertEquals(countries.get(0).getCountryName(), "New Zealand");
        Assert.assertEquals(countries.get(0).getISOCode(), "NZL");
        Assert.assertTrue(countries.get(0).getIsValid());
    }

    @Test
    public void countriesCanBeRevalidated() {
        Map<String, Country> currentCountries = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country country1 = new Country("New Zealand", "NZL", true);
        Country country2 = new Country("New Zealand", "NZL", false);
        newCountries.put("NZL", country1);
        currentCountries.put("NZL", country2);


        List<Country> countries = countrySchedulerUtil.getCountriesToSave(currentCountries, newCountries);
        Assert.assertEquals(1, countries.size());
        Assert.assertEquals(countries.get(0).getCountryName(), "New Zealand");
        Assert.assertEquals(countries.get(0).getISOCode(), "NZL");
        Assert.assertTrue(countries.get(0).getIsValid());
    }
}
