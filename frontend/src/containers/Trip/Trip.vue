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
			@toggleExpanded="toggleExpandedTrips"
      @updatedTripDestinations="updatedTripDestinations"
      @deleteTripDestination="deleteTripDestination"
      @newUsers="newUsers"
			@newTripAdded="newTripAdded"
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
    mapTripNodesToDestinations,
		getTripNodeById,
    transformTripNode,
  } from "./TripService";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command";

	import { restoreTrip, deleteTripFromList } from '../Trips/OldTripsService';

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
      newTripAdded(subTrip, oldParentTrip, newParentTrip) {
        const undoCommand = async (subTrip, oldParentTrip) => {
          await deleteTripFromList(subTrip.tripNodeId);
          await editTrip(oldParentTrip);
          this.getTrip();
				};

				const redoCommand = async (subTrip, newParentTrip) => {
          await restoreTrip(subTrip.tripNodeId);
          await editTrip(newParentTrip);
          this.getTrip();
				};

				const addTripCommand = new Command(undoCommand.bind(null, subTrip, oldParentTrip),
						redoCommand.bind(null, subTrip, newParentTrip));
				this.$refs.undoRedo.addUndo(addTripCommand);
			},

      /**
			 * Open and close a trip composite to show its tripNodes.
			 * @tripNodeId tripNode to be toggled.
			 * */
      toggleExpandedTrips(tripNodeId) {
        const tripNode = getTripNodeById(tripNodeId, this.trip);
        tripNode.isShowing = this.$set(tripNode, "isShowing", !tripNode.isShowing);
			},
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

        console.log("The trip in question is: ");
        console.log(this.trip);
        const destinations = mapTripNodesToDestinations(this.trip);
        return mapTripNodesToDestinations(this.trip);
      },
      /**
       * Gets trip by it's ID
       */
      async getTrip() {
        try {
          const tripId = this.$route.params.tripId;
          const rawTrip = await getTrip(tripId);
          const trip = transformTripNode(rawTrip);
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
        };

        const redoCommand = async (users) => {
          await editTrip(this.trip.tripId, this.trip.tripName, this.trip.tripDestinations, users);
          this.getTrip();
        };

        const command = new Command(undoCommand.bind(null, [...this.trip.users]), redoCommand.bind(null, users));
        this.$refs.undoRedo.addUndo(command);

        this.getTrip();
        this.showSuccessMessage("Successfully updated users");
      },
      /**
       * Reorder **copies** of the changed trip nodes (only the source and destination nodes)
       * @param {Object} indices the indices of what to order
       * @returns {Object} contains edited tripNodes, and whether the moved node remained
       * in the same parent node
       */
      getReorderedCopiedNodes(indices) {
        const { oldParentTripNodeId, newParentTripNodeId, oldIndex, newIndex } = indices;
        console.log("I made it one");
        const oldParentTripNode = {...getTripNodeById(oldParentTripNodeId, this.trip)};
        console.log("I made it two");
        const oldParentTripNodes = [...oldParentTripNode.tripNodes];
        console.log("I madeit three");
        let newParentTripNode = null;
        const stayedInSourceTripNode = oldParentTripNodeId === newParentTripNodeId;

        // If indexes of parent trip nodes are the same, we only need to do one edit
        if (stayedInSourceTripNode) {
          const temp = {
            ...oldParentTripNode.tripNodes[oldIndex]
          };
          oldParentTripNode.tripNodes.splice(oldIndex, 1);
          oldParentTripNode.tripNodes.splice(newIndex, 0, temp);
        } else {
          // If parent trip nodes are different, we need to edit two trips
          newParentTripNode = {...getTripNodeById(newParentTripNodeId, this.trip)};
          const newParentTripNodes = [...newParentTripNode.tripNodes];

          const temp = {...oldParentTripNode.tripNodes[oldIndex]};
          oldParentTripNode.tripNodes.splice(oldIndex, 1);
          newParentTripNode.tripNodes.splice(newIndex, 0, temp);
        }

        return {
          reorderedSourceTripNode: oldParentTripNode,
          stayedInSourceTripNode: stayedInSourceTripNode,
          reorderedTargetTripNode: newParentTripNode
        }

      },
      /**
       * Sends edited trips to backend
       * @param {Object} reorderedCopiedNodes the copies of the reordered nodes, and whether the node remained in
       * its original parent node
       */
      async reorderTripsInServer(reorderedCopiedNodes) {
          const { reorderedSourceTripNode, reorderedTargetTripNode, stayedInSourceTripNode } = reorderedCopiedNodes;
          if (stayedInSourceTripNode) {
            // we only need to do one edit

            await editTrip(reorderedSourceTripNode);

            // Drag and drop for one level of editing
            this.addEditTripCommand({
              ...this.trip,
              tripNodes: reorderedSourceTripNode.tripNodes               
            }, this.trip);
          } else {
            // If parent trip nodes are different, we need to edit two trips
            const editTripPromises = [
              editTrip(reorderedSourceTripNode),
              editTrip(reorderedTargetTripNode)
            ];

            // Drag and drop for trip node being dragged
            this.addEditTripCommand({
              ...this.trip,
              tripNodes: reorderedSourceTripNode.tripNodes
            }, this.trip);

            // Drag and drop for trip node being dragged into
            // Drag and drop for trip node being dragged
            this.addEditTripCommand({
              ...this.trip,
              tripNodes: reorderedTargetTripNode.tripNodes
            }, this.trip);

            await Promise.all(editTripPromises);
					}
      },
      /**
       * Changes order of destination
       */
      async tripNodeOrderChanged(indexes) {
        try {
          const reorderedCopiedNodes = this.getReorderedCopiedNodes(indexes);
          if (contiguousReorderedDestinations(reorderedCopiedNodes)) {
            this.showError("Cannot have contiguous destinations");
            const tripNodes = [...this.trip.tripNodes];
            this.trip.tripNodes = [];
            setTimeout(() => {
              this.trip.tripNodes = tripNodes;
            }, 0);
          } else {
            this.reorderTripsInServer(reorderedCopiedNodes);
            this.showSuccessMessage("Successfully changed order");
          }
        } catch (e) {
          console.log(e);
          this.showError("Could not change order");
        }
      },
      /**
       * Adds an edit trip command to the undo stack
       * @param {Object} oldTrip The oldTrip to go back to when pressing undo
       * @param {Object} newTrip The new trip to redo to when pressing redo
       */
      addEditTripCommand(oldTrip, newTrip) {
        const undoCommand = async (oldTrip) => {
          await editTrip(oldTrip);
          this.trip = oldTrip;
        };

        const redoCommand = async (newTrip) => {
          await editTrip(newTrip);
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




