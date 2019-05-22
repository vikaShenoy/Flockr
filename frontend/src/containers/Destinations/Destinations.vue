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
              @deleteNewDestination="deleteNewDestination"
      ></DestinationCard>
      <v-btn fab id="addDestinationButton" v-on:click="openAddDestinationDialog">
        <v-icon>add</v-icon>
      </v-btn>
    </div>
    <add-destination-dialog :dialog="showAddDestination" v-on:dialogChanged="changeShowAddDestinationDialog"/>
  </div>
</template>

<script>
  import DestinationCard from "./DestinationCard/DestinationCard";
  import {requestCountries, requestDestinations, requestDestinationTypes, sendDeleteDestination} from "./DestinationsService";
  import AddDestinationDialog from "./AddDestinationDialog/AddDestinationDialog";

  export default {
    components: {
      AddDestinationDialog,
      DestinationCard
    },
    data() {
      return {
        destinations: [],
        countries: [],
        destinationTypes: [],
        showAddDestination: false
      }
    },
    /**
     * Load data.
     */
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
      try {
        this.countries = await requestCountries();
      } catch (error) {
        console.log(error);
      }

      try {
        this.destinationTypes = await requestDestinationTypes();
      } catch (error) {
        console.log(error);
      }
    },
    methods: {
      /**
       * Called when the add button is selected.
       * Opens the add destination dialog window.
       */
      openAddDestinationDialog() {
        this.showAddDestination = true;
      },
      /**
       * Called when the add destination dialog emits a dialogChanged event.
       * Changes the showAddDestination variable to match the value in the add destination dialog component.
       */
      changeShowAddDestinationDialog(newValue) {
        this.showAddDestination = newValue;
      },
      /**
       * Add a new empty destination card.
       */
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

      /**
       * Delete a destination from frontend and send request to delete from backend.
       */
      deleteDestination: async function (event) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(event.target.parentNode);
        try {
          sendDeleteDestination(this.destinations[targetIndex].destinationObject.destinationId);
          this.destinations.splice(targetIndex, 1);
        } catch(error) {
          console.log(error);
        }
      },

      /**
       * Delete a new destination.
       */
      deleteNewDestination: async function (target) {
        let targetIndex = await this.getIndexOfDestinationFromTarget(target);
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