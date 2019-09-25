import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

/**
 * Formats the time like the specified format
 * @param time
 * @returns {string}
 */
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
        arrivalDate: tripDestination.arrivalDate === 0 ? null : moment(tripDestination.arrivalDate).format("YYYY-MM-DD"),
        arrivalTime: tripDestination.arrivalTime === -1 ? null : formatTime(moment.duration(tripDestination.arrivalTime, "minutes")),
        departureDate: tripDestination.departureDate === 0 ? null : moment(tripDestination.departureDate).format("YYYY-MM-DD"),
        departureTime: tripDestination.departureTime === -1 ? null : formatTime(moment.duration(tripDestination.departureTime, "minutes")),
      };
    }),
  };
}

/**
 * Send a request to get a trip
 * @param {number} tripId The trip ID to get
 * @param {userId} The userID of who to get the trip for
 */
export async function getTrip(tripId, userId) {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint(`/users/${userId}/trips/${tripId}`))
    .set("Authorization", authToken);
  return res.body;
}


