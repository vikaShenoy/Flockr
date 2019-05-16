import { transformTrips, tripState } from "../../../src/components/TripList/TripListService";
import { transformTrip } from "../../../src/containers/Trip/TripService";
import moment from "moment";

describe("I can transform trips and a single trip", () => {
  test("I can transform a trip to a readable trip", () => {
    const arrivalDate = 1557985965164; // Epoch timestamp in milliseconds
    const arrivalTime = 130;
    const departureDate = 1558072419182;
    const departureTime = 320;

    const transformedTrip = transformTrip({
      tripName: "My trip",
      tripDestinations: [
        {
          arrivalDate,
          arrivalTime,
          departureDate,
          departureTime,
          destination: {
            destinationName: "Cucumber Land",
          },
        },
      ],
    });

    expect(transformedTrip.tripName).toBe("My trip");
    expect(transformedTrip.tripDestinations.length).toBe(1);
    expect(transformedTrip.tripDestinations[0].arrival).toBe("16/05/2019 at 02:10");
    expect(transformedTrip.tripDestinations[0].departure).toBe("17/05/2019 at 05:20");
    expect(transformedTrip.tripDestinations[0].destinationName).toBe("Cucumber Land");
  });

  test("I can transform trips from the past", () => {

    const trips = [{
      tripName: "My trip",
      tripDestinations: [
        {
          arrivalDate: moment().subtract(5, "days").valueOf(),
          arrivalTime: 30,
          departureDate: moment().subtract(1, "day").valueOf(),
          departureTime: 120
        },
      ],
    }];     

  const transformedTrips = transformTrips(trips);

  expect(transformedTrips[0].status).toBe(tripState.PASSED);
  });

  test("I can transform trips from the future", () => {

    const trips = [{
      tripName: "My trip",
      tripDestinations: [
        {
          arrivalDate: moment().add(5, "days").valueOf(),
          arrivalTime: 30,
          departureDate: moment().add(10, "days").valueOf(),
          departureTime: 120
        },
      ],
    }];     

  const transformedTrips = transformTrips(trips);

  expect(transformedTrips[0].status).toBe(tripState.UPCOMING);
  });

  test("I can transform trips that are ongoing", () => {

    const trips = [{
      tripName: "My trip",
      tripDestinations: [
        {
          arrivalDate: moment().subtract(5, "days").valueOf(),
          arrivalTime: 30,
          departureDate: moment().add(10, "days").valueOf(),
          departureTime: 120
        },
      ],
    }];     

  const transformedTrips = transformTrips(trips);

  expect(transformedTrips[0].status).toBe(tripState.ONGOING);
  });

  test("I can transform trips that are have an unknown state", () => {
    const trips = [{
      tripName: "My trip",
      tripDestinations: [
        {
          arrivalTime: 30,
          departureTime: 120
        },
      ],
    }];     

    const transformedTrips = transformTrips(trips);

    expect(transformedTrips[0].status).toBe(tripState.UNKNOWN);
  });

  












});

