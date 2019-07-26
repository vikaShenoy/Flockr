import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import {async} from "q";


/**
 * Get a list of all public and private destinations that a user has
 * @param {number} userId ID of the user to get destinations from
 * @returns {Promise<Array>} the users public and private destinations
 */
export async function getYourDestinations() {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/destinations`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get a list of all public destinations
 * @returns {Promise<Array>} the list of public destinations
 */
export async function getPublicDestinations() {
  const res = await superagent.get(endpoint("/destinations"))
    .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}




/**
 * Sends a request to get a specific destination.
 *
 * @param destinationId {Number} the id of the destination.
 * @return {Promise<JSON>} the destination object.
 */
export async function requestDestination(destinationId) {
  const res = await superagent.get(endpoint(`/destinations/${destinationId}`))
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

export async function sendUndoDeleteDestination(destinationId) {
  const res = await superagent.put(endpoint(`/destinations/${destinationId}/undodelete`))
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
export async function sendAddDestination(destination) {
  const destinationInfo = {
    destinationName: destination.destinationName,
    destinationTypeId: destination.destinationType.destinationTypeId,
    countryId: destination.destinationCountry.countryId,
    districtId: destination.destinationDistrict.districtId,
    latitude: destination.destinationLat,
    longitude: destination.destinationLon,
    travellerTypeIds: destination.travellerTypes.map(travellerType => travellerType.travellerTypeId),
  };

  const userId = localStorage.getItem("userId");
  const res = await superagent.post(endpoint(`/users/${userId}/destinations`))
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
export async function sendUpdateDestination(destination, destinationId) {
  const destinationInfo = {
    destinationName: destination.destinationName,
    destinationTypeId: destination.destinationType.destinationTypeId,
    countryId: destination.destinationCountry.countryId,
    districtId: destination.destinationDistrict.districtId,
    latitude: destination.destinationLat,
    longitude: destination.destinationLon,
    travellerTypeIds: destination.travellerTypes.map(travellerType => travellerType.travellerTypeId),
    isPublic: destination.isPublic
  };

  const res = await superagent.put(endpoint(`/destinations/${destinationId}`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send(destinationInfo);

  return res.body;
}