<template>
  <div id="view-container">
    <v-card id="undo-redo-card" v-if="!viewOnly">
      <UndoRedo ref="undoRedo"/>
      <p>You can undo and redo your actions in this page</p>
    </v-card>

    <div v-if="!isAddingATrip" class="trips-container" :key="tripListKey">
      <v-container grid-list-xl text-center>
        <v-layout wrap>
          <v-flex xs10 offset-xs1>
            <TripList :userId="userId" @delete-trip="deleteTrip" :viewOnly="viewOnly"/>
          </v-flex>
        </v-layout>
      </v-container>

      <v-btn
              v-if="!viewOnly"
              id="add-trip-button"
              fab
              dark
              color="secondary"
              @click="addButtonClicked"
      >
        <v-icon
                dark
        >add
        </v-icon>
      </v-btn>
    </div>

    <AddTrip
            v-else
            @newTripAdded="newTripWasAdded"
            @cancel-trip-creation="isAddingATrip = false"
    />
  </div>
</template>


<script>
  import TripList from "../../components/TripList/TripList";
  import AddTrip from "../AddTrip/AddTrip";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command";
  import {deleteTripFromList, restoreTrip} from "./OldTripsService";

  export default {
    props: {
      viewOnly: { // hide action buttons and undo redo
        type: Boolean,
        required: false
      }
    },
    components: {
      AddTrip,
      TripList,
      UndoRedo
    },
    data() {
      return {
        // Used to know what user to get trips from
        userId: localStorage.getItem("userId"),
        isAddingATrip: false,
        tripListKey: 0 // used to force rerenders of the component when trips are added
      };
    },
    methods: {
      /**
       * Called when the list of trips changes and we need the list of trips
       * to be updated.
       * Creates undo/redo commands and adds them to the stack.
       */
      newTripWasAdded(tripId) {
        this.isAddingATrip = false;
        this.refreshTrips();


        let undoCommand = async (tripId) => {
          await deleteTripFromList(tripId);
          this.refreshTrips();
        };
        undoCommand = undoCommand.bind(null, tripId);

        let redoCommand = async (tripId) => {
          await restoreTrip(tripId);
          this.refreshTrips();
        };
        redoCommand = redoCommand.bind(null, tripId);
        const addedTripCommand = new Command(undoCommand, redoCommand);
        this.$refs.undoRedo.addUndo(addedTripCommand);
      },
      /**
       * Delete a trip given its id. Create and add undo/redo commands
       * to the stack.
       */
      deleteTrip(tripId) {
        let undoCommand = async (tripId) => {
          await restoreTrip(tripId);
          this.refreshTrips();
        };
        undoCommand = undoCommand.bind(null, tripId);

        let redoCommand = async (tripId) => {
          await deleteTripFromList(tripId);
          this.refreshTrips();
        };
        redoCommand = redoCommand.bind(null, tripId);
        redoCommand(); // execute the deletion
        const deleteTripCommand = new Command(undoCommand, redoCommand);
        this.$refs.undoRedo.addUndo(deleteTripCommand);
      },
      /**
       * Force a component re render for the list of trips by changing
       * its key
       */
      refreshTrips() {
        this.tripListKey += 1;
      },

      addButtonClicked() {
        console.log("Add button clicked")
        this.isAddingATrip = true;
      }
    }
  };
</script>

<style lang="scss" scoped>
  #undo-redo-card {
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
  }

  .trips-container {
    width: 100%;
  }

  #add-trip-button {
    position: fixed;
    right: 30px;
    bottom: 30px;
  }

  #view-container {
    width: 100%;
    height: 100%;
    padding: 15px;
  }
</style>
