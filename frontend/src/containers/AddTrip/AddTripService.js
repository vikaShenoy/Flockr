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
export async function addTrip(name, tripNodes, userIds) {
  const userId = localStorage.getItem("userId");
  const transformedTripNodes = transform(tripNodes);
  const res = await superagent.post(endpoint(`/users/${userId}/trips`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send({
    name,
    tripNodes: transformedTripNodes,
    userIds
  });
  return res.body;
}


function transform(tripNodes) {
  return tripNodes.map(tripNode => {
    let transformedTripNode = {};

    // TODO - is this necessary?
    if (tripNode.hasOwnProperty("tripNodeId")) {
      transformedTripNode.tripNodeId = tripNode.tripNodeId;
    }
    if (tripNode.nodeType === "TripComposite") {
      transformedTripNode.nodeType = "TripComposite";
      transformedTripNode.tripNodes = transform(tripNode.tripNodes);
    } else {
      transformedTripNode.nodeType = "TripDestinationLeaf";
      // TODO - Check. Frontend test data has it structured like this.
      if (tripNode.hasOwnProperty("destination")) {
        transformedTripNode.destinationId = tripNode.destination.destinationId;
      } else {
        transformedTripNode.destinationId = tripNode.destinationId;
      }
    }

    transformedTripNode.arrivalDate = moment(tripNode.arrivalDate).valueOf();
    transformedTripNode.arrivalTime =
        tripNode.arrivalTime === null ? null : moment.duration(tripNode.arrivalTime).asMinutes();
    transformedTripNode.departureDate = moment(tripNode.departureDate).valueOf();
    transformedTripNode.departureTime =
        tripNode.departureTime === null ? null : moment.duration(tripNode.departureTime).asMinutes();
    return transformedTripNode;
  });
}

// Old function. Delete at the end of task.
/*
function transformTripNodes(tripNodes) {
  let transformedTripNodes = tripNodes.map(tripNode => {

    let transformedTripNode = {};

    if (tripNode.hasOwnProperty("destination")) {
      transformedTripNode.destinationId = tripNode.destination.destinationId;
    } else {
      transformedTripNode.destinationId = tripNode.destinationId;
    }
    if (tripNode.nodeType === "TripComposite") {

    }
    transformedTripNode.nodeType = "TripDestinationLeaf";
    transformedTripNode.arrivalDate = moment(tripNode.arrivalDate).valueOf();
    transformedTripNode.arrivalTime =
        tripNode.arrivalTime === null ? null : moment.duration(tripNode.arrivalTime).asMinutes();
    transformedTripNode.departureDate = moment(tripNode.departureDate).valueOf();
    transformedTripNode.departureTime =
        tripNode.departureTime === null ? null : moment.duration(tripNode.departureTime).asMinutes();
    return transformedTripNode;
  });
  return transformedTripNodes;
}*/


export async function getAllUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users`))
      .set("Authorization", authToken);

  return res.body;
}

