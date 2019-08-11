<template>
  <div id="trip-container">
    <v-timeline
            dense
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
        tripNodes: Object
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
        // const table = document.querySelector(".v-timeline");
        // Sortable.create(table, {
        //   animation: 150,
        //   onEnd: ({newIndex, oldIndex}) => {
        //     this.$emit("destinationOrderChanged", {newIndex, oldIndex});
        //   }
        // });
        // Loop through each nested sortable element
        const sortableTimelines = document.querySelectorAll(".v-timeline");
        console.log(sortableTimelines);
        for (let i = 0; i < sortableTimelines.length; i++) {
          new Sortable(sortableTimelines[i], {
            group: 'nested',
            animation: 500,
            fallbackOnBody: true,
            swapThreshold: 0.65,
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
