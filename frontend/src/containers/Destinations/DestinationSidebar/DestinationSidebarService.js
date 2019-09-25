import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Get a list of all destinations (private and public) destinations of the user
 * @returns {Promise<Array>} the users public and private destinations
 */
export async function getYourDestinations() {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/destinations`))
      .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Gets the trips of the user
 * @returns {Promise<*>} the trips of the user
 */
export async function getUserTrips() {
  const userId = localStorage.getItem("userId");
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users/${userId}/trips`))
    .set("Authorization", authToken);

  return res.body;
}

/**
 * Delete a destination from the database
 * @param destinationId int id of the destination
 * @returns {Promise<void>} contains nothing
 */
export async function deleteDestination(destinationId) {
  await superagent.delete(endpoint(`/destinations/${destinationId}`))
    .set("Authorization", localStorage.getItem("authToken"));
}

/**
 * Undoes deletion of a destination
 * @param {number} destinationId ID of destination to undo deletion of
 */
export async function undoDeleteDestination(destinationId) {
  const authToken = localStorage.getItem("authToken");
  await superagent
    .put(endpoint(`/destinations/${destinationId}/undodelete`))
    .set("Authorization", authToken);
}


