import Vue from "vue";
import Vuetify from "vuetify";
Vue.use(Vuetify);
import "jest";

import { findStart, findEnd } from "../../../src/components/TripList/TripListService";

describe("Trip dates work as expected", () => {

  test("I can find the start of a trip by an arrival date and time", () => {
    const tripDestinations = [
      {
        arrivalDate: 1557985965164,
        arrivalTime: 130,
        departureDate: 1558072419182,
        departureTime: 320
      },
      {
        arrivalDate: 1557986965164,
        arrivalTime: 240,
        departureDate: 1568072419182,
        departureTime: 104
      }
    ];

    const startDate = findStart(tripDestinations);
    expect(startDate.valueOf()).toBe(tripDestinations[0].arrivalDate + tripDestinations[0].arrivalTime * 60 * 1000);
  });

  test("I can find the start date of a trip by an arrival date", () => {
    const tripDestinations = [
      {
        arrivalDate: 1557985965164,
        departureDate: 1558072419182
      },
      {
        arrivalDate: 1577985965164,
        departureDate: 1958072419182
      }
    ];

    const startDate = findStart(tripDestinations);

    expect(startDate.valueOf()).toBe(tripDestinations[0].arrivalDate);
  });

  test("I can find the start date of an departure date and time", () => {
    const tripDestinations = [
      {
        departureDate: 1558072419182,
        departureTime: 320
      },
      {
        arrivalDate: 1557986965164,
        arrivalTime: 240,
        departureDate: 1568072419182,
        departureTime: 104
      }
    ];

    const startDate = findStart(tripDestinations);
    expect(startDate.valueOf()).toBe(tripDestinations[0].departureDate + tripDestinations[0].departureTime * 60 * 1000);

  });

  test("I can find the start date of an departure date", () => {
    const tripDestinations = [
      {
        departureDate: 1558072419182
      },
      {
        arrivalDate: 1577985965164,
        departureDate: 1958072419182
      }
    ];

    const startDate = findStart(tripDestinations);

    expect(startDate.valueOf()).toBe(tripDestinations[0].departureDate);

  });

  test("I can find the end of a trip by a departure date and time", () => {
    const tripDestinations = [
      {
        arrivalDate: 1557985965164,
        arrivalTime: 130,
        departureDate: 1558072419182,
        departureTime: 320
      },
      {
        arrivalDate: 1557986965164,
        arrivalTime: 240,
        departureDate: 1568072419182,
        departureTime: 104
      }
    ];

    const startDate = findEnd(tripDestinations);
    expect(startDate.valueOf()).toBe(tripDestinations[1].departureDate + tripDestinations[1].departureTime * 60 * 1000);
  });

  test("I can find the end date of a trip by a departure date", () => {
    const tripDestinations = [
      {
        arrivalDate: 1557985965164,
        departureDate: 1558072419182
      },
      {
        arrivalDate: 1577985965164,
        departureDate: 1958072419182
      }
    ];

    const startDate = findEnd(tripDestinations);

    expect(startDate.valueOf()).toBe(tripDestinations[1].departureDate);
  });

  test("I can find the end date of an arrival date and time", () => {
    const tripDestinations = [
      {
        departureDate: 1558072419182,
        departureTime: 320
      },
      {
        arrivalDate: 1557986965164,
        arrivalTime: 240
      }
    ];

    const startDate = findEnd(tripDestinations);
    expect(startDate.valueOf()).toBe(tripDestinations[1].arrivalDate + tripDestinations[1].arrivalTime * 60 * 1000);

  });

  test("I can find the end date of an arrival date", () => {
    const tripDestinations = [
      {
        departureDate: 1558072419182
      },
      {
        arrivalDate: 1577985965164,
      }
    ];

    const startDate = findEnd(tripDestinations);

    expect(startDate.valueOf()).toBe(tripDestinations[1].arrivalDate);
  });

  test("I cannot get a start date if a date is not entered is not defined", () => {
    const tripDestinations = [
      {
        departureTime: 340
      },
      {
        arrivalTime: 200
      }
    ];

    const startDate = findStart(tripDestinations);

    expect(startDate.valueOf()).toBe(false);
  });

  test("I cannot get an end date if a date is not entered is not defined", () => {
    const tripDestinations = [
      {
        departureTime: 340
      },
      {
        arrivalTime: 200
      }
    ];

    const startDate = findEnd(tripDestinations);

    expect(startDate.valueOf()).toBe(false);
  });

  
});
