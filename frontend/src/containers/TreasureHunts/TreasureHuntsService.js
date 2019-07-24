import superagent from "superagent";
import {endpoint} from "../../utils/endpoint.js"

/**
 * Get a list of all public destinations
 * @returns the list of public destinations
 */
export async function getPublicDestinations() {
    const res = await superagent.get(endpoint("/destinations"))
        .set("Authorization", localStorage.getItem("authToken"));

    return res.body;
}

/**
 * Sends a request the backend to get all of the publicly available, valid treasure hunts
 * @returns {Promise<*>} the list of treasure hunts
 */
export async function getAllTreasureHunts() {
    let token = localStorage.getItem("authToken");

    const res = await superagent.get(endpoint("/treasurehunts"))
        .set("Authorization", token)

    return res.body;

}

/**
 * Send a request the the backend to create a treasure hunt
 * @param treasureHunt - the treasure hunt to be created
 * @returns {Promise<*>} the confirmation of creation or not
 */
export async function createTreasureHunt(treasureHunt) {
    let token = localStorage.getItem("authToken");
    let userId = localStorage.getItem("userId");

    const res = await superagent.post(endpoint(`/users/${userId}/treasurehunts`))
        .set("Authorization", token)
        .send(treasureHunt);

    return res.body;
}

/**
 * Sends a request to the backend to edit an existing treasure hunt
 * @param treasureHunt the treasure hunt to be updated
 * @returns {Promise<*>} the confirmation of the treasure hunt being updated or not
 */
export async function editTreasureHunt(treasureHunt) {

    let token = localStorage.getItem("authToken");
    let treasureHuntId = treasureHunt.treasureHuntId;

    const res = await superagent.put(endpoint(`/treasurehunts/${treasureHuntId}`))
        .set("Authorization", token)
        .send(treasureHunt);

    return res.body;
}

/**
 * Calls the backend api to retrieve the destination name and return it
 * @param destinationId of the destination to be retrieved
 * @returns {Promise<*>} the destination name given by the destination id
 */
export async function getDestination(destinationId) {
    let token = localStorage.getItem("authToken");

    const res = await superagent.get(endpoint(`/destinations/${destinationId}`)).set("Authorization", token);

    return res.body.destinationName;
}

/**
 * Calls an API, Google Maps in future, to get the timezone of a given destination
 * @param destinationId of the destination to get the timezone for
 * @returns {Promise<*>} the abbreviation of the timezone
 */
export async function getTimeZone(destinationId) {
    let token = localStorage.getItem("authToken");
    const res = await superagent.get(endpoint(`/destinations/${destinationId}`)).set("Authorization", token);
    let lat = res.body.destinationLat;
    let long = res.body.destinationLon;
    const google = await superagent.get(`http://api.timezonedb.com/v2.1/get-time-zone?key=1SNL8B7MWI5C&lat=${lat}&lng=${long}&by=position&format=json`);
    return google.body.abbreviation;

}