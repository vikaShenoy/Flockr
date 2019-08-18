<template>
  <div class="trip-destination">
    <v-timeline-item
            :style="{width: '90%', marginLeft: '5%'}"
            color="primary"
            small
            :right="alignRight"
            :hide-dot="tripNode.nodeType === 'TripComposite'"
    >
      <v-card v-bind:class="(tripNode.arrivalDate && tripNode.departureDate) ? '' : 'no-date'">
        <v-card-title class="secondary trip-destination-title">
          <h3 @click="goToTripNode()" class="white--text font-weight-light">{{ tripNode.name }}</h3>
          <v-spacer align="right">
            <v-btn class="delete-btn" flat @click="$emit('deleteTripNode', tripNode)">
              <v-icon>delete</v-icon>
            </v-btn>
            <v-btn class="edit-btn" flat @click="$emit('showEditTripDestination', tripNode)" v-if="tripNode.nodeType === 'TripDestinationLeaf'">
              <v-icon>edit</v-icon>
            </v-btn>
          </v-spacer>
        </v-card-title>
        <div class="container">
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_takeoff</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ formatDateTime(tripNode.arrivalDate, tripNode.arrivalTime) }}</p>
            </v-flex>
          </v-layout>
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_landing</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ formatDateTime(tripNode.departureDate, tripNode.departureTime) }}</p>
            </v-flex>
          </v-layout>
        <v-spacer align="center" v-if="tripNode.nodeType === 'TripComposite'">
          <v-icon class="expand-trip" color="secondary" @click="toggleShowTripNodes(tripNode)">{{ tripNode.isShowing ? "keyboard_arrow_up" : "keyboard_arrow_down"}}</v-icon>
        </v-spacer>
        </div>
      </v-card>

    <div v-if="tripNode.nodeType === 'TripComposite'" v-bind:class="{ expanded: tripNode.nodeType === 'TripComposite'
    && tripNode.isShowing}" id="trip-nodes">
      <div>
        <Timeline 
          :trip="tripNode"
					isSubTrip
        />
        </div>
    </div>



    </v-timeline-item>
  </div>
</template>


<script>
  import moment from "moment";
  

  export default {
    components: {
      Timeline: () => import("../Timeline")
    },
    name: "TimelineDestination",
    props: {
      tripNode: Object,
      alignRight: Boolean
    },
    methods: {
      /**
       * Formats a date based on an optional date and time
       */
      formatDateTime(date, time) {
        if (!date && !time) {
          return "No Date";
        }

        const momentDate = moment(date);
        const formattedDate = momentDate.isSame(moment(), "year") ? momentDate.format("DD MMM") : momentDate.format("DD MMM YYYY");

        if (date && time) {
          return `${formattedDate} at ${time}`;
        }

        return formattedDate;
      },
      toggleShowTripNodes(tripNode) {
        this.$emit("toggleExpanded", tripNode.tripNodeId);
      },
      /**
       * Either goes to subtrip if trip node is a trip, or destination
       */
      goToTripNode() {
        if (this.tripNode.nodeType === "TripComposite") {
          this.$router.push(`/trips/${this.tripNode.tripNodeId}`);
        } else {
          this.$router.push(`/destinations/${this.tripNode.destination.destinationId}`);
        }
      }
    }
  };
</script>

<style lang="scss" scoped>
  .trip-destination-title {
    padding: 5px;
  }

  .container {
    padding: 5px;
  }

  .trip-destination {
    cursor: move;
  }

  .date-info {
    padding-left: 10px;
    padding-top: 8px;

    p {
      // Override default p style to make text align from the left
      text-align: left;
    }
  }

  .edit-btn, .delete-btn {
    margin: 0;
    min-width: 0px;
    width: 10px;
    color: #fff;
    font-size: 0.4rem;
  }

  .expand-trip {
    font-size: 2.5rem;
    cursor: pointer;

    
  }

  .no-date {
    background-color: #DDDDDD;
  }

  .light-text {
    color: #FFF;
  }




</style>

<style lang="scss">
  .v-timeline--dense .v-timeline-item__body {
    max-width: calc(100% - 22px) !important;
    
  }

  #trip-nodes {
    max-height: 0px;
    overflow: hidden;
    transition: all 0.3s ease;
  }

  .expanded {
    // You can't transition a height to auto so need to specify
    // an arbitrarily large max height
    max-height: 300rem !important;
  }

  h3 {
    cursor: pointer;
  }

</style>


