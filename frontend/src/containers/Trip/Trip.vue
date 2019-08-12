<template>
  <div id="trip">
    <div id="map">
      <DestinationMap
        :destinations="mapTripNodesToDestinations()"
        :isTripMap="true"
      />
    </div>

    <div id="undo-redo-btns">
      <UndoRedo ref="undoRedo" color="white"/>
    </div>

    <TripItemSidebar
      :trip="trip"
      @tripNodeOrderChanged="tripNodeOrderChanged"
      @updatedTripDestinations="updatedTripDestinations"
      @deleteTripDestination="deleteTripDestination"
      @newUsers="newUsers"
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
    mapTripNodesToDestinations,
		getTripNodeById,
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
          tripNodeId: 5,
          name: "Trip5",
          users: [],
          nodeType: "TripComposite",
          tripNodes: [
            {
              tripNodeId: 4,
              name: "Trip4",
              nodeType: "TripComposite",
              arrivalDate: "03-04-2018",
              arrivalTime: "13:00",
              departureDate: "03-05-2018",
              departureTime: "14:00",
              isShowing: false,
              tripNodes: [
                {
                  tripNodeId: 1,
                  nodeType: "TripDestinationLeaf",
                  name: "Destination1",
                  arrivalDate: "03-04-2018",
                  arrivalTime: "13:00",
                  departureDate: "04-04-2018",
                  tripNodes: [],
                  departureTime: "13:00",
                  destination: {
                    destinationId: 1,
                    destinationLat: 34,
                    destinationLon: 31
                  }
                },
								{
                  tripNodeId: 2,
                  nodeType: "TripDestinationLeaf",
                  name: "Destination2",
                  arrivalDate: "03-04-2018",
                  arrivalTime: "13:00",
                  departureDate: "04-04-2018",
                  tripNodes: [],
                  departureTime: "13:00",
                  destination: {
                    destinationId: 2,
                    destinationLat: 34,
                    destinationLon: 31
                  }
								},
                {
                  tripNodeId: 3,
                  nodeType: "TripDestinationLeaf",
                  name: "Destination3",
                  arrivalDate: "03-04-2018",
                  arrivalTime: "13:00",
                  departureDate: "04-04-2018",
                  tripNodes: [],
                  departureTime: "13:00",
                  destination: {
                    destinationId: 3,
                    destinationLat: 34,
                    destinationLon: 31
                  }
                },

              ]
            },
            {
              tripNodeId: 4,
              nodeType: "TripDestinationLeaf",
              name: "Destination4",
              arrivalDate: "04-06-2018",
              arrivalTime: "13:00",
              departureDate: "3-09-2018",
              tripNodes: [],
              departureTime: "14:00",
              destination: {
                destinationId: 4,
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
      async tripNodeOrderChanged(indexes) {
        try {

          if (contiguousReorderedDestinations(this.trip.tripNodes, indexes)) {
            this.showError("Cannot have contiguous destinations");
            const tripNodes = [...this.trip.tripNodes];
            this.trip.tripNodes = [];
            setTimeout(() => {
              this.trip.tripNodes = tripNodes;
            }, 0);
          }

          const oldTripNodes = [...this.trip.tripNodes];

          // Reorder elements for new trip destinations

          const oldParentTripNode = getTripNodeById(indexes.oldParentTripNodeId, this.trip);
          if (indexes.oldParentTripNodeId === indexes.newParentTripNodeId) {
            const temp = {...oldParentTripNode.tripNodes[indexes.oldIndex]};
            oldParentTripNode.tripNodes.splice(indexes.oldIndex, 1);
            oldParentTripNode.tripNodes.splice(indexes.newIndex, 0, temp);
            await editTrip(oldParentTripNode);
          } else {
            const newParentTripNode = getTripNodeById(indexes.newParentTripNodeId, this.trip);
            const temp = {...oldParentTripNode.tripNodes[indexes.oldIndex]};
            oldParentTripNode.tripNodes.splice(indexes.oldIndex, 1);
            newParentTripNode.tripNodes.splice(indexes.newIndex, 0, temp);

            const editTripPromises = [
                editTrip(oldParentTripNode.tripNodeId, oldParentTripNode.name,
										oldParentTripNode.tripNodes, oldParentTripNode.users),
                editTrip(newParentTripNode.tripNodeId, newParentTripNode.name,
										newParentTripNode.tripNodes, newParentTripNode.users)];
            await Promise.all(editTripPromises);
					}

          // const oldTrip = {
          //   tripId: this.trip.tripId,
          //   tripName: this.trip.tripName,
          //   tripDestinations: oldTripNodes,
          //   users: this.trip.users
          // };
					//
          // const newTrip = {
          //   tripId: this.trip.tripId,
          //   tripName: this.trip.tripName,
          //   tripDestinations: this.trip.tripDestinations,
          //   users: this.trip.users
          // };
					//
          // this.addEditTripCommand(oldTrip, newTrip);
					//
          // this.showSuccessMessage("Successfully changed order");
        } catch (e) {
          console.log(e);
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
  #trip {
    width: 100%;
    display: flex;
    flex-direction: row;

    #map {
      flex-grow: 1;
    }
  }
  

  #undo-redo-btns {
    position: absolute;
    right: 23px;
    margin-top: 10px;
    z-index: 1000;
  }

</style>




