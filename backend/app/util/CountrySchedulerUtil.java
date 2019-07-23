package util;

import models.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountrySchedulerUtil {
       /**
     * Merges old countries with new countries from the api
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
}
