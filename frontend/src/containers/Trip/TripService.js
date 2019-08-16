import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment, { max } from "moment";

/**
 * Sends a request to get a trip
 * @param {number} userId 
 * @param {number} tripId 
 */
export async function getTrip(tripId) {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/trips/${tripId}`))
    .set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

function formatTime(time) {
  return moment.utc(time.as("milliseconds")).format("HH:mm");
}

export async function getTrips() {
  const userId = localStorage.getItem("userId");
  const res = await superagent.get(endpoint(`/users/${userId}/trips`))
    .set("Authorization", localStorage.getItem("authToken"));
  return res.body;
}

/**
 * Transform/format a trip response object.
 * @param {Object} trip The trip to transform
 * @param {string} trip.tripName The name of the trip
 * @param {Object[]} trip.tripDestinations The destinations in a trip
 * @param {number} trip.tripDestinations[].arrivalDate The arrival date of the destination
 * @param {number} trip.tripDestinations[].arrivalTime The arrival time of the destination
 * @param {number} trip.tripDestinations[].departureDate The departure date of the destination
 * @param {number} trip.tripDestinations[].departureTime The departure time of the destination
 * @return {Object} The transformed trip
 */ 
export function transformTripResponse(trip) {
  return {
    tripId: trip.tripId,
    tripName: trip.tripName,
    users: trip.users,
    tripDestinations: trip.tripDestinations.map(tripDestination => {
      return {
        tripDestinationId: tripDestination.tripDestinationId,
        destination: tripDestination.destination,
        arrivalDate: !tripDestination.arrivalDate ? null : moment(tripDestination.arrivalDate).format("YYYY-MM-DD"),
        arrivalTime: !tripDestination.arrivalTime ? null : formatTime(moment.duration(tripDestination.arrivalTime, "minutes")),
        departureDate: !tripDestination.departureDate ? null : moment(tripDestination.departureDate).format("YYYY-MM-DD"),
        departureTime: !tripDestination.departureTime ? null : formatTime(moment.duration(tripDestination.departureTime, "minutes")),
      };
    }),
  };
}

/**
 * Checks if destinations are contiguious
 * @param {Array} tripDestinations The list of destinations to swap
 * @returns {boolean} True if the destinations are contigious, false otherwise
 */
export function contiguousDestinations(tripDestinations) {
  let oldDestinationId = tripDestinations[0].destination.destinationId;
  for (const tripDestination of tripDestinations.slice(1)) {
    
    if (tripDestination.destination.destinationId === oldDestinationId) {
      return true;
    }
    oldDestinationId = tripDestination.destination.destinationId;
  }

  return false;
}

/**
 * Checks if destinations are contiguous after they have been swapped
 * @param {Array} tripDestinations list of destinations to swap and check
 * @param {number} newIndex index to be swapped to
 * @param {number} oldIndex index to be swapped by
 * @returns {boolean} True if the trip destinations are contiguous, false otherwise
 */
export function contiguousReorderedDestinations(tripNodes, indexes) {

  // TODO - needs to be implemented. See task 5867.
  // const copiedTripDestinations = [...tripDestinations];
  // //[copiedTripDestinations[newIndex], copiedTripDestinations[oldIndex]] = [tripDestinations[oldIndex], tripDestinations[newIndex]];
  // let temp = copiedTripDestinations[oldIndex];
  // copiedTripDestinations.splice(oldIndex, 1);
  // copiedTripDestinations.splice(newIndex, 0, temp);
  // return contiguousDestinations(copiedTripDestinations);
  return false;
}



/**
 * Edit a trip. Send a request to the edit trip backend endpoint with
 * the trip data to edit.
 * @param {number} tripId - The ID of the trip to edit
 * @param {string} tripName - The edited trip name
 * @param {Object[]} tripNodes - The edited trip destinations
 */
export async function editTrip(trip) {
   const userId = localStorage.getItem("userId");
   const authToken = localStorage.getItem("authToken");


   const transformedTripNodes = trip.tripNodes.map((tripNode) => {
     if (tripNode.nodeType === "TripComposite") {
       return {
         nodeType: tripNode.nodeType,
         tripNodeId: tripNode.tripNodeId
       };
      } else {
       return {
         destinationId: tripNode.destination.destinationId,
         arrivalDate: tripNode.arrivalDate ? moment(tripNode.arrivalDate).valueOf() : null,
         arrivalTime: tripNode.arrivalTime ? moment.duration(tripNode.arrivalTime).asMinutes() : null,
         departureDate: tripNode.departureDate ? moment(tripNode.departureDate).valueOf() : null,
         departureTime: tripNode.departureTime ? tripNode.departureTime === null
         || tripNode.departureTime === ""? null : moment.duration(tripNode.departureTime).asMinutes() : null,
         nodeType: tripNode.nodeType
       }
     }
   });
   const tripData = {
     name: trip.name,
     tripNodes: transformedTripNodes,
   };

   if (trip.users) {
     tripData.userIds = trip.users.map(user => user.userId);
   }
   await superagent.put(endpoint(`/users/${userId}/trips/${trip.tripNodeId}`))
    .set("Authorization", authToken)
    .send(tripData);
}

/**
 * Maps trip nodes (tree structure) to destinations (flat structure) using recursion
 * @param depth the recursion level. Used to indicate destinations which are
 * part of the same sub trip for coloring on the map.
 * @param {} tripNode The current trip node at a specific recursion level
 */
export function mapTripNodesToDestinations(tripNode, depth=0) {
  if (tripNode.nodeType === "TripDestinationLeaf") {
    const destination = tripNode.destination;
    destination.group = depth;
    return destination;
    //return tripNode.destination;
  }
  
  let destinations = [];
  for (const currentTripNode of tripNode.tripNodes) {
    destinations = [...destinations, mapTripNodesToDestinations(currentTripNode, depth + 1)];
  }
  return destinations.flatMap(destination => destination)
}

/**
 * Recursively finds a trip node by it's trip node ID 
 * @param {number} tripNodeId The ID to find
 * @param {Object} tripNode The current trip node that is being searched
 * @return {Object} The tripNode object that was found
 */
export function getTripNodeById(tripNodeId, tripNode) {

  if (tripNode.tripNodeId === tripNodeId) {
    return tripNode;
  }

  let tripNodeToFind = null;

  for (const currentTripNode of tripNode.tripNodes) {
    tripNodeToFind = getTripNodeById(tripNodeId, currentTripNode);
    if (tripNodeToFind) return tripNodeToFind
  }

  return tripNodeToFind;
}
