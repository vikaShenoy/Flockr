<template>
  <div id="trip">
    <div id="map">
      <DestinationMap
        :destinations="mapTripNodesToDestinations()"
        :isTripMap="true"
        :panOn="panOn"
      />

    <ConnectedUsers v-if="trip" :users="trip.users" :connectedUsers="connectedUsers" />
    </div>

    <div id="undo-redo-btns">
      <UndoRedo
        ref="undoRedo"
        color="white"
      />
    </div>

    <TripItemSidebar
      :trip="trip"
      @tripNodeOrderChanged="tripNodeOrderChanged"
      @toggleExpanded="toggleExpandedTrips"
      @tripNodesUpdated="tripNodesUpdated"
      @deleteTripNode="deleteTripNode"
      @newUsers="newUsers"
      @newTripAdded="newTripAdded"
      @getNewTrip="getTrip()"
      @setPan="setPan"
      @addEditTripCommand="tripNameChanged"
      @nestedTripNameUpdated="nestedTripNameChanged"
    />
  </div>
</template>

<script>
  import TripItemSidebar from "./TripItemSidebar/TripItemSidebar.vue";
  import DestinationMap from "../../components/DestinationMap/DestinationMap";
  import ConnectedUsers from "./ConnectedUsers/ConnectedUsers";

  import {
    contiguousDestinations,
    contiguousReorderedDestinations,
    editTrip,
    getTrip,
    mapTripNodesToDestinations,
    getTripNodeById,
    transformTripNode,
    getTripNodeParentById,
    tripNodeHasContiguousDestinations,
    tripNodesLessThan2,
  } from "./TripService";

  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command";
  import UserStore from '../../stores/UserStore';
  import { restoreTrip, deleteTripFromList } from '../Trips/OldTripsService';

  export default {
    components: {
      TripItemSidebar,
      DestinationMap,
      UndoRedo,
      ConnectedUsers
    },
    data() {
      return {
        trip: null,
        panOn: false,
        connectedUsers: []
      };
    },
    mounted() {
      this.getTrip();
      this.listenOnMessage();

    },
    methods: {
      /**
         * Called when the trip name is edited.
         * Sets an edit command for undo redo and updates the trip name.
         */
        tripNameChanged(oldTrip, newTrip, newName) {
          this.addEditTripCommand(oldTrip, newTrip);
          this.trip.name = newName;
        },
        
        async nestedTripNameChanged(tripNode, newName) {
          const oldTrip = {...tripNode};
          tripNode.name = newName;
          
          editTrip(tripNode); 

          this.addEditTripCommand(oldTrip, tripNode);
        },
        newTripAdded(subTrip) {
          const oldTrip = {...this.trip, tripNodes: [...this.trip.tripNodes]};
          this.trip.tripNodes.push(subTrip)
          editTrip(this.trip);

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

        const addTripCommand = new Command(undoCommand.bind(null, subTrip, oldTrip),
            redoCommand.bind(null, subTrip, this.trip));
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
       * Sets the pan on to the emitted value.
       * True if the pan is on, false if the pan is off.
       */
      setPan(panOn) {
        this.panOn = panOn;
      },
      /**
       * Listens websockets events to either update trip, connected users
       */
      listenOnMessage() {
        UserStore.data.socket.addEventListener("message", (event) => {
          const message = JSON.parse(event.data);
          if (message.type === "connected")  {
            this.connectedUsers.push(message.user);
            this.showSuccessMessage(`${message.user.firstName} connected`, 1000);

          } else if (message.type === "disconnected") {
            this.showSuccessMessage(`${message.user.firstName} disconnected`, 1000);
            this.connectedUsers = this.connectedUsers.filter(user => {
              return user.userId !== message.user.userId;
            });
          } else if (message.type === "tripUpdated") {
            this.handleTripUpdate(message.trip);
          }
        });
      },
      handleTripUpdate(rawTrip) {
        const transformedTrip = transformTripNode(rawTrip);
        if (transformedTrip.tripNodeId === this.trip.tripNodeId) {
          // Need to know what trip node is the parent
          this.trip = transformedTrip;
          this.trip.isSubTrip = false;
        } else {
          const parentTripNode = getTripNodeParentById(transformedTrip.tripNodeId, this.trip);
          for (let i = 0; i < parentTripNode.tripNodes.length; i++) {
            if (parentTripNode.tripNodes[i].tripNodeId === transformedTrip.tripNodeId) {
              transformedTrip.isShowing = parentTripNode.tripNodes[i].isShowing;
              this.$set(parentTripNode.tripNodes, i, transformedTrip);
            }
          }
        }
      },
      /**
       * Shows an snackbar error
       * @param {string} errorMessage errorMessage to show to user
       */
      showError(errorMessage) {
        this.$root.$emit("show-snackbar", {
          message: errorMessage,
          color: "error",
          timeout: 3000
        });
      },
      /**
       * Shows a success snackbar
       * @param {string} successMessage Success message to show
       */
      showSuccessMessage(successMessage, timeout) {
        this.$root.$emit("show-snackbar", {
          message: successMessage,
          color: "success",
          timeout: timeout ? timeout : 2000
        });
      },
      /**
       * Maps tripDestinations to destinations for map
       */
      mapTripNodesToDestinations() {
        if (!this.trip) {
          return [];
        }

        mapTripNodesToDestinations(this.trip);
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
          this.connectedUsers = trip.connectedUsers;

          // Important override used for drag n drop
          trip.isSubTrip = false;
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

        const command = new Command(undoCommand.bind(null, [...this.trip.users]),
            redoCommand.bind(null, users));
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
        const {oldParentTripNodeId, newParentTripNodeId, oldIndex, newIndex} = indices;
        const oldParentTripNode = {...getTripNodeById(oldParentTripNodeId, this.trip)};
        // Copy children nodes as well
        oldParentTripNode.tripNodes = [...oldParentTripNode.tripNodes];
        const sourceTripNodeCopy = {
          ...oldParentTripNode,
          tripNodes: [...oldParentTripNode.tripNodes]
        };
        let targetTripNodeCopy = null;
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
          newParentTripNode.tripNodes = [...newParentTripNode.tripNodes];
          targetTripNodeCopy = {...newParentTripNode, tripNodes: [...newParentTripNode.tripNodes]};

          const temp = {...oldParentTripNode.tripNodes[oldIndex]};
          oldParentTripNode.tripNodes.splice(oldIndex, 1);
          newParentTripNode.tripNodes.splice(newIndex, 0, temp);
        }

        return {
          reorderedSourceTripNode: oldParentTripNode,
          stayedInSourceTripNode: stayedInSourceTripNode,
          reorderedTargetTripNode: newParentTripNode,
          sourceTripNodeCopy,
          targetTripNodeCopy  
        }

      },
      /**
       * Reorders the actual trip object to make the model consistent with the view
       * @param {Object} indices Specifies the indices to swap with
       */
      reorderNodes(indices) {
        const {oldParentTripNodeId, newParentTripNodeId, oldIndex, newIndex} = indices;
        const oldParentTripNode = getTripNodeById(oldParentTripNodeId, this.trip);
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
          newParentTripNode = getTripNodeById(newParentTripNodeId, this.trip);
          const temp = {...oldParentTripNode.tripNodes[oldIndex]};
          oldParentTripNode.tripNodes.splice(oldIndex, 1);
          newParentTripNode.tripNodes.splice(newIndex, 0, temp);
      }
      },
      /**
       * Sends edited trips to backend
       * @param {Object} reorderedCopiedNodes the copies of the reordered nodes, and whether the node remained in
       * its original parent node
       */
      async reorderTripsInServer(reorderedCopiedNodes) {
        const {reorderedSourceTripNode, reorderedTargetTripNode, stayedInSourceTripNode, sourceTripNodeCopy, targetTripNodeCopy} = reorderedCopiedNodes;
        if (stayedInSourceTripNode) {
          // we only need to do one edit

          await editTrip(reorderedSourceTripNode);
          // Drag and drop for one level of editing
          this.addEditTripCommand(
              sourceTripNodeCopy,
              reorderedSourceTripNode
          );
        } else {
          // If parent trip nodes are different, we need to edit two trips
          const editTripPromises = [
            editTrip(reorderedSourceTripNode),
            editTrip(reorderedTargetTripNode)
          ];

          await Promise.all(editTripPromises);

          // Edits both source and target trips to their old value
          const undoCommand = async (oldSourceTripNode, oldTargetTripNode) => {
            const tripsToEdit = [editTrip(oldSourceTripNode), editTrip(oldTargetTripNode)];
            await Promise.all(tripsToEdit);
            this.getTrip();
          };

          // Edits both the source and target trips to their new value
          const redoCommand = async (newSourceTripNode, newTargetSourceNode) => {
            const tripsToEdit = [editTrip(newSourceTripNode), editTrip(newSourceTripNode)];
            await Promise.all(tripsToEdit);
            this.getTrip();
          };

          const tripReorderCommand = new Command(
              undoCommand.bind(null, sourceTripNodeCopy, targetTripNodeCopy),
              redoCommand.bind(null, reorderedSourceTripNode, reorderedTargetTripNode));
          this.$refs.undoRedo.addUndo(tripReorderCommand);
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
            const trip = this.trip;
            this.trip = null;
            setTimeout(() => {
              this.trip = trip;
            }, 0);

          } else if (tripNodesLessThan2(reorderedCopiedNodes)) {
            this.showError("Cannot have less than 2 trip nodes");
            const trip = this.trip;
            this.trip = null;
            setTimeout(() => {
              this.trip = trip;
            }, 0);
          } else {
            this.reorderTripsInServer(reorderedCopiedNodes);
            this.reorderNodes(indexes);
            this.showSuccessMessage("Successfully changed order");
          }
        } catch (e) {
          console.log(e);
          this.showError("Could not change order");
        }
      },
      /**
       * Adds an edit trip command to the undo stack
       *
       * @param {Object} oldTrip The oldTrip to go back to when pressing undo
       * @param {Object} newTrip The new trip to redo to when pressing redo
       */
      addEditTripCommand(oldTrip, newTrip) {
        const undoCommand = async (oldTrip) => {
          await editTrip(oldTrip);
          this.getTrip();
        };

        const redoCommand = async (newTrip) => {
          await editTrip(newTrip);
          this.getTrip();
        };

        const updateTripCommand = new Command(undoCommand.bind(null, oldTrip),
            redoCommand.bind(null, newTrip));
        this.$refs.undoRedo.addUndo(updateTripCommand);
      },
      tripNodesUpdated(parentTripNode, tripNodes) {
        const oldTrip = {...parentTripNode, tripNodes: [...parentTripNode.tripNodes]};
        parentTripNode.tripNodes = tripNodes;
        this.addEditTripCommand(oldTrip, parentTripNode);
      },
      /**
       * Delete a trip node from a trip and update view
       * Add command to the undo/redo stack.
       * @param tripNode tripNode to delete.
       * @returns {Promise<void>}
       */
      async deleteTripNode(tripNode) {
        const parentTripNode = getTripNodeParentById(tripNode.tripNodeId, this.trip);
        if (parentTripNode.tripNodes.length === 2) {
          this.showError("You cannot have less then 2 trip nodes");
          return;
        }
        const filteredTripNodes = parentTripNode.tripNodes.filter(currentTripNode => {
          return tripNode.tripNodeId !== currentTripNode.tripNodeId;
        });

        if (tripNodeHasContiguousDestinations({...parentTripNode, tripNodes: filteredTripNodes})) {
          this.showError("Deleting this destination results in contiguous destinations");
          return;
        }

        const oldParentTripNode = {...parentTripNode, tripNodes: [...parentTripNode.tripNodes]};
        parentTripNode.tripNodes = filteredTripNodes;
        const newParentTripNode = {...parentTripNode, tripNodes: [...parentTripNode.tripNodes]};

        try {
          const undoCommand = async (oldParentTripNode) => {
            await editTrip(oldParentTripNode);
            parentTripNode.tripNodes = oldParentTripNode.tripNodes;
          };
          const redoCommand = async (newParentTripNode) => {
            await editTrip(newParentTripNode);
            parentTripNode.tripNodes = newParentTripNode.tripNodes;
          };

          const deleteDestCommand = new Command(undoCommand.bind(null, oldParentTripNode),
              redoCommand.bind(null, newParentTripNode));
          this.$refs.undoRedo.addUndo(deleteDestCommand);

          await editTrip(parentTripNode);
          this.showSuccessMessage("Removed destination from trip");

        } catch (e) {
          this.showError("Could not remove destination from trip");
        }
      }
    },
    watch: {
      /**
       * Watch for rerouting to the same page, if so, get new trip contents
       */
      $route() {
        this.trip = null;
        this.getTrip();
        this.$refs.undoRedo.clearStack();
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
    position: relative;
  }
}

  #undo-redo-btns {
    position: absolute;
    right: 23px;
    margin-top: 10px;
    z-index: 1000;
  }
</style>




