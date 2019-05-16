import moment from "moment";
import { sortTrips } from "../../../src/components/TripList/TripListService";

describe("I can sort trips by their start date", () => {
  test("That trips are sorting correctly", () => {
    const trips = [
      {
        tripId: 1,
        tripDestinations: [
          {
            arrivalDate: moment()
              .add(1, "month")
              .valueOf(),
            arrivalTime: 680,
            departureDate: moment()
              .add(2, "month")
              .valueOf(),
            departureTime: 390,
          },
          {
            arrivalDate: moment()
              .add(3, "month")
              .valueOf(),
            arrivalTime: 690,
            departureDate: moment()
              .add(4, "month")
              .valueOf(),
            departureTime: 450,
          },
        ],
      },
      {
        // Trip should go to the top since no dates are specified
        tripId: 2,
        tripDestinations: [
          {
            arrivalTime: 680,
            departureTime: 390,
          },
          {
            arrivalTime: 400,
          },
        ],
      },
      {
        tripId: 3,
        tripDestinations: [
          {
            arrivalDate: moment()
              .add(1, "day")
              .valueOf(),
            arrivalTime: 680,
            departureDate: moment()
              .add(2, "days")
              .valueOf(),
            departureTime: 390,
          },
          {
            arrivalDate: moment()
              .add(3, "days")
              .valueOf(),
            arrivalTime: 690,
            departureDate: moment()
              .add(4, "days")
              .valueOf(),
            departureTime: 450,
          },
        ],
      },
    ];

    const sortedTrips = sortTrips(trips);

    expect(sortedTrips[0].tripId).toBe(2);
    expect(sortedTrips[1].tripId).toBe(3);
    expect(sortedTrips[2].tripId).toBe(1);
  });
});
