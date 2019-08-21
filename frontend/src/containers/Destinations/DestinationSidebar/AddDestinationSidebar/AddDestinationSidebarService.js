import superagent from "superagent";
import {endpoint} from "../../../../utils/endpoint";

/**
 * Get a list of all the countries currently in the database, must be logged in to succeed
 * @returns {Promise<Array>} of all countries
 */
export async function requestCountries() {
  const res = await superagent.get(endpoint("/destinations/countries"))
  .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get a list of all the destination Types currently in the database, must be logged in to succeed
 * @returns {Promise<Array>} of all destination types
 */
export async function requestDestinationTypes() {
  const res = await superagent.get(endpoint("/destinations/types"))
  .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get a list of all the districts for the given country currently in the database, must be logged in to succeed
 * @param countryId int id of the country selected
 * @returns {Promise<Array>}  of all countries
 */
export async function requestDistricts(countryId) {
  const res = await superagent.get(
      endpoint(`/destinations/countries/${countryId}/districts`))
  .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get a list of valid user traveller types.
 */
export async function requestTravellerTypes() {
  const res = await superagent.get(endpoint("/users/types"))
  .set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

