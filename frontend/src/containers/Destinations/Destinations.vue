<template>
  <div style="width: 100%">
    <div class="page-title"><h1>Destinations</h1></div>
      <div class="destinations-panel destinations-card">
        <div v-if="!publicDestinations || !userDestinations" id="loading">
           <v-progress-circular
            indeterminate
            color="secondary"
          ></v-progress-circular>
        </div>

        <div v-else>
          <v-expansion-panel>
            <v-expansion-panel-content>
              <template v-slot:header>
                <h2>My Destinations</h2>
              </template>
              <!--
                Need this div to appear when no destinations are there or
                the expansion panel will disappear
               -->
              <div v-if="userDestinations.length === 0"></div>
              <destination-card

                    v-for="(destination, index) in userDestinations"
                    v-bind:key="index"
                    :destination="destination"
                    @deleteDestination="displayDeletePrompt(destination, index)"
                    @editDestination="editDestination(index, destination)"
                    @displayMessage="displayMessage"
                    @displayRemovePrompt="displayRemovePrompt"
                    />
            </v-expansion-panel-content>
          </v-expansion-panel>

          <br>

          <v-expansion-panel>
            <v-expansion-panel-content>
              <template v-slot:header>
                <h2>All Public Destinations</h2>
              </template>
              <div v-if="publicDestinations.length === 0"></div>
              <DestinationCard
                    v-for="(destination, index) in publicDestinations"
                    v-bind:key="index"
                    :destination="destination"
                    @deleteDestination="displayPrompt(destination, index)"
                    @editDestination="editDestination(index, destination)"
                    @displayMessage="displayMessage"/>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </div>
        <v-btn fab id="addDestinationButton" v-on:click="openAddDestinationDialog">
          <v-icon>add</v-icon>
        </v-btn>
      </div>
    <modify-destination-dialog
            :dialog="showModifyDestination"
            :destinationTypes="destinationTypes"
            :countries="countries"
            :editedDestination="editedDestination"
            :editMode="editMode"
            :index="editIndex"
            @dialogChanged="changeShowAddDestinationDialog"
            @displayMessage="displayMessage"
            @addNewDestination="addNewDestinationCard"
            @updateDestination="updateDestination"/>
    <snackbar
      :snackbarModel="snackBar"
      @displayMessage="displayMessage"
      @dismissSnackbar="dismissSnackbar"/>
    <prompt-dialog
      :onConfirm="promptDialog.deleteFunction"
      :dialog="promptDialog.show"
      :message="promptDialog.message"
      @promptEnded="promptEnded"/>
  </div>
</template>

<script>
  import DestinationCard from "./DestinationCard/DestinationCard";
  import {requestCountries, getUserDestinations, getPublicDestinations, requestDestinationTypes, sendDeleteDestination} from "./DestinationsService";
  import ModifyDestinationDialog from "./ModifyDestinationDialog/ModifyDestinationDialog";
  import Snackbar from "../../components/Snackbars/Snackbar";
  import PromptDialog from "../../components/PromptDialog/PromptDialog";

  export default {
    components: {
      PromptDialog,
      Snackbar,
      ModifyDestinationDialog: ModifyDestinationDialog,
      DestinationCard
    },
    data() {
      return {
        userDestinations: null,
        publicDestinations: null,
        countries: [],
        destinationTypes: [],
        showModifyDestination: false,
        editedDestination: {
          destinationId: null,
          destinationName: null,
          destinationType: {
            destinationTypeId: null,
            destinationTypeName: null
          },
          destinationDistrict: {
            districtName: null,
            districtId: null
          },
          destinationCountry: {
            countryName: null,
            countryId: null
          },
          destinationLat: null,
          destinationLon: null,
          isPublic: false,
          index: null
          // TODO: Add owner here when ready.
        },
        editMode: false,
        editIndex: null,
        promptDialog: {
          show: false,
          deleteFunction: null,
          message: ""
        },
        snackBar: {
          show: false,
          timeout: 5000,
          text: "",
          color: "green",
          snackbarId: 1
        }
      }
    },
    /**
     * Load data.
     */
    mounted: async function () {
      try {
        const userId = localStorage.getItem("userId");
        this.userDestinations = await getUserDestinations(userId);
        this.publicDestinations = await getPublicDestinations();

      } catch(error) {
        this.displayMessage({
          show: true,
          text: error.message,
          color: "red"
        });
      }
      try {
        this.countries = await requestCountries();
      } catch (error) {
        this.displayMessage({
          show: true,
          text: error.message,
          color: "red"
        });
      }

      try {
        this.destinationTypes = await requestDestinationTypes();
      } catch (error) {
        this.displayMessage({
          show: true,
          text: error.message,
          color: "red"
        });
      }
    },
    methods: {
      /**
       * Displays a message using the snackbar.
       */
      displayMessage(snackBar) {
        this.snackBar.text = snackBar.text;
        this.snackBar.color = snackBar.color;
        this.snackBar.show = true;
      },
      /**
       * Dismisses the snackbar
       */
      dismissSnackbar() {
        this.snackBar.show = false;
      },
      /**
       * Called when the add button is selected.
       * Opens the add destination dialog window.
       */
      openAddDestinationDialog() {
        this.editMode = false;
        this.showModifyDestination = true;
      },
      /**
       * Called when the modify destination dialog emits a dialogChanged event.
       * Changes the showModifyDestination variable to match the value in the add destination dialog component.
       */
      changeShowAddDestinationDialog(newValue) {
        this.showModifyDestination = newValue;
      },
      /**
       * Add a new destination to the list of destinations.
       *
       * @param newDestination {POJO} the new destination to add to the list of destinations.
       */
      addNewDestinationCard: function (newDestination) {
        // TODO: change this to take public/private into account
        this.userDestinations.unshift(newDestination);
      },
      /**
       * Update an existing destination after edit.
       *
       * @param destination {POJO} the updated destination.
       * @param index {int} the index of the destination.
       */
      updateDestination(destination, index) {
        this.destinations[index] = destination;
        this.editedDestination = {
          destinationId: null,
          destinationName: null,
          destinationType: {
            destinationTypeId: null,
            destinationTypeName: null
          },
          destinationDistrict: {
            districtName: null,
            districtId: null
          },
          destinationCountry: {
            countryName: null,
            countryId: null
          },
          destinationLat: null,
          destinationLon: null,
          isPublic: false
        };
        this.editIndex = null;
        this.showModifyDestination = false;
      },
      /**
       * Gets the delete function for a destination.
       *
       * @param destination {POJO} the destination to be deleted.
       * @param index {Number} the index of the destination in the destinations list.
       * @return {Function} the delete function for this destination.
       */
      getDeleteFunction: function (destination, index) {
        return async () => {
          try {
            await sendDeleteDestination(destination.destinationId);
            this.destinations.splice(index, 1);
            this.displayMessage({
              text: `Destination ${destination.destinationName} successfully deleted.`,
              color: "green"
            });
          } catch (error) {
            this.displayMessage({
              show: true,
              text: error.message,
              color: "red"
            });
          }
        }
      },
      /**
       * Called when the edit button is selected on a destination.
       *
       * @param {Number} index the index of the destination in the destinations list.
       * @param {POJO} destination the destination Object
       */
      editDestination(index, destination) {
        this.editedDestination = destination;
        this.editIndex = index;
        this.editMode = true;
        this.showModifyDestination = true;
      },
      /**
       * Calls a prompt dialog to be displayed to the user.
       *
       * @param destination {POJO} the destination to be deleted on confirmation.
       * @param index {Number} the index of the destination in the destinations list.
       */
      displayDeletePrompt(destination, index) {
        this.promptDialog.message = "Are you sure you would like to delete this destination?";
        this.promptDialog.deleteFunction = this.getDeleteFunction(destination, index);
        this.promptDialog.show = true;
      },
      /**
       * Calls a prompt dialog to be displayed to the user.
       *
       * @param removePhotoFunction {Function} the function to call on confirmation.
       */
      displayRemovePrompt(removePhotoFunction) {
        this.promptDialog.message = "Are you sure you would like to remove this photo from the destination?";
        this.promptDialog.deleteFunction = removePhotoFunction;
        this.promptDialog.show = true;
      },
      /**
       * Called when the prompt dialog has finished.
       * Closes the prompt dialog and resets the values to defaults.
       */
      promptEnded() {
        this.promptDialog.message = "";
        this.promptDialog.deleteFunction = null;
        this.promptDialog.show = false;
      },
      /**
       * Sets the district of the edited Destination to null values.
       * If the destination country value does not match the districts country value.
       */
      onEditCountryChanged() {
        if (![null, undefined].includes(this.editedDestination.destinationDistrict.country) &&
            this.editedDestination.destinationCountry.countryId !==
            this.editedDestination.destinationDistrict.country.countryId) {
          this.editedDestination.destinationDistrict.districtId = null;
          this.editedDestination.destinationDistrict.districtName = null;
          this.editedDestination.destinationDistrict.country.countryId = null;
        }
      }
    },
    watch: {
      editCountryValue : {
        handler: "onEditCountryChanged",
        immediate: true
      }
    },
    computed: {
      editCountryValue() {
        return this.editedDestination.destinationCountry.countryId;
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

#add-destinations-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  color: $secondary;
  .v-icon {
    color: $darker-white;
  }
}

#loading {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

</style>