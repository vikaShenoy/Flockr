<template>
  <div style="width: 100%">
    <div class="page-title"><h1>Destinations</h1></div>
    <div class="destinations-panel destinations-card">
      <DestinationCard
              v-for="(destination, index) in destinations"
              v-bind:key="index"
              :destination="destination.dest"
              :editMode="destination.editMode"
              :deleteOnClick="deleteDestination"
              @editModeChanged="changeEditMode"
      ></DestinationCard>
      <v-btn fab dark id="addDestinationButton" v-on:click="addNewDestinationCard">
        <v-icon dark>add</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>

  import DestinationCard from "./DestinationCard/DestinationCard";
  import { endpoint } from "../../utils/endpoint";
  const axios = require("axios");

  export default {
    components: {
      DestinationCard
    },
    data() {
      return {
        destinations: []
      }
    },
    mounted: function () {
      let currentDestinations;
      axios.get(endpoint("/destinations"))
              .then(response => {
                currentDestinations = response.data;
                for (let index in currentDestinations) {
                  this.destinations.push({
                    dest: currentDestinations[index],
                    editMode: false
                  });
                }
              })
              .catch(error => alert(error));
    },
    methods: {
      addNewDestinationCard: function () {
        this.destinations.unshift({
          dest: {
            "destId": "",
            "destName": "",
            "destType": {
              destTypeId: "",
              destTypeName: ""
            },
            "destDistrict": {
              districtId: "",
              districtName: ""
            },
            "destLat": 0,
            "destLon": 0,
            "destCountry": {
              countryId: "",
              countryName: ""
            }
          }, editMode: true
        });
      },

      deleteDestination: async function (event) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(event.target.parentNode);
        // Check if the destination is a new one (not in the database)
        if (!this.destinations[targetIndex].dest.id === "") {

          // TODO: Send the delete command to the back end

        }
        // Remove the destination from the page
        this.destinations.splice(targetIndex, 1);
      },

      changeEditMode: async function (value, target) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(target);
        this.destinations[targetIndex].editMode = value;
      },

      getIndexOfDestinationFromTarget: function (target) {
        let targetIndex = 0;
        let destinationCards = target.parentNode.childNodes;

        // Iterate through destination cards till the target card is found and save the index
        for(let i = 0; i < destinationCards.length; i++) {
          if (destinationCards[i] === target) {
            targetIndex = i;
          }
        }
        return targetIndex;
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




