<template>
  <div id="trip-list-container">
     <div v-if="trips" class="col-md-12">
       <h3 v-if="!trips.length"><v-icon>directions_walk</v-icon> No Trips Available</h3>
      <TripItem
        @handleDelete="handleDelete"
        @refreshList="refreshList"
        class="trip-card"
        v-else v-for="trip in trips"
        :key="trip.tripId"
        :trip="trip"
        :viewOnly="false"
      />
    </div>

    <div v-else>
      <v-progress-circular
        indeterminate
        color="secondary"
      />
    </div>

    <UndoRedo v-if="!viewOnly" ref="undoRedo" id="undo-redo"/>
  </div>
</template>

<script>
import { getTrips, sortTrips, transformTrips, getTripData } from "./TripListService.js";
import TripItem from "./TripItem/TripItem";
import UndoRedo from "../UndoRedo/UndoRedo";
import Command from "../UndoRedo/Command";

export default {
  components: {
      UndoRedo,
    TripItem
  },
  props: {
    userId: {
      type: Number | String,
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
      async refreshList() {
          try {
              const trips = await getTrips(this.userId);
              const sortedTrips = sortTrips(trips);
              this.trips = transformTrips(sortedTrips);
          } catch (e) {
              console.log(e);
              // Handle errors later
          }
      },
      async handleDelete(trip) {
          const undoCommand = async () => {
              console.log("Undo the delete here")
              console.log(trip)

          };

          const redoCommand = async () => {
              console.log("redo the delete here");
          };

          const deleteTripCommand = new Command(undoCommand.bind(null), redoCommand.bind(null));
          this.$refs.undoRedo.addUndo(deleteTripCommand);
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
</style>


