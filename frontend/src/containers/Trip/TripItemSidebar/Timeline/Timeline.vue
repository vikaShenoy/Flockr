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
							@toggleExpanded = "tripNodeId => $emit('toggleExpanded', tripNodeId)"
              @showEditTripDestination="tripDestination => $emit('showEditTripDestination', tripDestination)"
              @deleteTripDestination="deleteTripDestination => $emit('deleteTripDestination', tripDestination)"
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
    mounted() {
      /* Initialising sorting should only be done by the root timeline hence the conditional */
      if (!this.isSubTrip) {
        // setTimeout enforces that the function runs only when the whole view has rendered, meaning 
        // that we initialise drag and drop for every level
        setTimeout(() => {
          this.initSorting();
        }, 0);
      }
    },
    methods: {
      initSorting() {
        // Loop through each nested sortable element
        const sortableTimelines = document.querySelectorAll(".v-timeline");
        for (let i = 0; i < sortableTimelines.length; i++) {
          sortTimeline(sortableTimelines[i], indexes => {
            this.$emit("tripNodeOrderChanged", indexes);
					});
        }
      },
    },
		watch: {
		},
  }
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
