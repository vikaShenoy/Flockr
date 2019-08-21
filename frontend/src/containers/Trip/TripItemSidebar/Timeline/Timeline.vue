<template>
  <div id="trip-container">
    <v-timeline
      dense
      :data-trip-node-id="trip.tripNodeId"
    >
      <!--data-destinationId is used to disable sorting items with the same destinationID-->
      <TripNode
        v-for="tripNode in trip.tripNodes"
        v-bind:key="tripNode.tripNodeId"
        :tripNode="tripNode"
        :alignRight="false"
        @toggleExpanded="tripNodeId => $emit('toggleExpanded', tripNodeId)"
        @showEditTripDestination="tripDestination => $emit('showEditTripDestination', tripDestination)"
        @deleteTripNode="tripNode => $emit('deleteTripNode', tripNode)"
        @tripNameUpdated="(tripNode, newName) => $emit('tripNameUpdated', tripNode, newName)"
      />
    </v-timeline>
  </div>
</template>


<script>
import Sortable from "sortablejs";
import TripNode from "./TripNode/TripNode";
import { sortTimeline } from "./TimelineService";

export default {
  props: {
    trip: {
      tripNodes: Object,
      tripNodeId: Number
    },
    isSubTrip: {
      type: Boolean
    }
  },
  components: {
    TripNode
  },
  methods: {
    initSorting() {
      // Loop through each nested sortable element
      const sortableTimelines = document.querySelectorAll(".v-timeline");
      for (let i = 0; i < sortableTimelines.length; i++) {
        // Only sort timelines that don't have sorted property already
        const beenSortedAlready = sortableTimelines[i].getAttribute(
          "has-been-sorted"
        );
        if (!beenSortedAlready) {
          sortTimeline(sortableTimelines[i], indexes => {
            this.$emit("tripNodeOrderChanged", indexes);
          });
        }
      }
    }
  },
  mounted() {
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
