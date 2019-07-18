<template>
  <div class="trip-destination">
    <v-timeline-item
      :style="{width: '90%', marginLeft: '5%'}"
      color="primary"
      small
      :right="alignRight"
    >
      <v-card>
        <v-card-title class="secondary trip-destination-title">
          <h3 class="white--text font-weight-light">{{ tripDestination.destination.destinationName }}</h3>
          <v-spacer align="right">
            <v-btn class="delete-btn" flat @click="$emit('deleteTripDestination', tripDestination)"><v-icon>delete</v-icon></v-btn> 
            <v-btn class="edit-btn" flat @click="$emit('showEditTripDestination', tripDestination)"><v-icon>edit</v-icon></v-btn> 
          </v-spacer>
        </v-card-title>
        <div class="container">
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_takeoff</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ formatDateTime(tripDestination.arrivalDate, tripDestination.arrivalTime) }}</p>
            </v-flex>
          </v-layout>
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_landing</v-icon>
            </v-flex>
            <v-flex xs10 class="date-info">
              <p>{{ formatDateTime(tripDestination.departureDate, tripDestination.departureTime) }}</p>
            </v-flex>
          </v-layout>
        </div>
      </v-card>
    </v-timeline-item>
  </div>
</template>


<script>
import moment from "moment";

export default {
  name: "TimelineDestination",
  props: {
    tripDestination: {
      destinationName: String,
      arrivalDate: String,
      departureDate: String
    },
    alignRight: Boolean
  },
  methods: {
    /**
     * Formats a date based on an optional date and time
     */
    formatDateTime(date, time) {
      if (!date && !time) {
        console.log(date, time)
        return "No Date";
      }

      const momentDate = moment(date);
      const formattedDate = momentDate.isSame(moment(), "year") ? momentDate.format("DD MMM") : momentDate.format("DD MMM YYYY");

      if (date && time) {
        return `${formattedDate} at ${time}`;
      }

      return formattedDate;
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


</style>

