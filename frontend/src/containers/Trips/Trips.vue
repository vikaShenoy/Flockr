<template>
  <div class="trips-container">
    <div v-if="trips" class="col-md-8 offset-md-2">
      <TripItem v-for="trip in trips" v-bind:key="trip.tripId" :trip="trip"/>
    </div>

    <v-btn
      id="add-trip"
      fab
      dark
      color="secondary"
    >
      <v-icon
        dark
        @click="$router.push('/trips/add')"
      >add</v-icon>
    </v-btn>
  </div>
</template>


<script>
import { getTrips, transformTrips, sortTrips } from "./TripsService.js";
import TripItem from "./TripItem/TripItem";

export default {
  components: {
    TripItem
  },
  data() {
    return {
      trips: null
    };
  },
  mounted() {
    this.getTrips();
  },
  methods: {
    async getTrips() {
      try {
        const userId = localStorage.getItem("userId");
        const trips = await getTrips(userId);
        const sortedTrips = sortTrips(trips);
        const tripsTransformed = transformTrips(sortedTrips);

        //const sortedTrips = sortTrips(tripsTransformed);

        this.trips = tripsTransformed;
      } catch (e) {
        console.log(e);
        // Add error handling later 
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.trips-container {
  width: 100%;
  padding-top: 15px;
  padding-bottom: 15px;
}
#add-trip {
  position: fixed;
  right: 30px;
  bottom: 30px;
}
</style>
