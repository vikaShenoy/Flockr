<template>
  <v-card
    id="destination-sidebar"
    :elevation="20"
  >
  <div id="title">
    <h2>Destinations</h2>
    <v-btn
      flat
      color="secondary"
      id="add-destination-btn"
      @click="$emit('addDestinationClicked')"
    >
      <v-icon>add</v-icon>
    </v-btn>


   <v-btn-toggle v-model="viewOption" flat id="view-option" mandatory>
      <v-btn class="option" value="your" v-bind:class="{'not-selected': viewOption !== 'your'}">
        Your Destinations
      </v-btn>
      <v-btn flat class="option" value="public" v-bind:class="{'not-selected': viewOption !== 'public'}">
        Public Destinations
      </v-btn>
   </v-btn-toggle>

  </div>

  <div id="destinations-list">
    <div v-if="shouldShowSpinner" id="spinner">
      <v-progress-circular
        indeterminate
        color="secondary"
        style="align-self: center;"
      >
      </v-progress-circular>

    </div>

    <div v-else>
      <DestinationSummary
        v-for="destination in getDestinationsList"
        v-bind:key="destination.destinationId"
        :destination="destination"
        @showDeleteDestination="showDeleteDestination"
      />
    </div>

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
    The destination that you are trying to delete is in the following trips 

    <ul>
      <li v-for="usedTrip in usedTrips" v-bind:key="usedTrip.tripId">{{ usedTrip.tripName }}</li>
    </ul>
    
  </AlertDialog>



  </v-card>
</template>

<script>
import DestinationSummary from "./DestinationSummary/DestinationSummary";
import PromptDialog from "../../../components/PromptDialog/PromptDialog";
import { getUserTrips, deleteDestination } from "./DestinationSidebarService";
import AlertDialog from "../../../components/AlertDialog/AlertDialog";

export default {
  props: ["yourDestinations", "publicDestinations"],
  components: {
    DestinationSummary,
    PromptDialog,
    AlertDialog
  },
  data() {
    return {
      viewOption: "your",
      isShowingDeleteDestDialog: false,
      trips: [],
      cannotDeleteDestDialog: false,
      currentDeletingDestinationId: null,
      usedTrips: []
    };
  },
  mounted() {
    this.getUserTrips(); 
  },
  methods: {
    /**
     * Gets a user's trips
     */
    async getUserTrips() {
      const userTrips = await getUserTrips();
      this.trips = userTrips;
    },
    // Deletes a destination
    async deleteDestination() {
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
    showDeleteDestination(destinationId) {
      const usedTrips = this.getTripsUsingDestination(destinationId);
      if (usedTrips.length) {
        this.usedTrips = usedTrips;
        this.cannotDeleteDestDialog = true;
      } else {
        this.isShowingDeleteDestDialog = true;
        this.currentDeletingDestinationId = destinationId;
      }
    }
  },
  computed: {
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
    viewOption(newViewOption) {
      this.$emit("viewOptionChanged", newViewOption); 
    }
  }
  
}

</script>


<style lang="scss" scoped>

  @import "../../../styles/_variables.scss";

  #destination-sidebar {
    height: 100%;
    width: 315px; 
    float: right;

    #title {
      height: 100px;
      background-color: $primary;
      color: $darker-white;
      text-align: center;
      display: flex;
      align-items: center;
      flex-direction: column;
      justify-content: space-between;
      position: fixed;
    }

    h2 {
      margin-top: 15px;
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

    #destinations-list {
      height: calc(100% - 100px);
      margin-top: 100px;
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
  }
</style>

