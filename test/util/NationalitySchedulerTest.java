package util;

import models.Country;
import models.Nationality;
import models.Passport;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NationalitySchedulerTest {
    private final CountrySchedulerUtil countrySchedulerUtil = new CountrySchedulerUtil();

    @Test
    public void nationalitiesCanBeAdded() {
        Map<String, Nationality> currentNationalities = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        newCountries.put("NZL", nz);

        Nationality nationalityNZ = new Nationality(nz.getCountryName());
        nationalityNZ.setNationalityCountry(nz);

        List<Nationality> nationalityList = countrySchedulerUtil.getNationalitiesToSave(newCountries, currentNationalities);
        Assert.assertTrue(nationalityList.contains(nationalityNZ));
    }

    @Test
    public void nationalitiesCanBeInvalidated() {
        Map<String, Nationality> currentNationalities = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country aus = new Country("Australia", "AUS", true);
        newCountries.put("NZL", nz);
        Nationality nationalityNZ = new Nationality(nz.getCountryName());
        nationalityNZ.setNationalityCountry(nz);
        currentNationalities.put("NZL", nationalityNZ);

        // AU Nationality is in the current nationalities but not in the newCountries so it should be invalidated
        // and getisValid() should return false
        Nationality nationalityAU = new Nationality(aus.getCountryName());
        nationalityAU.setNationalityCountry(aus);
        currentNationalities.put("AUS", nationalityAU);

        List<Nationality> nationalityList = countrySchedulerUtil.getNationalitiesToSave(newCountries, currentNationalities);
        Assert.assertEquals(2, nationalityList.size());
        for (Nationality nationality : nationalityList) {
            if (nationality.getNationalityCountry().getISOCode().equalsIgnoreCase("AUS")) {
                Assert.assertTrue(!nationality.getNationalityCountry().getIsValid());
            } else if (nationality.getNationalityCountry().getISOCode().equalsIgnoreCase("NZL")) {
                Assert.assertTrue(nationality.getNationalityCountry().getIsValid());
            }
        }
    }

    @Test
    public void nationalityNamesCanBeUpdated() {
        Map<String, Nationality> currentNationalities = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country nzl = new Country("New Zealandia", "NZL", true);
        newCountries.put("NZL", nzl);
        Nationality nationalityNZ = new Nationality(nz.getCountryName());
        nationalityNZ.setNationalityCountry(nz);
        currentNationalities.put("NZL", nationalityNZ);

        List<Nationality> nationalityList = countrySchedulerUtil.getNationalitiesToSave(newCountries, currentNationalities);
        Assert.assertEquals(1, nationalityList.size());
        Assert.assertEquals(nationalityList.get(0).getNationalityCountry().getCountryName(), nzl.getCountryName());
        Assert.assertEquals(nationalityList.get(0).getNationalityCountry().getISOCode(), nzl.getISOCode());
        Assert.assertEquals(nationalityList.get(0).getNationalityCountry().getIsValid(), nzl.getIsValid());
        Assert.assertEquals(nationalityList.get(0).getNationalityName(), nzl.getCountryName());
    }

    @Test
    public void nationalitiesCanBeRevalidated() {
        Map<String, Nationality> currentNationalities = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country nzl = new Country("New Zealand", "NZL", false);
        newCountries.put("NZL", nz);
        Nationality nationalityNZ = new Nationality(nzl.getCountryName());
        nationalityNZ.setNationalityCountry(nzl);
        currentNationalities.put("NZL", nationalityNZ);

        List<Nationality> nationalityList = countrySchedulerUtil.getNationalitiesToSave(newCountries, currentNationalities);
        Assert.assertEquals(1, nationalityList.size());
        Assert.assertEquals(nationalityList.get(0).getNationalityCountry().getCountryName(), nz.getCountryName());
        Assert.assertEquals(nationalityList.get(0).getNationalityCountry().getISOCode(), nz.getISOCode());
        Assert.assertTrue(nationalityList.get(0).getNationalityCountry().getIsValid());

    }

}
