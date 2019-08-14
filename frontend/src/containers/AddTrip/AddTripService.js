import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

/**
 * Sends a request to add a trip.
 * @param {string} name name of the trip to add.
 * @param {object[]} tripDestinations list of trip destinations to add as part of the trip.
 * @param {Array<number>} userIds The userID's to add to the trip
 * @return response from backend.
 */
export async function addTrip(name, tripDestinations, userIds) {
  const userId = localStorage.getItem("userId");
  const transformedTripDestinations = transformTripNodes(tripDestinations);

  const res = await superagent.post(endpoint(`/users/${userId}/trips`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send({
    name,
    tripNodes: transformedTripDestinations,
    userIds
  });
  return res.body;
}


export async function addSubtrip(parentTrip, tripId, tripNodes) {
  const userId = localStorage.getItem("userId");
  const transformedTripNodes = transformTripNodes(tripNodes);
  const subTrip = {
    tripNodeId: tripId,
    nodeType: "TripComposite",
    tripNodes: transformedTripNodes
  }
  // TODO - finish off this request
}



export async function getAllUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users`))
    .set("Authorization", authToken);

  return res.body;
}


function transformTripNodes(tripDestination) {
  const transformedTripDestination = {};
  transformedTripDestination.nodeType = "TripDestinationLeaf";
  transformedTripDestination.destinationId = tripDestination.destinationId;
  transformedTripDestination.arrivalDate = moment(tripDestination.arrivalDate).valueOf();
  transformedTripDestination.arrivalTime =
      tripDestination.arrivalTime === null ? null : moment.duration(tripDestination.arrivalTime).asMinutes();
  transformedTripDestination.departureDate = moment(tripDestination.departureDate).valueOf();
  transformedTripDestination.departureTime =
      tripDestination.departureTime === null ? null : moment.duration(tripDestination.departureTime).asMinutes();
  return transformedTripDestination;
}
