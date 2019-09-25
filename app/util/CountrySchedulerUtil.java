package util;

import models.Country;
import models.Nationality;
import models.Passport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for helping with country scheduler task.
 */
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

        for (String ISOCode : newCountries.keySet()) {
            Country newCountry = newCountries.get(ISOCode);
            if (!currentPassports.containsKey(newCountry.getCountryName())) {
                Passport passport = new Passport(newCountry.getCountryName());
                passport.setCountry(newCountry);
                passportsToSave.add(passport);
            } else {
                for (Passport passport : currentPassports.values()) {
                    if (passport.getPassportCountry().equalsIgnoreCase(newCountry.getCountryName())) {
                        passport.setPassportCountry(newCountry.getCountryName());
                        passport.setCountry(newCountry);
                        passportsToSave.add(passport);
                    }
                }
            }
        }

        for (String ISOCode : currentPassports.keySet()) {
            if (!newCountries.containsKey(ISOCode)) {
                currentPassports.get(ISOCode).getCountry().setIsValid(false);
                passportsToSave.add(currentPassports.get(ISOCode));
            }
        }


        return passportsToSave;
    }

    /**
     * Gets the nationalities tro save.
     *
     * @param newCountries the new countries
     * @param currentNationalities the existing nationalities.
     * @return the list of nationalities to save.
     */
    public List<Nationality> getNationalitiesToSave(Map<String, Country> newCountries, Map<String, Nationality> currentNationalities) {
        List<Nationality> nationalitiesToSave = new ArrayList<>();

        for (String ISOCode : newCountries.keySet()) {
            Country newCountry = newCountries.get(ISOCode);
            if (!currentNationalities.containsKey(newCountry.getCountryName())) {
                Nationality nationality = new Nationality(newCountry.getCountryName());
                nationality.setNationalityCountry(newCountry);
                nationalitiesToSave.add(nationality);
            } else {
                for (Nationality nationality : currentNationalities.values()) {
                    if (nationality.getNationalityName().equalsIgnoreCase(newCountry.getCountryName())) {
                        nationality.setNationalityName(newCountry.getCountryName());
                        nationality.setNationalityCountry(newCountry);
                        nationalitiesToSave.add(nationality);
                    }
                }
            }
        }

        for (String ISOCode : currentNationalities.keySet()) {
            if (!newCountries.containsKey(ISOCode)) {
                currentNationalities.get(ISOCode).getNationalityCountry().setIsValid(false);
                nationalitiesToSave.add(currentNationalities.get(ISOCode));
            }
        }
        return nationalitiesToSave;
    }
}
