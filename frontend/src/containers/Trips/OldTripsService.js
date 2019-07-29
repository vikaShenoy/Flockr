import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";

/**
 * Call the backend and delete a trip
 * @param tripId the ID of the trip to delete
 * @returns {Promise<*>} the body of the response
 */
export async function deleteTripFromList(tripId) {
    const authToken = localStorage.getItem("authToken");
    const userId = localStorage.getItem("userId");
    const res = await superagent.delete(endpoint(`/users/${userId}/trips/${tripId}`)).set("Authorization", authToken);
    return res.body;
}

/**
 * Restore the trip in the backend
 * @param {Number | String} tripId the id of the trip being restored
 * @returns {Promise<any>} the body of the response
 */
export async function restoreTrip(tripId) {
    const authToken = localStorage.getItem("authToken");
    const userId = localStorage.getItem("userId");
    const res = await superagent.put(endpoint(`/users/${userId}/trips/${tripId}/restore`)).set("Authorization", authToken);
    return res.body;
}