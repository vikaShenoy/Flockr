<template>
  <div id="trip-list-container">
     <div v-if="trips" class="col-md-8">
       <h3 v-if="!trips.length"><v-icon>directions_walk</v-icon> No Trips Available</h3>
      <TripItem v-else v-for="trip in trips" v-bind:key="trip.tripId" :trip="trip"/>
    </div>

    <div v-else>
      <v-progress-circular
        indeterminate
        color="secondary"
      ></v-progress-circular>
    </div>
    </div>
</template>

<script>
import { getTrips, sortTrips, transformTrips } from "./TripListService.js";
import TripItem from "./TripItem/TripItem";

export default {
  components: {
    TripItem
  },
  props: ["userId"],
  data() {
    return {
      trips: null
    };
  },
  async mounted() {
    try {
      const trips = await getTrips(this.userId);
      const sortedTrips = sortTrips(trips);
      console.log(sortedTrips);
      this.trips = transformTrips(sortedTrips);
    } catch (e) {
      console.log(e);
      // Handle errors later
    }
  }
}
</script>

<style lang="scss" scoped>
  #trip-list-container {
    width: 100%;
    display: flex; 
    justify-content: center;
  }
</style>


