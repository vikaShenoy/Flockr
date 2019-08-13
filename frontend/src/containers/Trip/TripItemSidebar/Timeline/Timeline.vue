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
              @showEditTripDestination="tripDestination => $emit('showEditTripDestination', tripDestination)"
              @deleteTripDestination="deleteTripDestination => $emit('deleteTripDestination', tripDestination)"
      />
    </v-timeline>
  </div>
</template>


<script>
  import Sortable from "sortablejs";
  import TripNode from "./TripNode/TripNode";

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
      if (!this.isSubTrip) {
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
          new Sortable(sortableTimelines[i], {
            group: 'nested',
            animation: 500,
            fallbackOnBody: true,
            swapThreshold: 0.65,
						onEnd: (event) => {
              const oldParentTripNodeId = Number(event.from.getAttribute("data-trip-node-id"));
							const newParentTripNodeId = Number(event.to.getAttribute("data-trip-node-id"));
							const newIndex = event.newIndex;
							const oldIndex = event.oldIndex;

							this.$emit("tripNodeOrderChanged", {
                oldParentTripNodeId,
								newParentTripNodeId,
								newIndex,
								oldIndex
							});
						}
          });
        }
      }
    }
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
