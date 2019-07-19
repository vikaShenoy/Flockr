import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

/**
 * Gets all traveller types
 */
export async function getTravellerTypes() {
  const res = await superagent.get(endpoint("/users/types"))
    .set("Authorization", localStorage.getItem("authToken"))

  return res.body;
}

export async function sendProposal(destinationId, travellerTypeIds) {
  await superagent.post(endpoint(`/destinations/${destinationId}/proposals`))
    .send({
      travellerTypeIds 
    })
    .set("Authorization", localStorage.getItem("authToken"))
}