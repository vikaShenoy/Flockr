<template>
  <v-card
          id="trip-item-sidebar"
          :elevation="20"
  >

  
    <div id="title" v-if="trip">
      <v-btn
        flat
        color="secondary"
        id="manage-trip-btn"
        @click="isShowingManageTripDialog = true"
      >Manage
      </v-btn>

      <h2 id="trip-name">{{ trip.tripName }}</h2>
    </div>

    <div id="trip-destinations-list">
      <div v-if="!trip" id="spinner">
        <v-progress-circular
                indeterminate
                color="secondary"
                style="align-self: center;"
        >
        </v-progress-circular>

      </div>

      <div v-else>
        <Timeline
                :trip="trip"
                v-on:destinationOrderChanged="destinationOrderChanged"
                v-on:showEditTripDestination="showEditTripDestination"
                v-on:deleteTripDestination="tripDestination => $emit('deleteTripDestination', tripDestination)"
        />

        <v-btn
                depressed
                color="secondary"
                id="add-trip-destination-btn"
                @click="isShowingAddDestinationDialog = true"
        >
          Add Destination
        </v-btn>


        <ModifyTripDestinationDialog
                :isShowing.sync="isShowingAddDestinationDialog"
                :editMode="false"
                :trip="trip"
                v-on:updatedTripDestinations="tripDestinationsUpdated"
        />

        <ModifyTripDestinationDialog
                :isShowing.sync="isShowingUpdateDestinationDialog"
                :editMode="true"
                :trip="trip"
                v-on:updatedTripDestinations="tripDestinationsUpdated"
                :editedTripDestination="editedTripDestination"
        />

        <ManageTripDialog 
          :isShowing.sync="isShowingManageTripDialog" 
          :trip="trip"
          v-if="trip"
          @newUsers="newUsers"
        />
      </div>
    </div>

  </v-card>
</template>

<script>
  import Timeline from "./Timeline/Timeline.vue";
  import ModifyTripDestinationDialog from "./ModifyTripDestinationDialog/ModifyTripDestinationDialog";
  import ManageTripDialog from "./ManageTripDialog/ManageTripDialog";

  export default {
    components: {
      Timeline,
      ModifyTripDestinationDialog,
      ManageTripDialog
    },
    data() {
      return {
        isShowingAddDestinationDialog: false,
        isShowingUpdateDestinationDialog: false,
        editedTripDestination: null,
        isShowingManageTripDialog: false
      };
    },
    props: {
      trip: {
        required: true
      }
    },
    methods: {
      /**
       * Emitted when the order of destinations have changed
       */
      destinationOrderChanged(indexes) {
        this.$emit("destinationOrderChanged", indexes);
      },
      /**
       * Called when the edit button has been pressed on
       * a trip destination
       */
      showEditTripDestination(tripDestination) {
        this.isShowingUpdateDestinationDialog = true;
        this.editedTripDestination = tripDestination;
      },
      tripDestinationsUpdated(tripDestinations) {
        this.$emit("updatedTripDestinations", tripDestinations);
      },
      newUsers(newUsers) {
        this.$emit("newUsers", newUsers); 
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #trip-item-sidebar {
    width: 315px;
    height: calc(100vh - 60px);


    #title {
      height: 80px;
      background-color: $primary;
      color: #FFF;
      z-index: 0;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    h2 {
      z-index: 3;
    }

    #trip-destinations-list {
      padding-bottom: 10px;
      overflow-y: auto;
      height: calc(100vh - 145px);
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

    #add-trip-destination-btn {
      margin: 0 auto;
      display: block;
    }

    #manage-trip-btn {
      position: absolute;
      top: 0;
      left: 0;
    }

    #trip-name {
      margin-top: 30px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      margin-left: 10px;
      margin-right: 10px;
    }
  }


</style>

