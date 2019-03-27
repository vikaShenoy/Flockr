<template>
  <div style="width: 100%">
    <div class="page-title"><h1>Destinations</h1></div>
    <div class="destinations-panel destinations-card">
      <DestinationCard
              v-for="(destination, index) in destinations"
              v-bind:key="index"
              :destination="destination.destinationObject"
              :editMode="destination.editMode"
              :deleteOnClick="deleteDestination"
              :destinationTypes="destinationTypes"
              :countries="countries"
              @editModeChanged="changeEditMode"
              @idChanged="changeIdOfDestination"
      ></DestinationCard>
      <v-btn fab dark id="addDestinationButton" v-on:click="addNewDestinationCard">
        <v-icon dark>add</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
  import DestinationCard from "./DestinationCard/DestinationCard";
  import {requestCountries, requestDestinations, requestDestinationTypes, sendDeleteDestination} from "./DestinationsService";

  export default {
    components: {
      DestinationCard
    },
    data() {
      return {
        destinations: [],
        countries: {
          names: [],
          ids: []
        },
        destinationTypes: {
          names: [],
          ids: []
        }
      }
    },
    mounted: async function () {
      let currentDestinations;
      try {
        currentDestinations = await requestDestinations();

        for (let index in currentDestinations) {
          this.destinations.push({
            destinationObject: currentDestinations[index],
            editMode: false
          });
        }
      } catch(error) {
        console.log(error);
      }
      let currentCountries;

      try {
        currentCountries = await requestCountries();

        for (let index in currentCountries) {
          this.countries.names.push(currentCountries[index].countryName);
          this.countries.ids.push(currentCountries[index].countryId);
        }
      } catch (error) {
        console.log(error);
      }

      let currentDestinationTypes;
      try {
        currentDestinationTypes = await requestDestinationTypes();

        for (let index in currentDestinationTypes) {
          this.destinationTypes.names.push(currentDestinationTypes[index].destinationTypeName);
          this.destinationTypes.ids.push(currentDestinationTypes[index].destinationTypeId);
        }
      } catch (error) {
        console.log(error);
      }
    },
    methods: {
      addNewDestinationCard: function () {
        this.destinations.unshift({
          destinationObject: {
            destinationId: null,
            destinationName: "",
            destinationType: {
              destinationTypeId: null,
              destinationTypeName: null
            },
            destinationDistrict: {
              districtId: null,
              districtName: null
            },
            destinationLat: "",
            destinationLon: "",
            destinationCountry: {
              countryId: null,
              countryName: null
            }
          }, editMode: true
        });
      },

      deleteDestination: async function (event) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(event.target.parentNode);
        // Check if the destination is a new one (not in the database)
        if (!(this.destinations[targetIndex].destinationObject.destinationId === "")) {
          try {
            sendDeleteDestination(this.destinations[targetIndex].destinationObject.destinationId);
          } catch(error) {
            console.log(error);
          }
        }
        this.destinations.splice(targetIndex, 1);
      },

      changeEditMode: async function (value, target) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(target);
        this.destinations[targetIndex].editMode = value;
      },

      changeIdOfDestination: async function (value, target) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(target);
        this.destinations[targetIndex].destinationObject.destinationId = value;
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
    },
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
  background: $primary;
  font-size: 20px;
  h1 {
    color: $darker-white;
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
  padding: 110px 50px 50px;
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




