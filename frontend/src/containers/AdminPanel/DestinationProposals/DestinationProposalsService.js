import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint"

/**
 * Gets all destination proposals 
 * @returns {Object} the destination proposals
 */
export async function getDestinationProposals() {
   const res = await superagent.get(endpoint("/destinations/proposals"))
                .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Accepts a proposal
 */
export async function acceptProposal(destinationProposalId) {
   await superagent.patch(endpoint(`/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));
}


/**
 * Declines a proposal
 */
export async function declineProposal(destinationProposalId) {
   await superagent.delete(endpoint(`/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));

}



