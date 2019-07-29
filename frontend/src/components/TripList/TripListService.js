import superagent from "superagent";
import { endpoint } from "../../utils/endpoint";
import moment from "moment";

// Specifies states that a trip can have
export const tripState = {
  UNKNOWN: "Unknown",
  ONGOING: "Ongoing",
  UPCOMING: "Upcoming",
  PASSED: "Passed"
};

/**
 * Gets all of a users trips
 * @param {number} userId
 * @return {Object} The users trips
 */
export async function getTrips(userId) {
  const authToken = localStorage.getItem("authToken");

  const res = await superagent.get(endpoint(`/users/${userId}/trips`)).set("Authorization", authToken);
  return res.body;
}

/**
 * Finds the start date of the trip
 * @param {Object[]} tripDestinations
 * @returns {moment} A moment object of the start date (and time if it exists)
 */
export function findStart(tripDestinations) {
  for (const tripDestination of tripDestinations) {
    if (tripDestination.arrivalDate) {
      if (tripDestination.arrivalTime) {
        return moment(tripDestination.arrivalDate).add(moment.duration(tripDestination.arrivalTime, "minutes"));
      } else {
        return moment(tripDestination.arrivalDate);
      }
    }

    if (tripDestination.departureDate) {
      if (tripDestination.departureTime) {
        return moment(tripDestination.departureDate).add(moment.duration(tripDestination.departureTime, "minutes"));
      } else {
        return moment(tripDestination.departureDate);
      }
    }
  }
  return false;
}

/**
 * Finds the end date of the trip
 * @param {Object[]} tripDestinations
 * @returns {moment} A moment object of the start date (and time if it exists)
 */
export function findEnd(tripDestinations) {
  for (let i = tripDestinations.length - 1; i > -1; i--) {
    if (tripDestinations[i].departureDate) {
      if (tripDestinations[i].departureTime) {
        return moment(tripDestinations[i].departureDate).add(moment.duration(tripDestinations[i].departureTime, "minutes"));
      } else {
        return moment(tripDestinations[i].departureDate);
      }
    }

    if (tripDestinations[i].arrivalDate) {
      if (tripDestinations[i].arrivalTime) {
        return moment(tripDestinations[i].arrivalDate).add(moment.duration(tripDestinations[i].arrivalTime, "minutes"));
      } else {
        return moment(tripDestinations[i].arrivalDate);
      }
    }
  }
  return false;
}

/**
 * Transforms the trip to the right structure
 * @param {Object[]} trips
 */
export function transformTrips(trips) {
  return trips.map(trip => {
    const tripStart = findStart(trip.tripDestinations);
    const tripEnd = findEnd(trip.tripDestinations);

    const currentTime = moment();

    let tripStatus;

    if (!tripStart || !tripEnd) {
      tripStatus = tripState.UNKNOWN;
    } else if (currentTime.isAfter(tripStart) && currentTime.isBefore(tripEnd)) {
      tripStatus = tripState.ONGOING;
    } else if (currentTime.isBefore(tripStart)) {
      tripStatus = tripState.UPCOMING;
    } else if (currentTime.isAfter(tripEnd)) {
      tripStatus = tripState.PASSED;
    } else {
      tripStatus = tripState.UNKNOWN;
    }

    return {
      tripId: trip.tripId,
      tripName: trip.tripName,
      status: tripStatus,
    };
  });
}

/**
 * Sorts the trips by the trip start date. If the trip doesn't have a start date, the trip
 * gets put at the top
 * @param {Object[]} trips
 */
export function sortTrips(trips) {
  return trips.sort((tripA, tripB) => {
    const startA = findStart(tripA.tripDestinations);
    const startB = findStart(tripB.tripDestinations);

    // Will put trips that don't have any dates at the top
    if (!startA && startB) {
      return -1;
    }

    if (!startB && startA) {
      return 1;
    }
    
    // If 2 trips compared don't have any dates or the dates are the same
    if (!startA && !startB || startA.isSame(startB)) {
      return tripB.tripId - tripA.tripId;
    } else {
      return startB.valueOf() - startA.valueOf();
    }
  });
}


/**
 * Function to call the backend and delete a trip from the database
 * @param tripId the ID of the trip to delete
 * @returns {Promise<*>} the body of the response
 */
export async function deleteTripFromList(tripId) {
  const authToken = localStorage.getItem("authToken");
  const userId = localStorage.getItem("userId");

  const res = await superagent.delete(endpoint(`/users/${userId}/trips/${tripId}`)).set("Authorization", authToken);

  return res.body;
}

export async function restoreTrip(tripId) {
  const authToken = localStorage.getItem("authToken");
  const userId = localStorage.getItem("userId");

    const res = await superagent.put(endpoint(`/users/${userId}/trips/${tripId}/restore`)).set("Authorization", authToken);

  return res.body;
}




