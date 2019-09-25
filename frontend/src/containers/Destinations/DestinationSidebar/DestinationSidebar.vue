<template>
  <v-card
          id="destination-sidebar"
          ref="destinationSidebar"
    :elevation="20"
  >

    <div id="title">
      <h2>{{ shouldShowEditor ? "Add Destination" : "Destinations"}}</h2>
      <v-btn
        flat
        color="secondary"
        id="add-destination-btn"
        @click="toggleEditor"
      >
        <v-icon>{{ shouldShowEditor ? "close" : "add" }}</v-icon>
      </v-btn>

      <div id="undo-redo">
        <UndoRedo ref="undoRedo"/>
      </div>

      <v-text-field
        label="Search destinations"
        append-icon="search"
        dark
        @input="searchCriterionUpdated"
        class="search-destinations"
        single-line
        :loading="destinationsLoading"
      />

      <v-btn-toggle v-if="!shouldShowEditor" v-model="viewOption" flat id="view-option" mandatory>
        <v-btn class="option" value="your" v-bind:class="{'not-selected': viewOption !== 'your'}">
          Your Destinations
        </v-btn>
        <v-btn flat class="option" value="public" v-bind:class="{'not-selected': viewOption !== 'public'}">
          Public Destinations
        </v-btn>
      </v-btn-toggle>

    </div>

    <div id="destinations-list" ref="destinationsList" @scroll.passive="handleScrolling">
      <div v-if="shouldShowEditor">
        <AddDestinationSidebar
          v-on:addNewDestination="addNewDestination"
          :latitude="latitude"
          :longitude="longitude"
        ></AddDestinationSidebar>
      </div>

      <div v-else-if="shouldShowSpinner" id="spinner">
        <v-progress-circular
          indeterminate
          color="secondary"
          style="align-self: center;"
        />
      </div>

      <DestinationSummary
        v-for="destination in getDestinationsList"
        v-bind:key="destination.destinationId"
        :destination="destination"
        @showDeleteDestination="showDeleteDestination"
      />
    </div>

    <PromptDialog
            :dialog="isShowingDeleteDestDialog"
            message="Are you sure you want to delete the destination?"
            :onConfirm="deleteDestination"
            v-on:promptEnded="isShowingDeleteDestDialog = false"
    />

    <AlertDialog
            title="Cannot delete destination"
            :dialog.sync="cannotDeleteDestDialog"
    >
      The destination that you are trying to delete is in the following trips:

      <ul>
        <li v-for="usedTrip in usedTrips" v-bind:key="usedTrip.tripId">{{ usedTrip.tripName }}</li>
      </ul>

    </AlertDialog>
  </v-card>
</template>

<script>
  import DestinationSummary from "./DestinationSummary/DestinationSummary";
  import PromptDialog from "../../../components/PromptDialog/PromptDialog";
  import {deleteDestination, getUserTrips, undoDeleteDestination} from "./DestinationSidebarService";
  import AlertDialog from "../../../components/AlertDialog/AlertDialog";
  import UndoRedo from "../../../components/UndoRedo/UndoRedo";
  import Command from '../../../components/UndoRedo/Command';
  import AddDestinationSidebar from './AddDestinationSidebar/AddDestinationSidebar';

  export default {
    props: {
      yourDestinations: null,
      publicDestinations: null,
      latitude: null,
      longitude: null,
      destinationsLoading: Boolean // used to show if there is a pending API call to load destinations
    },
    components: {
      AddDestinationSidebar,
      DestinationSummary,
      PromptDialog,
      AlertDialog,
      UndoRedo,
    },
    data() {
      return {
        viewOption: "your",
        isShowingDeleteDestDialog: false,
        shouldShowEditor: false,
        trips: [],
        cannotDeleteDestDialog: false,
        currentDeletingDestinationId: null,
        usedTrips: [],
        destinationSearchWatchdog: null // used for debouncing
      };
    },
    mounted() {
      this.getUserTrips();
      this.resetCoordinates();
    },
    methods: {
      /**
       * Called when the destinationsList is scrolled
       */
      handleScrolling() {
        const { destinationsList } = this.$refs;
        const { scrollHeight, scrollTop, clientHeight } = destinationsList;
        const nearBottom = scrollHeight - scrollTop === clientHeight;
        if (nearBottom) {
          this.$emit('get-more-public-destinations');
        }
      },
      /**
       * Emitted when someone types in the input for searching destinations
       */
      searchCriterionUpdated(newValue) {
        clearTimeout(this.destinationSearchWatchdog);
        this.destinationSearchWatchdog = setTimeout(() => this.$emit('search-criterion-updated', newValue), 400);
      },
      /**
       * Resets the coordinates to nothing
       */
      resetCoordinates() {
        this.latitude = null;
        this.longitude = null;
      },
      /**
       * Get a user's trips.
       */
      async getUserTrips() {
        this.trips = await getUserTrips();
      },
      /**
       * Toggles the editor and resets the latitude and longitude coordinates
       */
      toggleEditor() {
        this.resetCoordinates();
        this.shouldShowEditor = !this.shouldShowEditor;
      },
      /**
       * Emits an add new destination function
       */
      addNewDestination(destination) {
        this.$emit('addNewDestination', destination);
        this.toggleEditor();
      },
      /**
       * Delete a destination. Refresh the destination list to remove it. Add the undo/redo commands to the stack.
       */
      async deleteDestination() {
        const undoCommand = async (destinationId) => {
          await undoDeleteDestination(destinationId);
          this.$emit("refreshDestinations", destinationId);
        };

        const redoCommand = async (destinationId) => {
          await deleteDestination(this.currentDeletingDestinationId);
          this.$emit("refreshDestinations", destinationId);
        };

        const deleteDestinationCommand = new Command(undoCommand.bind(null, this.currentDeletingDestinationId),
            redoCommand.bind(this.currentDeletingDestinationId));
        this.$refs.undoRedo.addUndo(deleteDestinationCommand);
        await deleteDestination(this.currentDeletingDestinationId);
        this.$emit("refreshDestinations");
      },
      /**
       * Gets trips that are using a specific destination
       */
      getTripsUsingDestination(destinationId) {
        return this.trips.filter(trip => {
          const usedTripDestinations = trip.tripDestinations.filter(tripDestination => {
            return tripDestination.destination.destinationId === destinationId;
          });

          return usedTripDestinations.length;
        });
      },
      /**
       * Shows the trips that the destination is being used on when destination
       * is trying to be deleted
       */
      showDeleteDestination(destinationId) {
        const usedTrips = this.getTripsUsingDestination(destinationId);
        if (usedTrips.length) {
          this.usedTrips = usedTrips;
          this.cannotDeleteDestDialog = true;
        } else {
          this.isShowingDeleteDestDialog = true;
          this.currentDeletingDestinationId = destinationId;
        }
      },
      /**
       * Method to add an undo/redo command to the undo/redo stack.
       */
      addUndoRedoCommand(command) {
        this.$refs.undoRedo.addUndo(command)
      }
    },
    computed: {
      /**
       * Sets which destinations should be shown: either your destinations or the public destinations
       */
      shouldShowSpinner() {
        return this.viewOption === "your" && !this.yourDestinations || this.viewOption === "public" && !this.publicDestinations;
      },
      /*
        Retrieves the destination list corresponding to whether your or public destinations
        are selected
      */
      getDestinationsList() {
        return this.viewOption === "your" ? this.yourDestinations : this.publicDestinations;
      }
    },
    watch: {
      /**
       * Changes the view option to the given new view option and emits it to the parent component
       * @param newViewOption the new view option
       */
      viewOption(newViewOption) {
        this.$emit("viewOptionChanged", newViewOption);
      }
    }
  }

</script>


<style lang="scss" scoped>

  @import "../../../styles/_variables.scss";

  .v-progress-linear {
    .primary {
      background-color: $secondary !important;
      border-color: $secondary !important;
    }
  }

  #destination-sidebar {
    width: 315px;
    justify-self: flex-end;
    display: flex;
    flex-direction: column;
    height: calc(100vh - 60px);

    #title {
      background-color: $primary;
      color: $darker-white;
      text-align: center;
      display: flex;
      align-items: center;
      flex-direction: column;
      justify-content: space-between;
      min-height: 150px;
    }

    .search-destinations {
      color: $text-light-grey;
      padding-top: 0;


      .v-text-field.v-input--is-loading > div > .v-input__slot > .v-progress-linear > .v-progress-linear__bar > div > .v-progress-linear__bar__indeterminate.long.primary {
        background-color: $secondary;
      }

      &.v-input--is-loading {
        color: $text-light-grey !important;
        caret-color: $text-light-grey !important;
      }

      &.primary--text {
        color: $text-light-grey !important;
        caret-color: $text-light-grey !important;
      }
    }

    h2 {
      margin-top: 15px;
    }

    .v-btn-toggle {
      padding-top: 10px;
    }

    .option {
      background-color: $secondary;
      color: $darker-white;
    }

    .not-selected {
      background: none;
      background-color: none !important;
    }

    .theme--light.v-btn-toggle {
      background: none !important;
    }

    #spinner {
      justify-content: center;
      align-content: center;
      display: flex;
      height: 100%;
      flex-direction: column;
    }

    #add-destination-btn {
      position: absolute;
      margin-top: 13px;
      left: 0;
    }

    #undo-redo {
      position: absolute;
      right: 25px;
      margin-top: 17px;
    }

    #destinations-list {
      overflow: auto;
    }
  }
</style>

