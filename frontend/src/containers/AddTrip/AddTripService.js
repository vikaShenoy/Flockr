import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";
import {editTrip} from "../EditTrip/EditTripService";

/**
 * Sends a request to add a trip.
 * @param {string} name name of the trip to add.
 * @param {object[]} tripDestinations list of trip destinations to add as part of the trip.
 * @param {Array<number>} userIds The userID's to add to the trip
 * @return response from backend.
 */
export async function createTrip(name, tripNodes, userIds) {
  const userId = localStorage.getItem("userId");
  const transformedTripNodes = transformTripNodes(tripNodes);
  const res = await superagent.post(endpoint(`/users/${userId}/trips`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send({
    name,
    tripNodes: transformedTripNodes,
    userIds
  });
  return res.body;
}

function transformTripNodes(tripNodes) {
  return tripNodes.map(tripNode => {
    let transformedTripNode = {};
    transformedTripNode.nodeType = "TripDestinationLeaf";
    transformedTripNode.destinationId = tripNode.destinationId;
    transformedTripNode.arrivalDate = moment(tripNode.arrivalDate).valueOf();
    transformedTripNode.arrivalTime =
        tripNode.arrivalTime === null ? null : moment.duration(tripNode.arrivalTime).asMinutes();
    transformedTripNode.departureDate = moment(tripNode.departureDate).valueOf();
    transformedTripNode.departureTime =
        tripNode.departureTime === null ? null : moment.duration(tripNode.departureTime).asMinutes();
    return transformedTripNode;
  });
}


/**
 * Gets all users
 * @returns {Array} Returns a list of users
 */
export async function getAllUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users`))
    .set("Authorization", authToken);

  return res.body;
}
