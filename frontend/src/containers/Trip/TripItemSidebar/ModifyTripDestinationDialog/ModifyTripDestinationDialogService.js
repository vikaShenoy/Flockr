import superagent from "superagent";
import { endpoint } from "../../../../utils/endpoint";
import moment from "moment";

/**
 * Gets all destinations
 */
export async function getDestinations() {
  const authToken = localStorage.getItem("authToken");
  const res = await superagent.get(endpoint("/destinations")).set("Authorization", authToken);
  return res.body;
}

/**
 * Transforms a formatted trip back to timestamps that the server understands
 */
export function transformFormattedTrip(trip) {
   const transformedTripDestinations = trip.tripDestinations.map(tripDestination => {
     return {
       destinationId: tripDestination.destination.destinationId,
       arrivalDate: tripDestination.arrivalDate ? moment(tripDestination.arrivalDate).valueOf() : null,
       arrivalTime: tripDestination.arrivalTime ? moment.duration(tripDestination.arrivalTime).asMinutes() : null,
       departureDate: tripDestination.departureDate ? moment(tripDestination.departureDate).valueOf() : null,
       departureTime: tripDestination.departureTime ? moment.duration(tripDestination.departureTime).asMinutes() : null
     }
  }); 

  return {...trip, tripDestinations: transformedTripDestinations};
}

/**
 * Edit a trip. Send a request to the edit trip backend endpoint with
 * the trip data to edit.
 * @param {number} tripId - The ID of the trip to edit
 * @param {string} tripName - The edited trip name
 * @param {Object[]} tripDestinations - The edited trip destinations
 */
export async function editTrip(tripId, trip) {
   const userId = localStorage.getItem("userId");
   const authToken = localStorage.getItem("authToken");

   await superagent.put(endpoint(`/users/${userId}/trips/${tripId}`))
    .send({
      tripName: trip.tripName,
      tripDestinations: trip.tripDestinations,
      userIds:  trip.users.map(user => user.userId)
    })
    .set("Authorization", authToken);
    
} 

