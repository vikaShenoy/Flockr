<template>
  <div v-bind:class="hasPermissionToEdit ? 'trip-destination-cursor' : '' ">
    <v-timeline-item  
      :style="{width: '90%', marginLeft: '5%'}"
      color="primary"
      small
      :right="alignRight"
      :hide-dot="tripNode.nodeType === 'TripComposite'"
    >
      <v-card v-bind:class="(tripNode.arrivalDate && tripNode.departureDate) ? '' : 'no-date'">
        <v-card-title class="secondary trip-destination-title">
          <v-text-field
            v-if="isEditingTrip"
            autofocus
            v-model="editedTripName"
            @blur="nameEdited"
            @keyup.enter="nameEdited"
            color="white"
            class="trip-name-field"
            :rules="tripNameRules"
          ></v-text-field>
          <h3
            v-else
            @click="goToTripNode()"
            class="white--text font-weight-light"
          >{{ tripNode.name }}</h3>

          <v-spacer align="right" v-if="hasPermissionToEdit">
            <v-btn class="edit-btn" flat @click="editTripNode">
              <v-icon v-if="!isEditingTrip">edit</v-icon>
            </v-btn>

            <v-btn class="delete-btn" flat @click="$emit('deleteTripNode', tripNode)" v-if="hasPermissionToUnlink">
              <v-icon v-if="isTripDestinationLeaf">delete</v-icon>
              <v-icon v-else>link_off</v-icon>
            </v-btn>
          </v-spacer>
        </v-card-title>
        <div class="container">
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_takeoff</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ tripNode.arrivalTotal }}</p>
            </v-flex>
          </v-layout>
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_landing</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ tripNode.departureTotal }}</p>
            </v-flex>
          </v-layout>
          <v-spacer align="center" v-if="tripNode.nodeType === 'TripComposite'">
            <v-btn
              @click="toggleShowTripNodes(tripNode)"
              tile
              outlined
              color="secondary"
            >
              <v-icon left>{{ tripNode.isShowing ? "keyboard_arrow_up" : "keyboard_arrow_down"}}</v-icon>
              {{ tripNode.isShowing ? 'Collapse' : 'Expand' }}
            </v-btn>
          </v-spacer>
        </div>
      </v-card>

      <div
        v-if="tripNode.nodeType === 'TripComposite'"
        v-bind:class="{ expanded: tripNode.nodeType === 'TripComposite'
    && tripNode.isShowing}"
        id="trip-nodes"
      >
        <div>
          <Timeline
            @deleteTripNode="tripNode => $emit('deleteTripNode', tripNode)"
            @toggleExpanded="tripNode => $emit('toggleExpanded', tripNode)"
            @showEditTripDestination="tripNode => $emit('showEditTripDestination', tripNode)"
            :trip="tripNode"
            isSubTrip
            :rootTrip="rootTrip"
          />
        </div>
      </div>
    </v-timeline-item>
  </div>
</template>


<script>
import moment from "moment";
import { rules } from "../../../../../utils/rules";
import UserStore from "../../../../../stores/UserStore";
import roleType from "../../../../../stores/roleType";

export default {
  components: {
    Timeline: () => import("../Timeline")
  },
  name: "TimelineDestination",
  props: {
    tripNode: Object,
    alignRight: Boolean,
    rootTrip: Object,
    parentTrip: Object,
  },
  data() {
    return {
      isEditingTrip: false,
      editedTripName: "",
      tripNameRules: [rules.required]
    };
  },
  computed: {
    isTripDestinationLeaf() {
      return this.tripNode.nodeType === "TripDestinationLeaf";
    },
    getArrivalDate() {
      const isLeaf = this.tripNode.nodeType === "TripDestinationLeaf";
      return isLeaf
        ? this.tripNode.arrivalDate
        : this.getTripNodeArrivalDate(this.tripNode);
    },
    getArrivalTime() {
      const isLeaf = this.tripNode.nodeType === "TripDestinationLeaf";
      return isLeaf
        ? this.tripNode.arrivalTime
        : this.getTripNodeArrivalTime(this.tripNode);
    },
    getDepartureDate() {
      const isLeaf = this.tripNode.nodeType === "TripDestinationLeaf";
      return isLeaf
        ? this.tripNode.departureDate
        : this.getTripNodeDepartureDate(this.tripNode);
    },
    getDepartureTime() {
      const isLeaf = this.tripNode.nodeType === "TripDestinationLeaf";
      return isLeaf
        ? this.tripNode.departureTime
        : this.getTripNodeDepartureTime(this.tripNode);
    },
    hasPermissionToEdit() {
      let userRole;
      if (this.tripNode.nodeType === "TripDestinationLeaf") {
        userRole = this.parentTrip.userRoles.find(
          userRole => userRole.user.userId === UserStore.data.userId
        );
      } else {
        userRole = this.tripNode.userRoles.find(
          userRole => userRole.user.userId === UserStore.data.userId
        );
      }

      const isTripManager = userRole && userRole.role.roleType === roleType.TRIP_MANAGER;
      const isTripOwner = userRole && userRole.role.roleType === roleType.TRIP_OWNER;

      return isTripManager || isTripOwner;
    },
    hasPermissionToUnlink() {
      const userRole = this.parentTrip.userRoles.find(
        userRole => userRole.user.userId === UserStore.data.userId
      );

      const isTripManager = userRole && userRole.role.roleType === roleType.TRIP_MANAGER;
      const isTripOwner = userRole && userRole.role.roleType === roleType.TRIP_OWNER;

      return isTripManager || isTripOwner;
    }
  },
  methods: {
    getTripNodeArrivalDate(tripNode) {
      if (tripNode.nodeType === "TripDestinationLeaf") {
        if (tripNode.arrivalDate) {
          return tripNode.arrivalDate;
        }
        if (tripNode.departureDate) {
          return tripNode.departureDate;
        }
      } else {
        for (const currentTripNode of tripNode.tripNodes) {
          return this.getTripNodeArrivalDate(currentTripNode);
        }
      }
    },
    getTripNodeArrivalTime(tripNode) {
      if (tripNode.nodeType === "TripDestinationLeaf") {
        if (tripNode.arrivalDate) {
          return tripNode.arrivalTime;
        }
        if (tripNode.departureDate) {
          return tripNode.departureTime;
        }
      } else {
        for (const currentTripNode of tripNode.tripNodes) {
          return this.getTripNodeArrivalTime(currentTripNode);
        }
      }
    },
    getTripNodeDepartureDate(tripNode) {
      if (tripNode.nodeType === "TripDestinationLeaf") {
        if (tripNode.departureDate) {
          return tripNode.departureDate;
        }
        if (tripNode.arrivalDate) {
          return tripNode.arrivalDate;
        }
      } else {
        for (const currentTripNode of [...tripNode.tripNodes].reverse()) {
          return this.getTripNodeDepartureDate(currentTripNode);
        }
      }
    },
    getTripNodeDepartureTime(tripNode) {
      if (tripNode.nodeType === "TripDestinationLeaf") {
        if (tripNode.departureDate) {
          return tripNode.departureTime;
        }
        if (tripNode.arrivalDate) {
          return tripNode.arrivalTime;
        }
      } else {
        for (const currentTripNode of [...tripNode.tripNodes].reverse()) {
          return this.getTripNodeDepartureTime(currentTripNode);
        }
      }
    },

    /**
     * Formats a date based on an optional date and time
     */
    formatDateTime(date, time) {
      if (!date && !time) {
        return "No Date";
      }

      const momentDate = moment(`${date} ${time}`);

      return momentDate.isSame(moment(), "year")
          ? momentDate.format("DD MMM hh:mm A")
          : momentDate.format("DD MMM YYYY hh:mm A");
    },
    toggleShowTripNodes(tripNode) {
      this.$emit("toggleExpanded", tripNode.tripNodeId);
    },
    nameEdited() {
      if (this.editedTripName && this.editedTripName.length > 0) {
        if (this.editedTripName !== this.tripNode.name) {
          this.isEditingTrip = false;
          this.$emit("tripNameUpdated", this.tripNode, this.editedTripName);
        } else {
          this.isEditingTrip = false;
        }
      }
    },
    /**
     * Either goes to subtrip if trip node is a trip, or destination
     */
    goToTripNode() {
      if (this.tripNode.nodeType === "TripComposite") {
        this.$router.push(`/trips/${this.tripNode.tripNodeId}`);
      } else {
        this.$router.push(
          `/destinations/${this.tripNode.destination.destinationId}`
        );
      }
    },
    editTripNode() {
      if (this.tripNode.nodeType === "TripComposite") {
        this.editedTripName = this.tripNode.name;
        this.isEditingTrip = true;
      } else {
        this.$emit("showEditTripDestination", this.tripNode);
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

.trip-destination-cursor {
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

.edit-btn,
.delete-btn {
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
  background-color: #dddddd;
}

.light-text {
  color: #fff;
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

.trip-name-field {
  -webkit-text-fill-color: white;
}
</style>


