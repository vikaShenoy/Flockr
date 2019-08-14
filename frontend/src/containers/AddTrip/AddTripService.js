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
export async function addTrip(name, tripNodes, userIds) {
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


/**
 * Add a sub trip to a parent trip using the PUT endpoint.
 * Called after the user creates a new subtrip in the trip item sidebar.
 * @param parentTrip the trip which is being updated.
 * @param subtripId id of the subtrip
 * @param subtripNodes tripNodes of the subtrip
 * @returns {Promise<*>}
 */
export async function addSubTrip(parentTrip, subTripId, subTripNodes) {
  const userId = localStorage.getItem("userId");
  const transformedTripNodes = transformTripNodes(subTripNodes);
  const subTrip = {
    tripNodeId: subTripId,
    nodeType: "TripComposite",
    tripNodes: transformedTripNodes
  };
  let updatedTripNodes = parentTrip.tripNodes;
  updatedTripNodes.push(subTrip);
  console.log("Updated trip nodes: ");
  console.log(updatedTripNodes);
  // Send a put request which adds the sub trip to the parent trip's tripNodes list.
  const res = await superagent.put(endpoint(`/users/${userId}/trips/${parentTrip.tripNodeId}`))
      .set("Authorization", localStorage.getItem("authToken"))
      .send({
        name: parentTrip.name,
        userIds: parentTrip.userIds,
        tripNodes: updatedTripNodes
      });
  return res.body;
}

// TODO - edit this so if the object contains destination it goes into the object to get the destination id.
function transformTripNodes(tripNodes) {
  let transformedTripNodes = tripNodes.map(tripNode => {
    const transformedTripDestination = {};
    transformedTripDestination.nodeType = "TripDestinationLeaf";
    transformedTripDestination.arrivalDate = moment(tripNode.arrivalDate).valueOf();
    transformedTripDestination.arrivalTime =
        tripNode.arrivalTime === null ? null : moment.duration(tripNode.arrivalTime).asMinutes();
    transformedTripDestination.departureDate = moment(tripNode.departureDate).valueOf();
    transformedTripDestination.departureTime =
        tripNode.departureTime === null ? null : moment.duration(tripNode.departureTime).asMinutes();
    return transformedTripDestination;
  });
  return transformedTripNodes;
}


export async function getAllUsers() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users`))
      .set("Authorization", authToken);

  return res.body;
}

