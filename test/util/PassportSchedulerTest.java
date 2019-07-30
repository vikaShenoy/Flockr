package util;

import models.Country;
import models.Passport;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassportSchedulerTest {
    private final CountrySchedulerUtil countrySchedulerUtil = new CountrySchedulerUtil();

    @Test
    public void passportsCanBeAdded() {
        Map<String, Passport> currentPassports = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        newCountries.put("NZL", nz);

        // Create a passport which should be added in the list of passportList after the call to the
        // function getPassportsToSave
        Passport passportNZ = new Passport(nz.getCountryName());
        passportNZ.setCountry(nz);

        List<Passport> passportList = countrySchedulerUtil.getPassportsToSave(newCountries, currentPassports);
        Assert.assertTrue(passportList.contains(passportNZ));
    }

    @Test
    public void passportsCanBeInvalidated() {
        Map<String, Passport> currentPassports = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country aus = new Country("Australia", "AUS", true);
        newCountries.put("NZL", nz);
        Passport passportNZ = new Passport(nz.getCountryName());
        passportNZ.setCountry(nz);
        currentPassports.put("NZL", passportNZ);

        // AU Passport is in the current passport but not in the newCountries so it should be invalidated
        // and getisValid() should return false
        Passport passportAU = new Passport(aus.getCountryName());
        passportAU.setCountry(aus);
        currentPassports.put("AUS", passportAU);

        List<Passport> passportList = countrySchedulerUtil.getPassportsToSave(newCountries, currentPassports);
        Assert.assertEquals(2, passportList.size());
        for (Passport passport : passportList) {
            if (passport.getCountry().getISOCode().equalsIgnoreCase("AUS")) {
                Assert.assertTrue(!passport.getCountry().getIsValid());
            } else if (passport.getCountry().getISOCode().equalsIgnoreCase("NZL")) {
                Assert.assertTrue(passport.getCountry().getIsValid());
            }
        }
    }

    @Test
    public void passportNamesCanBeUpdated() {
        Map<String, Passport> currentPassports = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country nzl = new Country("New Zealandia", "NZL", true);
        newCountries.put("NZL", nzl);
        Passport passportNZ = new Passport(nz.getCountryName());
        passportNZ.setCountry(nz);
        currentPassports.put("NZL", passportNZ);

        List<Passport> passportList = countrySchedulerUtil.getPassportsToSave(newCountries, currentPassports);
        Assert.assertEquals(1, passportList.size());
        Assert.assertEquals(passportList.get(0).getCountry().getCountryName(), nzl.getCountryName());
        Assert.assertEquals(passportList.get(0).getCountry().getISOCode(), nzl.getISOCode());
        Assert.assertEquals(passportList.get(0).getCountry().getIsValid(), nz.getIsValid());
        Assert.assertEquals(passportList.get(0).getPassportCountry(), nzl.getCountryName());
    }

    @Test
    public void passportsCanBeRevalidated() {
        Map<String, Passport> currentPassports = new HashMap<>();
        Map<String, Country> newCountries = new HashMap<>();
        Country nz = new Country("New Zealand", "NZL", true);
        Country nzl = new Country("New Zealand", "NZL", false);
        newCountries.put("NZL", nz);
        Passport passportNZ = new Passport(nzl.getCountryName());
        passportNZ.setCountry(nzl);
        currentPassports.put("NZL", passportNZ);

        List<Passport> passportList = countrySchedulerUtil.getPassportsToSave(newCountries, currentPassports);
        Assert.assertEquals(1, passportList.size());
        Assert.assertEquals(passportList.get(0).getCountry().getCountryName(), nz.getCountryName());
        Assert.assertEquals(passportList.get(0).getCountry().getISOCode(), nz.getISOCode());
        Assert.assertTrue(passportList.get(0).getCountry().getIsValid());
    }

}
