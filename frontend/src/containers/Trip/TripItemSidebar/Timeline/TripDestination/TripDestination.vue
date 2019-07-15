<template>
  <div>
    <v-timeline-item
      :style="{width: '90%', marginLeft: '5%'}"
      color="primary"
      small
      :right="alignRight"
    >
      <v-card>
        <v-card-title class="secondary trip-destination-title">
          <h3 class="white--text font-weight-light">{{ tripDestination.destination.destinationName }}</h3>
        </v-card-title>
        <div class="container">
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_takeoff</v-icon>
            </v-flex>
            <v-flex xs10>
              <p>{{tripDestination.arrival}}</p>
            </v-flex>
          </v-layout>
          <v-layout>
            <v-flex xs2>
              <v-icon size="30">flight_landing</v-icon>
            </v-flex>
            <v-flex xs10>
              <p>{{tripDestination.departure}}</p>
            </v-flex>
          </v-layout>
        </div>
      </v-card>
    </v-timeline-item>
  </div>
</template>


<script>
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
</style>

