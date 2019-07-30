import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Get all traveller types in the db by calling an endpoint.
 */
export async function getTravellerTypes() {
  const res = await superagent.get(endpoint("/users/types"))
    .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Send a destination traveller type proposal to the admins for review.
 * @param destinationId id of the destination the proposal is for.
 * @param travellerTypeIds ids of the traveller types being proposed for the destination.
 * @returns {Promise<*>} body of the endpoint response.
 */
export async function sendProposal(destinationId, travellerTypeIds) {
  const userId = localStorage.getItem("userId");
  const res =  await superagent.post(endpoint(`/users/${userId}/destinations/${destinationId}/proposals`))
    .send({
      travellerTypeIds 
    })
    .set("Authorization", localStorage.getItem("authToken"))

  return res.body;
}