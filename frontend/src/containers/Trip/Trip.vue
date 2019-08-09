<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap
        :destinations="mapTripNodesToDestinations()"
        :isTripMap="true"
      />
    </div>

    <div id="undo-redo-btns">
      <UndoRedo ref="undoRedo" color="white"/>
    </div>{

    <TripItemSidebar
      :trip="trip"
      @destinationOrderChanged="destinationOrderChanged"
      @updatedTripDestinations="updatedTripDestinations"
      @deleteTripDestination="deleteTripDestination"
      @newUsers="newUsers"
      @toggleShowTripNodes="toggleShowTripNodes"
    />

    <Snackbar
      :snackbarModel="snackbarModel"
      @dismissSnackbar="snackbarModel.show = false"
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
    transformTripResponse,
    mapTripNodesToDestinations
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
        // trip: null,
        trip: {
          name: "My trip",
          users: [],
          nodeType: "TripComposite",
          tripNodes: [
            {
              tripNodeId: 1,
              name: "My favourite nested sub trip",
              nodeType: "TripComposite",
              arrivalDate: "03-04-2018",
              arrivalTime: "13:00",
              departureDate: "03-05-2018",
              departureTime: "14:00",
              isShowing: false,
              tripNodes: [
                {
                  tripNodeId: 2,
                  nodeType: "TripDestinationLeaf",
                  name: "New Zealand",
                  arrivalDate: "03-04-2018",
                  arrivalTime: "13:00",
                  departureDate: "04-04-2018",
                  departureTime: "13:00",
                  destination: {
                    destinationId: 1,
                    destinationLat: 34,
                    destinationLon: 31
                  }
                },
                {
                  tripNodeId: 3,
                  nodeType: "TripDestinationLeaf",
                  name: "Some place",
                  arrivalDate: "04-04-2018",
                  arrivalTime: "13:00",
                  departureDate: "3-05-2018",
                  departureTime: "14:00",
                  tripNodes: [],
                  destination: {
                    destinationId: 2,
                    destinationLat: 69,
                    destinationLon: 34
                  }
                }
              ]
            },
            {
              tripNodeId: 4,
              nodeType: "TripDestinationLeaf",
              name: "Some other place",
              arrivalDate: "04-06-2018",
              arrivalTime: "13:00",
              departureDate: "3-09-2018",
              departureTime: "14:00",
              destination: {
                destinationId: 3,
                destinationLat: 59,
                destinationLon: 36
              }
            }
          ]
        },
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
      // this.getTrip();
      console.log(mapTripNodesToDestinations(this.trip));
   },
    methods: {
      /**
       * Shows an snackbar error
       * @param {string} errorMessage errorMessage to show to user
       */
      showError(errorMessage) {
        this.snackbarModel.text = errorMessage;
        this.snackbarModel.color = "error";
        this.snackbarModel.show = true;
      },
      /**
       * Shows a success snackbar
       * @param {string} successMessage Success message to show
       */
      showSuccessMessage(successMessage) {
        this.snackbarModel.text = successMessage;
        this.snackbarModel.color = "success";
        this.snackbarModel.show = true;
      },
      /**
       * Maps tripDestinations to destinations for map
       */
      mapTripNodesToDestinations() {
        if (!this.trip) {
          return [];
        }

        return mapTripNodesToDestinations(this.trip);
      },
      /**
       * Gets trip by it's ID
       */
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
       * Emit event called when new users have been updated
       */
      async newUsers(users) {
        const undoCommand = async (oldUsers) => {
          await editTrip(this.trip.tripId, this.trip.tripName, this.trip.tripDestinations, oldUsers);
          this.getTrip();
        } 

        const redoCommand = async (users) => {
          await editTrip(this.trip.tripId, this.trip.tripName, this.trip.tripDestinations, users);
          this.getTrip();
        }

        const command = new Command(undoCommand.bind(null, [...this.trip.users]), redoCommand.bind(null, users));
        this.$refs.undoRedo.addUndo(command);

        this.getTrip();
        this.showSuccessMessage("Successfully updated users");
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
            tripDestinations: oldTripDestinations,
            users: this.trip.users
          };

          const newTrip = {
            tripId: this.trip.tripId,
            tripName: this.trip.tripName,
            tripDestinations: this.trip.tripDestinations,
            users: this.trip.users
          };

          this.addEditTripCommand(oldTrip, newTrip);

          await editTrip(tripId, this.trip.tripName, this.trip.tripDestinations, this.trip.users);
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
          console.log(oldTrip);
          await editTrip(oldTrip.tripId, oldTrip.tripName, oldTrip.tripDestinations, oldTrip.users);
          this.trip = oldTrip;
        };

        const redoCommand = async (newTrip) => {
          await editTrip(newTrip.tripId, newTrip.tripName, newTrip.tripDestinations, oldTrip.users);
          this.trip = newTrip;
        };

        const updateTripCommand = new Command(undoCommand.bind(null, oldTrip), redoCommand.bind(null, newTrip));
        this.$refs.undoRedo.addUndo(updateTripCommand);
      },
      updatedTripDestinations(tripDestinations) {
        const oldTrip = {
          tripId: this.trip.tripId,
          tripName: this.trip.tripName,
          tripDestinations: this.trip.tripDestinations,
          users: this.trip.users
        };

        const newTrip = {
          tripId: this.trip.tripId,
          tripName: this.trip.tripName,
          tripDestinations: tripDestinations,
          users: this.trip.users
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
            await editTrip(tripId, this.trip.tripName, oldTripDestinations, this.trip.users);
            this.$set(this.trip, "tripDestinations", oldTripDestinations);
          };
          const redoCommand = async () => {
            await editTrip(tripId, this.trip.tripName, newTripDestinations, this.trip.users);
            this.$set(this.trip, "tripDestinations", newTripDestinations);
          };

          const deleteDestCommand = new Command(undoCommand.bind(null), redoCommand.bind(null));
          this.$refs.undoRedo.addUndo(deleteDestCommand);

          await editTrip(tripId, this.trip.tripName, newTripDestinations, this.trip.users);
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




