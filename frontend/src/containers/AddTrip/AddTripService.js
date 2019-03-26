import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

/**
 * Sends a request to add a trip
 * @param {string} tripName 
 * @param {object[]} tripDestinations 
 */
export async function addTrip(tripName, tripDestinations) {
  const transformedTripDestinations = tripDestinations.map(tripDestination => {
    const transformedTripDestination = {};

    transformedTripDestination.destinationId = tripDestination.destinationId;
    transformedTripDestination.arrivalDate = moment(tripDestination.arrivalDate).valueOf();
    transformedTripDestination.arrivalTime = tripDestination.arrivalTime === null ? null : moment.duration(tripDestination.arrivalTime).asMinutes();
    transformedTripDestination.departureDate = moment(tripDestination.departureDate).valueOf(); 
    transformedTripDestination.departureTime = tripDestination.departureTime === null ? null : moment.duration(tripDestination.departureTime).asMinutes();

    return transformedTripDestination;
  }); 


  const res = await superagent.post(endpoint(`/travellers/${localStorage.getItem("userId")}/trips`))
  .set("Authorization", localStorage.getItem("authToken"))
  .send({
    tripName,
    tripDestinations: transformedTripDestinations
  });
  return res.body;
}
