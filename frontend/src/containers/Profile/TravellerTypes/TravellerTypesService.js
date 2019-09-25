import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Gets all traveller types
 */
export async function getAllTravellerTypes() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/users/types"))
    .set("Authorization", authToken);
  return res.body;
}

/**
 * Updates the user's traveller types with the newly chosen traveller types of the user
 * @param {number} userId The ID of the traveller
 * @param {number} travellerTypeIds The ID's of the traveller types to update
 */
export function updateTravellerTypes(userId, travellerTypeIds) {
  const authToken = localStorage.getItem("authToken");
  return superagent.patch(endpoint(`/users/${userId}`))
      .set("Authorization", authToken)
      .send({travellerTypes: travellerTypeIds});
}