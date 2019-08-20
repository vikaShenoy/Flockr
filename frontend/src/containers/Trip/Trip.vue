<template>
  <div id="trip">
    <div id="map">
      <DestinationMap
              :destinations="mapTripDestinationsToDestinations()"
              :isTripMap="true"
              :panOn="panOn"
      />

    <ConnectedUsers v-if="trip" :users="trip.users" :connectedUsers="trip.connectedUsers" />
    </div>

    <div id="undo-redo-btns">
      <UndoRedo ref="undoRedo" color="white"/>
    </div>

    <TripItemSidebar
            :trip="trip"
            v-on:destinationOrderChanged="destinationOrderChanged"
            v-on:updatedTripDestinations="updatedTripDestinations"
            v-on:deleteTripDestination="deleteTripDestination"
            v-on:setPan="setPan"
            @newUsers="newUsers"
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
    transformTripResponse
  } from "./TripService";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command"
import UserStore from '../../stores/UserStore';


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
        panOn: false
      };
    },
    mounted() {
      this.getTrip();
      this.listenOnMessage();
      
    },
    methods: {
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
            this.trip.connectedUsers.push(message.user);
            this.showSuccessMessage(`${message.user.firstName} connected`, 1000);
            
          } else if (message.type === "disconnected") {
            this.showSuccessMessage(`${message.user.firstName} disconnected`, 1000);
            this.trip.connectedUsers = this.trip.connectedUsers.filter(user => {
              return user.userId !== message.user.userId;
            });
          }
        });
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
      mapTripDestinationsToDestinations() {
        if (!this.trip) {
          return [];
        }
        return this.trip.tripDestinations.map((tripDest) => {
          return tripDest.destination;
        })
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
    display: flex;
  }

  #map {
    flex: 1;
    position: relative;
  }

  #undo-redo-btns {
    position: absolute;
    right: 23px;
    margin-top: 10px;
    z-index: 1000;
  }

</style>




