<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap 
        :destinations="[]"
      />
    </div>

    <TripItemSidebar 
      :trip="trip"      
      v-on:destinationOrderChanged="destinationOrderChanged"
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
import { getTrip, transformTrip } from "./TripService";
import { getYourDestinations, getPublicDestinations } from "../Destinations/DestinationsService";
import Snackbar from "../../components/Snackbars/Snackbar";
import { editTrip, transformTripResponse } from "../EditTrip/EditTripService";

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
    async getTrip() {
      try {
        const tripId = this.$route.params.tripId;
        const trip = await getTrip(tripId);
        this.trip = trip;
      } catch (e) {
        this.showError("Could not get trip");
      }
    },
    /**
     * Changes order of destination
     */
    async destinationOrderChanged(indexes) {
      try {
        const tripId = this.$route.params.tripId;
        await editTrip(tripId, this.trip.tripName, this.trip.tripDestinations);
        const movedRow = this.trip.tripDestinations.splice(indexes.oldIndex, 1)[0];
        this.trip.tripDestinations.splice(indexes.newIndex, 0, movedRow);
        this.showSuccessMessage("Successfully changed order");
      } catch (e) {
        this.showError("Could not changed order");
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
    width: calc(100% - 300px);
    display: inline-block;
    height: 100%;
    position: fixed;
  }

</style>




