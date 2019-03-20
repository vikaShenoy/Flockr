<template>
  <div style="width: 100%">
    <div class="page-title"><h1>Destinations</h1></div>
    <div class="destinations-panel destinations-card">
      <DestinationCardEdit
              v-for="newDestination in newDestinations"
              v-bind:key="newDestination.id"
              :destination="newDestination"
              :onClick="saveNewDestination">
      </DestinationCardEdit>
      <DestinationCard
              v-for="destination in destinations"
              v-bind:key="destination.id"
              :destination="destination">
      </DestinationCard>
      <v-btn fab dark id="addDestinationButton" v-on:click="addNewDestinationCard">
        <v-icon dark>add</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
import DestinationCard from "./DestinationCard/DestinationCard";
import DestinationCardEdit from "./DestinationCard/DestinationCardEdit";

export default {
  components: {
    DestinationCardEdit,
    DestinationCard
  },
  data() {
    return {
      destinations: [
        {
          "id": 15043,
          "destinationName": "Christchurch",
          "destinationType": "City",
          "district": "Canterbury",
          "latitude": 43.5321,
          "longitude": 172.6362,
          "country": "New Zealand"
        },
        {
          "id": 15044,
          "destinationName": "1st Staircase",
          "destinationType": "Place",
          "district": "South Auckland",
          "latitude": -38.450361,
          "longitude": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15045,
          "destinationName": "1st Staircase",
          "destinationType": "Place",
          "district": "South Auckland",
          "latitude": -38.450361,
          "longitude": 174.642722,
          "country": "New Zealand"
        }
      ],
      newDestinations: []
    }
  },
  methods: {
    addNewDestinationCard: function () {
      this.newDestinations.unshift({
        "id": 0,
        "destinationName": "",
        "destinationType": "",
        "district": "",
        "latitude": 0,
        "longitude": 0,
        "country": ""
      });
    },
    saveNewDestination: function (event) {
      // TODO: Implement validation of fields
      let newDest = this.newDestinations.splice(this.newDestinations.indexOf(event.target.index), 1).pop();
      this.destinations.unshift(newDest);
      // TODO: Send the new destination to the back-end
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

.page-title {
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

.title-card {
  display: inline-block;
}

.destinations-panel {
  padding: 150px 50px 50px 50px;
}

.destinations-panel :hover #delete-destination-button {
  visibility: visible;
}

.destinations-panel :hover #edit-destination-button {
  visibility: visible;
}

.destinations-card {
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




