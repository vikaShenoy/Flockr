package util;

import models.Country;
import models.Passport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountrySchedulerUtil {

    /**
     * Merges old countries with new countries from the api
     *
     * @param currentCountries the current countries that are in the db
     * @param newCountries the new countries retrieved from the api
     * @return new countries that should be updated in the database
     */
    public List<Country> getCountriesToSave(Map<String, Country> currentCountries, Map<String, Country> newCountries) {
        List<Country> countriesToSave = new ArrayList<>();


        for (String ISOCode : newCountries.keySet()) {
            Country newCountry = newCountries.get(ISOCode);
            if (!currentCountries.containsKey(ISOCode)) {
                countriesToSave.add(newCountry);
            } else {
                Country currentCountry = currentCountries.get(ISOCode);
                if (!currentCountry.getIsValid() || !(currentCountry.getCountryName().equals(newCountry.getCountryName()))) {
                    currentCountry.setIsValid(true);
                    currentCountry.setCountryName(newCountry.getCountryName());
                    countriesToSave.add(currentCountry);
                }

            }
        }

        for (String ISOCode : currentCountries.keySet()) {
            if (!newCountries.containsKey(ISOCode)) {
                currentCountries.get(ISOCode).setIsValid(false);
                countriesToSave.add(currentCountries.get(ISOCode));
            }
        }

        return countriesToSave;
    }

    /**
     * Merges old passport countries with the new countries found from the API. If the passport with the country name
     * already exists, the country details are then updated to the new one. Otherwise, a new passport with the new
     * country name is created.
     *
     * @param newCountries the new countries found from the API
     * @param currentPassports the passports that are currently stored in the database
     * @return the passports that needs to be updated in the database
     */
    public List<Passport> getPassportsToSave(Map<String, Country> newCountries, Map<String, Passport> currentPassports) {
        List<Passport> passportsToSave = new ArrayList<>();
        List<Passport> passports = Passport.find.all();

        for (String ISOCode : newCountries.keySet()) {
            Country newCountry = newCountries.get(ISOCode);
            if (!currentPassports.containsKey(newCountry.getCountryName())) {
                Passport passport = new Passport(newCountry.getCountryName());
                passport.setCountry(newCountry);
                passportsToSave.add(passport);
            }

            for (Passport passport : passports) {
                if (passport.getPassportCountry().equalsIgnoreCase(newCountry.getCountryName())) {
                    passport.setCountry(newCountry);
                    passportsToSave.add(passport);
                }
            }
        }
        return passportsToSave;
    }
}
