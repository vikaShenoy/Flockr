import superagent from "superagent";
import {endpoint} from "../../utils/endpoint";
import moment from "moment";

/**
 * Gets all of a users trips
 * @param {number} userId
 * @return {Object} The users trips
 */
export async function getTrips(userId) {
    const authToken = localStorage.getItem("authToken");

    const res = await superagent.get(endpoint(`/travellers/${userId}/trips`))
        .set("Authorization", authToken);
    return res.body;
}

/**
 * Finds the start date of the trip
 * @param {Object[]} tripDestinations
 */
export function findStart(tripDestinations) {
    for (const tripDestination of tripDestinations) {
        if (tripDestination.arrivalDate) {
            if (tripDestination.arrivalTime) {
                return moment(tripDestinations.arrivalDate).add(moment.duration(tripDestination.arrivalTime, "minutes"));
            } else {
                return moment(tripDestination.arrivalDate);
            }
        }

        if (tripDestination.departureDate) {
            if (tripDestination.departureTime) {
                return moment(tripDestinations.departureDate).add(moment.duration(tripDestination.departureTime, "minutes"));
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
            tripStatus = "Unknown";
        } else if (currentTime.isAfter(tripStart) && currentTime.isBefore(tripEnd)) {
            tripStatus = "Ongoing";
        } else if (currentTime.isBefore(tripStart)) {
            tripStatus = "Upcoming";
        } else if (currentTime.isAfter(tripEnd)) {
            tripStatus = "Passed";
        } else {
            tripStatus = "Unknown";
        }

        return {
            tripId: trip.tripId,
            tripName: trip.tripName,
            status: tripStatus
        };
    });
}

/**
 * Sorts the trips by the trip start date
 * @param {Object[]} trips
 */
export function sortTrips(trips) {
    return trips.sort((tripA, tripB) => {
        const startA = findStart(tripA.tripDestinations);
        const startB = findStart(tripB.tripDestinations);
        console.log(startA);
        console.log(startB);
        return startA - startB;
    });

    /*        tripA.departureDateMenu
        const tripAStart = findStart(tripA.tripDestinations);
        const tripBStart = findStart(tripB.tripDestinations);
        const tripAEnd = findStart(tripA.tripDestinations);
        const tripBEnd = findStart(tripB.tripDestinations);

        if (tripAStart.) {
            return 1;
        } else {
            return -1;
        }*/

    /*        return trips.map(trip => {
                const start = findStart(trip.tripDestinations);
                const end = findEnd(trip.tripDestinations);
                if (!start && !end) {
                    let index = trips.indexOf(trip);
                    trips.splice(index, 1);
                    trips.splice(0, 0, trip);
                }

                return trip;
            });*/
    /*
        let tripsWithUnknownStartDates = [];
        trips.map(trip => {
            const start = findStart(trip.tripDestinations);
            const end = findEnd(trip.tripDestinations);
            if (!start && !end) {
                let index = trips.indexOf(trip);
                tripsWithUnknownStartDates.push(index);
            }
        });

        return trips.map(trip => {
            let count = 0;
            for (let i = 0; i < tripsWithUnknownStartDates.length; i++) {
                trips.splice(tripsWithUnknownStartDates.get(i), 1);
                trips.splice(count, 0, trip);
            }
        });*/

    /*
            // If the trip doesn't have a start and an end, it's placed at the top.
            if (!tripStartA && !tripEndA) {
                tripsWithNoDates.push(tripA);
            }
            if (!tripStartB && !tripEndB) {
                tripsWithNoDates.push(tripB);
            }

            if (tripStartA < tripStartB) {
                sortedTrips.push(tripA);
                sortedTrips.push(tripB);
            } else {
                sortedTrips.push(tripB);
                sortedTrips.push(tripA);
            }*/

}



