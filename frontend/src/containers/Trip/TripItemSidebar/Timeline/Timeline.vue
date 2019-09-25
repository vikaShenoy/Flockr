<template>
  <div id="trip-container">
    <v-timeline dense :data-trip-node-id="trip.tripNodeId">
      <!--data-destinationId is used to disable sorting items with the same destinationID-->
      <TripNode
        v-for="tripNode in trip.tripNodes"
        v-bind:key="tripNode.tripNodeId"
        :tripNode="tripNode"
        :rootTrip="rootTrip"
        :parentTrip="trip"
        :alignRight="false"
        @toggleExpanded="tripNodeId => $emit('toggleExpanded', tripNodeId)"
        @showEditTripDestination="tripDestination => $emit('showEditTripDestination', tripDestination)"
        @deleteTripNode="$emit('deleteTripNode', tripNode)"
        @tripNameUpdated="(tripNode, newName) => $emit('tripNameUpdated', tripNode, newName)"
      />
    </v-timeline>
  </div>
</template>


<script>
import TripNode from "./TripNode/TripNode";
import { sortTimeline } from "./TimelineService";
import roleType from "../../../../stores/roleType";
import UserStore from "../../../../stores/UserStore";
import { getTripNodeById } from "../../TripService";

export default {
  props: {
    trip: {
      tripNodes: Object,
      tripNodeId: Number
    },
    isSubTrip: {
      type: Boolean
    },
    // Need to pass the parent trip as Timeline component works recursively
    rootTrip: {
      type: Object
    }
  },
  components: {
    TripNode
  },
  methods: {
    /**
     * Sorts the trips nodes
     */
    initSorting() {
      // Loop through each nested sortable element
      const sortableTimelines = document.querySelectorAll(".v-timeline");

      for (let i = 0; i < sortableTimelines.length; i++) {
        // Only sort timelines that don't have sorted property already
        const tripNodeId = sortableTimelines[i].getAttribute(
          "data-trip-node-id"
        );

        const tripToBeSorted = getTripNodeById(Number(tripNodeId), this.trip);

        const beenSortedAlready = sortableTimelines[i].getAttribute(
          "has-been-sorted"
        );
        if (!beenSortedAlready && this.hasPermissionToEdit(tripToBeSorted)) {
          sortTimeline(sortableTimelines[i], indexes => {
            this.$emit("tripNodeOrderChanged", indexes);
          });
        }
      }
    },
    /**
     * Checks if the user has the right to edit the trip. Returns true if the user has the right, otherwise
     * returns false
     * @param trip the trip to be checked
     * @returns {T | boolean}
     */
    hasPermissionToEdit(trip) {
      const userRole = trip.userRoles.find(
        userRole => userRole.user.userId === UserStore.data.userId
      );

      const isTripManager = userRole && userRole.role.roleType === roleType.TRIP_MANAGER;
      const isTripOwner = userRole && userRole.role.roleType === roleType.TRIP_OWNER;

      return isTripManager || isTripOwner;
    }
  },
  mounted() {
    // User needs to be a trip manager or owner to change order of trip nodes
    if (!this.isSubTrip) {
      setTimeout(() => {
        this.initSorting();
      }, 0);
    }
  },
  watch: {
    trip: {
      handler() {
        // Sorting should only be done once by the parent which is why flag is used
        // User also needs to have the correct permissions to be able to drag and drop
        if (!this.isSubTrip) {
          // setTimeout enforces that the function runs only when the whole view has rendered, meaning
          // that we initialise drag and drop for every level
          setTimeout(() => {
            this.initSorting();
          }, 0);
        }
      },
      deep: true
    }
  }
};
</script>

<style lang="scss" scoped>
#trip-container {
  width: 100%;
}

h2 {
  text-align: center;
  padding: 15px;
}
</style>
