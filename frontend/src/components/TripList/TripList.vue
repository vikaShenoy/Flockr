<template>
  <div id="trip-list-container">
    <div v-if="trips" class="col-md-12">
      <h3 v-if="!trips.length">
        <v-icon>directions_walk</v-icon>
        No Trips Available
      </h3>

      <TripItem
        @handleDelete="handleDelete"
        @refreshList="refreshList"
        class="trip-card"
        v-else v-for="trip in trips"
        :key="trip.tripId"
        :trip="trip"
        :viewOnly="viewOnly"
      />
    </div>

    <div v-else id="loader">
      <v-progress-circular
        indeterminate
        color="secondary"
      />
    </div>
  </div>
</template>

<script>
  import {getTrips, sortTrips, transformTrips} from "./TripListService.js";
  import TripItem from "./TripItem/TripItem";

  export default {
    components: {
      TripItem
    },
    props: {
      userId: {
        type: [Number, String],
        required: true
      },
      viewOnly: {
        type: Boolean, // hides action buttons and undo redo
        required: false
      }
    },
    data() {
      return {
        trips: null
      };
    },
    async mounted() {
      this.refreshList();
    },
    methods: {
      /**
       * Called on mount and after the trip list changes (eg. deletion).
       * Updates the view to show trips.
       * @returns {Promise<void>}
       */
      async refreshList() {
        const trips = await getTrips(this.userId);
        const sortedTrips = sortTrips(trips);
        this.trips = transformTrips(sortedTrips);
      },
      handleDelete(tripId) {
        this.$emit("delete-trip", tripId);
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
    justify-content: center;
    margin-left: 10px;
  }

  #undo-redo {
    position: fixed;
    right: 1000px;
    bottom: 45px;
  }

  #loader {
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>


