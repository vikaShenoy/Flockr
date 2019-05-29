<template>
  <div id="trip-list-container">
     <div v-if="trips" class="col-md-12">
       <h3 v-if="!trips.length"><v-icon>directions_walk</v-icon> No Trips Available</h3>
      <TripItem class="trip-card" v-else v-for="trip in trips" v-bind:key="trip.tripId" :trip="trip"/>
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
    justify-content: center;
  }

  .trip-card {
    width: 150%;
    justify-content: center;
    margin-left: 10px;
    }
</style>


