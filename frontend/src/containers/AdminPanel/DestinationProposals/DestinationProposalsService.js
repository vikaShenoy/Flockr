import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint"

/**
 * Get all destination proposals.
 * @returns {Object} the destination proposals
 */
export async function getDestinationProposals() {
   const res = await superagent.get(endpoint("/destinations/proposals"))
                .set("Authorization", localStorage.getItem("authToken"));

  return res.body;
}

/**
 * Get a single destination proposal.
 * @param destinationProposalId id of the proposal to retrieve.
 * @returns {Promise<*>} a single destination proposal.
 */
export async function getDestinationProposal(destinationProposalId) {
   const res = await superagent.get(endpoint(`/destinations/proposals/${destinationProposalId}`))
                 .set("Authorization", localStorage.getItem("authToken"));
   return res.body;
}

/**
 * Accept a proposal.
 */
export async function acceptProposal(destinationProposalId) {
   await superagent.patch(endpoint(`/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));
}


/**
 * Decline a proposal.
 */
export async function declineProposal(destinationProposalId) {
   const userId = localStorage.getItem("userId");
   await superagent.delete(endpoint(`/users/${userId}/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));
}



