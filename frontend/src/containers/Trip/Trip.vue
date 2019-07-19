<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap 
        :destinations="mapTripDestinationsToDestinations()"
      />
    </div>

    <TripItemSidebar 
      :trip="trip"      
      v-on:destinationOrderChanged="destinationOrderChanged"
      v-on:updatedTripDestinations="updatedTripDestinations"
      v-on:deleteTripDestination="deleteTripDestination"
    />

    <Snackbar
      :snackbarModel="snackbarModel"
      v-on:dismissSnackbar="snackbarModel.show = false"
    />
  </div>
</template>

<script>
import TripItemSidebar from "./TripItemSidebar/TripItemSidebar.vue";
import DestinationMap from "../../components/DestinationMap/DestinationMap";
import { getYourDestinations, getPublicDestinations } from "../Destinations/DestinationsService";
import Snackbar from "../../components/Snackbars/Snackbar";
import { getTrip, transformTripResponse, contiguousDestinations, editTrip, contiguousReorderedDestinations } from "./TripService";


export default {
  components: {
    TripItemSidebar,
    DestinationMap,
    Snackbar
  },
  data() {
    return {
      trip: null,

      snackbarModel: {
        show: false,
        timeout: 3000,
        text: "",
        color: "",
        snackbarId: 0
      }
    };
  },
  mounted() {
    this.getTrip();
  },
  methods: {
    showError(errorMessage) {
      this.snackbarModel.text = errorMessage;
      this.snackbarModel.color = "error";
      this.snackbarModel.show = true;
    },
    showSuccessMessage(successMessage) {
      this.snackbarModel.text = successMessage;
      this.snackbarModel.color = "success";
      this.snackbarModel.show = true;
    },
    mapTripDestinationsToDestinations() {
      if (!this.trip) {
        return [];
      } 
      return this.trip.tripDestinations.map((tripDest) => {
        return tripDest.destination;
      })
    },
    async getTrip() {
      try {
        const tripId = this.$route.params.tripId;
        const rawTrip = await getTrip(tripId);
        const trip = transformTripResponse(rawTrip);
        this.trip = trip;
      } catch (e) {
        console.log(e);
        this.showError("Could not get trip");
      }
    },
    /**
     * Changes order of destination
     */
    async destinationOrderChanged(indexes) {
      try {
        const tripId = this.$route.params.tripId;

        if (contiguousReorderedDestinations(this.trip.tripDestinations, indexes.newIndex, indexes.oldIndex)) {
          this.showError("Cannot have contiguous destinations");
          const tripDestinations = [...this.trip.tripDestinations];
          this.$set(this.trip, "tripDestinations", tripDestinations);
          return
        }

        // Reorder elements
        [this.trip.tripDestinations[indexes.newIndex], this.trip.tripDestinations[indexes.oldIndex]] = [this.trip.tripDestinations[indexes.oldIndex], this.trip.tripDestinations[indexes.newIndex]];

        await editTrip(tripId, this.trip.tripName, this.trip.tripDestinations);
        this.showSuccessMessage("Successfully changed order");
      } catch (e) {
        console.log(e);
        this.showError("Could not changed order");
      }
    },
    updatedTripDestinations(tripDestinations) {
      this.$set(this.trip, "tripDestinations", tripDestinations);
    },
    async deleteTripDestination(tripDestination) {
      if (this.trip.tripDestinations.length === 2) {
        this.showError("You cannot have less then 2 destinations");
        return;
      }
      const newTripDestinations = [...this.trip.tripDestinations].filter(currentTripDestination => {
        return tripDestination.tripDestinationId !== currentTripDestination.tripDestinationId;
      });
      if (contiguousDestinations(newTripDestinations)) {
        this.showError("Deleting this destination results in contiguous destinations");
        return;
      }

      const tripId = this.$route.params.tripId;

      try {
        await editTrip(tripId, this.trip.tripName, newTripDestinations);
        this.$set(this.trip, "tripDestinations", newTripDestinations);
        this.showSuccessMessage("Removed destination from trip");
      } catch (e) {
        this.showError("Could not remove destination from trip");        
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  #destinations {
    width: 100%;
  }

  #map {
    width: calc(100% - 555px);
    display: inline-block;
    height: 100%;
    position: fixed;
  }

</style>




