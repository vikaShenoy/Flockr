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
    tripName: trip.tripName,
    tripDestinations: trip.tripDestinations.map(tripDestination => {
      return {
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
 * Checks if two destinations are contiguious after they have been swapped
 * @param {Array} tripDestinations The list of destinations to swap
 * @param {number} newIndex The index to swap to
 * @param {number} oldIndex The index to swap from
 * @returns {boolean} True if the destinations are contigious, false otherwise
 */
export function contiguousDestinations(tripDestinations, newIndex, oldIndex) {
  const copiedTripDestinations = [...tripDestinations];
  [copiedTripDestinations[newIndex], copiedTripDestinations[oldIndex]] = [tripDestinations[oldIndex], tripDestinations[newIndex]];

  let oldDestinationId = copiedTripDestinations[0].destination.destinationId;

  for (let i = 1; i < copiedTripDestinations.length; i ++) {
    
    if (copiedTripDestinations[i].destination.destinationId === oldDestinationId) {
      return true;
    }

    oldDestinationId = copiedTripDestinations[i].destination.destinationId;
  }

  return false;
}

/**
 * Edit a trip. Send a request to the edit trip backend endpoint with
 * the trip data to edit.
 * @param {number} tripId - The ID of the trip to edit
 * @param {string} tripName - The edited trip name
 * @param {Object[]} tripDestinations - The edited trip destinations
 */
export async function editTrip(tripId, tripName, tripDestinations) {
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
      tripDestinations: transformedTripDestinations
    });

}

