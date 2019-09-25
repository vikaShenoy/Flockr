import superagent from "superagent";
import {endpoint} from "../../../utils/endpoint"

/**
 * Get all destination proposals.
 * @param page: the number of the page of proposals to retrieve
 * @returns {Object} the destination proposals.
 */
export async function getDestinationProposals(page) {
   const res = await superagent.get(endpoint("/destinations/proposals?page=" + page))
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
 * Accept a destination proposal.
 * @param destinationProposalId id of the proposal to accept.
 */
export async function acceptProposal(destinationProposalId) {
   await superagent.patch(endpoint(`/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));
}

/**
 * Decline a destination proposal.
 * @param destinationProposalId id of the proposal to reject (delete).
 */
export async function declineProposal(destinationProposalId) {
   const userId = localStorage.getItem("userId");
   await superagent.delete(endpoint(`/users/${userId}/destinations/proposals/${destinationProposalId}`))
                .set("Authorization", localStorage.getItem("authToken"));
}

/**
 * Sends a request to update a destination Proposal.
 * @param proposal the proposal to update.
 * @return {Promise<Object>} the promise containing the updated proposal.
 */
export async function updateProposal(proposal) {
  const authToken = localStorage.getItem("authToken");
  const response = await superagent.put(
      endpoint(`/destinations/proposals/${proposal.destinationProposalId}`))
  .set("Authorization", authToken)
  .send(proposal);

  return response.body;
}


