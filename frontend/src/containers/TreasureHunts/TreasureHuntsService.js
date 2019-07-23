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