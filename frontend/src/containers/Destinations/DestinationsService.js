import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";


/**
 * Get a list of all the destinations currently in the database, must be logged in to succeed
 * @returns {Promise<Array>} of all destinations
 */
export async function requestDestinations() {
  const res = await superagent.get(endpoint("/destinations"))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

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
  const res = await superagent.get(endpoint(`/destinations/countries/${countryId}/districts`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Delete a destination from the database
 * @param destinationId int id of the destination
 * @returns {Promise<>} contains nothing
 */
export async function sendDeleteDestination(destinationId) {
  const res = await superagent.delete(endpoint(`/destinations/${destinationId}`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Add a destination to the database
 * @param destinationInfo JSON object containing the destination
 * @param destinationInfo.destinationName String name of the destination
 * @param destinationInfo.destinationTypeId int id of the destination type
 * @param destinationInfo.countryId int id of the country
 * @param destinationInfo.districtId int id of the district
 * @param destinationInfo.latitude float latitude of the destination
 * @param destinationInfo.longitude float longitude of the destination
 * @returns {Promise<>} contains nothing
 */
export async function sendAddDestination(destinationInfo) {
  const res = await superagent.post(endpoint("/destinations"))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(destinationInfo);

  return res.body;
}

/**
 * Update an existing destination in the database
 * @param destinationInfo JSON object containing the destination
 * @param destinationInfo.destinationName String name of the destination
 * @param destinationInfo.destinationTypeId int id of the destination type
 * @param destinationInfo.countryId int id of the country
 * @param destinationInfo.districtId int id of the district
 * @param destinationInfo.latitude float latitude of the destination
 * @param destinationInfo.longitude float longitude of the destination
 * @param destinationId int id of the destination to update
 * @returns {Promise<>} contains nothing
 */
export async function sendUpdateDestination(destinationInfo, destinationId) {
  const res = await superagent.put(endpoint(`/destinations/${destinationId}`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(destinationInfo);

  return res.body;
}