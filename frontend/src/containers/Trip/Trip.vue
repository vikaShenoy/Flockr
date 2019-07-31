<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap
              :destinations="mapTripDestinationsToDestinations()"
              :isTripMap="true"
      />
    </div>

    <div id="undo-redo-btns">
      <UndoRedo ref="undoRedo" color="white"/>
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
  import Snackbar from "../../components/Snackbars/Snackbar";
  import {
    contiguousDestinations,
    contiguousReorderedDestinations,
    editTrip,
    getTrip,
    transformTripResponse
  } from "./TripService";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command"


  export default {
    components: {
      TripItemSidebar,
      DestinationMap,
      Snackbar,
      UndoRedo
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
            this.trip.tripDestinations = [];
            setTimeout(() => {
              this.trip.tripDestinations = tripDestinations;
            }, 0);

            return
          }
          const oldTripDestinations = [...this.trip.tripDestinations];

          // Reorder elements for new trip destinations
          const temp = {...this.trip.tripDestinations[indexes.oldIndex]};
          this.trip.tripDestinations.splice(indexes.oldIndex, 1);
          this.trip.tripDestinations.splice(indexes.newIndex, 0, temp);

          const oldTrip = {
            tripId: this.trip.tripId,
            tripName: this.trip.tripName,
            tripDestinations: oldTripDestinations
          };

          const newTrip = {
            tripId: this.trip.tripId,
            tripName: this.trip.tripName,
            tripDestinations: this.trip.tripDestinations
          };

          this.addEditTripCommand(oldTrip, newTrip);

          await editTrip(tripId, this.trip.tripName, this.trip.tripDestinations);
          this.showSuccessMessage("Successfully changed order");
        } catch (e) {
          this.showError("Could not change order");
        }
      },
      /**
       * Adds an edit trip command to the undo stack
       */
      addEditTripCommand(oldTrip, newTrip) {
        const undoCommand = async (oldTrip) => {
          await editTrip(oldTrip.tripId, oldTrip.tripName, oldTrip.tripDestinations);
          this.trip = oldTrip;
        };

        const redoCommand = async (newTrip) => {
          await editTrip(newTrip.tripId, newTrip.tripName, newTrip.tripDestinations);
          this.trip = newTrip;
        };

        const updateTripCommand = new Command(undoCommand.bind(null, oldTrip), redoCommand.bind(null, newTrip));
        this.$refs.undoRedo.addUndo(updateTripCommand);
      },
      updatedTripDestinations(tripDestinations) {
        const oldTrip = {
          tripId: this.trip.tripId,
          tripName: this.trip.tripName,
          tripDestinations: this.trip.tripDestinations
        };

        const newTrip = {
          tripId: this.trip.tripId,
          tripName: this.trip.tripName,
          tripDestinations: tripDestinations
        };

        this.addEditTripCommand(oldTrip, newTrip);
        this.$set(this.trip, "tripDestinations", tripDestinations);
      },
      /**
       * Delete a trip destination from a trip and update view.
       * Add command to the undo/redo stack.
       * @param tripDestination trip destination to delete.
       * @returns {Promise<void>}
       */
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
        const oldTripDestinations = this.trip.tripDestinations;
        try {
          const undoCommand = async () => {
            await editTrip(tripId, this.trip.tripName, oldTripDestinations);
            this.$set(this.trip, "tripDestinations", oldTripDestinations);
          };
          const redoCommand = async () => {
            await editTrip(tripId, this.trip.tripName, newTripDestinations);
            this.$set(this.trip, "tripDestinations", newTripDestinations);
          };

          const deleteDestCommand = new Command(undoCommand.bind(null), redoCommand.bind(null));
          this.$refs.undoRedo.addUndo(deleteDestCommand);

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

  #undo-redo-btns {
    position: absolute;
    right: 23px;
    margin-top: 10px;
    z-index: 1000;
  }

</style>




