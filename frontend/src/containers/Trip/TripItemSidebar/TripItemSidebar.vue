<template>
  <v-card
    id="trip-item-sidebar"
    :elevation="20"
  >
  <div id="title" v-if="trip">
    <h2>{{ trip.tripName }}</h2>
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
      <Timeline :trip="trip"
        v-on:destinationOrderChanged="destinationOrderChanged"
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
        />
          
    </div>
  </div>

  </v-card>
</template>

<script>
import Sortable from "sortablejs";
import Timeline from "./Timeline/Timeline.vue";
import ModifyTripDestinationDialog from "./ModifyTripDestinationDialog/ModifyTripDestinationDialog";


export default {
  components: {
    Timeline,
    ModifyTripDestinationDialog
  },
  data() {
    return {
      isShowingAddDestinationDialog: false
    };
  },
  props: {
    trip: {
      required: true
    }
  },
  methods: {
    destinationOrderChanged(indexes) {
      this.$emit("destinationOrderChanged", indexes);
    }
  }
}

</script>


<style lang="scss" scoped>

  @import "../../../styles/_variables.scss";

  #trip-item-sidebar {
    position: fixed;
    height: 100%;
    width: 315px; 
    right: 0;


    #title {
      height: 50px;
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

    #trip-destinations-list {
      height: 100%;
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
  }


</style>

