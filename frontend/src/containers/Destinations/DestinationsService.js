import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Get a list of all public and private destinations that a user has
 *
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
 *
 * @param {String} searchCriterion optional, to filter destinations by name
 * @param {Number} offset the offset for the returned public destinations
 * @returns {Promise<Array>} the list of public destinations, sorted by name
 */
export async function getPublicDestinations(searchCriterion, offset) {
  const query = {
    offset: offset
  };
  if (searchCriterion) {
    query.search = searchCriterion;
  }
  const res = await superagent.get(endpoint("/destinations"))
    .set("Authorization", localStorage.getItem("authToken"))
    .query(query);

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

/**
 * Undo deletion for a destination (soft-delete). Used in undo/redo.
 * @param destinationId id of the destination to undo delete for.
 * @returns {Promise<*>}
 */
export async function sendUndoDeleteDestination(destinationId) {
  const res = await superagent.put(endpoint(`/destinations/${destinationId}/undodelete`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Add a destination to the database
 * @param destination JSON object containing the destination
 * @param destination.destinationName String name of the destination
 * @param destination.destinationTypeId int id of the destination type
 * @param destination.countryId int id of the country
 * @param destination.districtId int id of the district
 * @param destination.latitude float latitude of the destination
 * @param destination.longitude float longitude of the destination
 * @returns {Promise<>} contains nothing
 */
export async function sendAddDestination(destination) {
  const destinationInfo = {
    destinationName: destination.destinationName,
    destinationTypeId: destination.destinationType.destinationTypeId,
    countryId: destination.destinationCountry.countryId,
    districtName: destination.destinationDistrict.districtName,
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
 * @param destination JSON object containing the destination
 * @param destination.destinationName String name of the destination
 * @param destination.destinationTypeId int id of the destination type
 * @param destination.countryId int id of the country
 * @param destination.districtId int id of the district
 * @param destination.latitude float latitude of the destination
 * @param destination.longitude float longitude of the destination
 * @param destinationId int id of the destination to update
 * @returns {Promise<>} contains nothing
 */
export async function sendUpdateDestination(destination, destinationId) {
  const destinationInfo = {
    destinationName: destination.destinationName,
    destinationTypeId: destination.destinationType.destinationTypeId,
    countryId: destination.destinationCountry.countryId,
    districtName: destination.destinationDistrict,
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

