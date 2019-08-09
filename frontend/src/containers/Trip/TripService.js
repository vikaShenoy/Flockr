import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

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
export function contiguousReorderedDestinations(tripDestinations, newIndex, oldIndex) {

  const copiedTripDestinations = [...tripDestinations];
  //[copiedTripDestinations[newIndex], copiedTripDestinations[oldIndex]] = [tripDestinations[oldIndex], tripDestinations[newIndex]];
  let temp = copiedTripDestinations[oldIndex];
  copiedTripDestinations.splice(oldIndex, 1);
  copiedTripDestinations.splice(newIndex, 0, temp);
  return contiguousDestinations(copiedTripDestinations);
}




/**
 * Edit a trip. Send a request to the edit trip backend endpoint with
 * the trip data to edit.
 * @param {number} tripId - The ID of the trip to edit
 * @param {string} tripName - The edited trip name
 * @param {Object[]} tripDestinations - The edited trip destinations
 */
export async function editTrip(tripId, tripName, tripDestinations, users) {
   const userId = localStorage.getItem("userId");

   const transformedTripDestinations = tripDestinations.map(tripDestination  => ({
    destinationId: tripDestination.destination.destinationId,
    arrivalDate: tripDestination.arrivalDate ? moment(tripDestination.arrivalDate).valueOf() : null,
    arrivalTime: tripDestination.arrivalTime ? moment.duration(tripDestination.arrivalTime).asMinutes() : null,
    departureDate: tripDestination.departureDate ? moment(tripDestination.departureDate).valueOf() : null,
    departureTime: tripDestination.departureTime ? tripDestination.departureTime === null || tripDestination.departureTime === ""? null : moment.duration(tripDestination.departureTime).asMinutes() : null,
   })); 

  const authToken = localStorage.getItem("authToken");

  await superagent.put(endpoint(`/users/${userId}/trips/${tripId}`))
    .set("Authorization", authToken)
    .send({
      tripName,
      tripDestinations: transformedTripDestinations,
      userIds: users.map(user => user.userId)
    });
}

/**
 * Maps trip nodes (tree structure) to destinations (flat structure) using recursion
 * @param {} tripNode The current trip node at a specific recursion level
 */
export function mapTripNodesToDestinations(tripNode) {
  if (tripNode.nodeType === "TripDestinationLeaf") {
    return tripNode.destination;
  }
  
  let destinations = [];
  for (const currentTripNode of tripNode.tripNodes) {
    destinations = [...destinations, mapTripNodesToDestinations(currentTripNode)]; 
  }

  return destinations.flatMap(destination => destination)
}

