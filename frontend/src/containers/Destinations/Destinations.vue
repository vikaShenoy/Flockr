<template>
  <div>
    <div id="page-title"><h1>Destinations</h1></div>
    <div id="destinations-card">
      <DestinationsCard
        v-for="dest in destinations"
        v-bind:key="dest.id"
      >
        <v-card-title><h2>{{ dest.name }}</h2></v-card-title>
      </DestinationsCard>
      <v-btn fab dark id="addDestinationButton">
        <v-icon dark>add</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>

import DestinationsCard from "./DestinationCard/DestinationCard";

export default {
  components: {
    DestinationsCard
  },
  data() {
    return {
      destinations: [
        {
          "id": 15043,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15044,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15045,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        }
      ]
    }
  },
  methods: {
    // Methods here
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

#page-title {
  position: fixed;
  z-index: 1;
  width: 100%;
  padding: 15px;
  text-align: left;
  background: black;
  color: $primary;
  font-size: 30px;
  h1 {
    color: white;
  }
}

#destinations-card {
  padding: 150px 50px 50px;

}

#addDestinationButton {
  position: fixed;
  bottom: 30px;
  right: 30px;
  color: $secondary;
  .v-icon {
    color: $darker-white;
  }
}

</style>




