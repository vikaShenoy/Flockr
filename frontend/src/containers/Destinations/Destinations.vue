<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap
              :destinations="getDestinationsCurrentlyViewing()"
              :latitude="latitude"
              :longitude="longitude"
              @addCoordinates="addCoordinates"
      />
    </div>

    <DestinationSidebar
            :viewOption="viewOption"
            :yourDestinations="yourDestinations"
            :publicDestinations="publicDestinations"
            v-on:viewOptionChanged="viewOptionChanged"
            v-on:addDestinationClicked="addDestinationClicked"
            @addNewDestination="addNewDestination"
            @refreshDestinations="refreshDestinations"
            ref="sidebar"
            :latitude="latitude"
            :longitude="longitude"
    />
    <Snackbar :snackbarModel="snackbarModel" v-on:dismissSnackbar="snackbarModel.show=false"/>
  </div>
</template>

<script>
  import DestinationSidebar from "./DestinationSidebar/DestinationSidebar";
  import DestinationMap from "../../components/DestinationMap/DestinationMap";
  import {
    getPublicDestinations,
    getYourDestinations,
    sendDeleteDestination,
    sendUndoDeleteDestination
  } from "./DestinationsService";
  import Command from "../../components/UndoRedo/Command";
  import Snackbar from "../../components/Snackbars/Snackbar";

  export default {
    components: {
      Snackbar,
      DestinationSidebar,
      DestinationMap
    },
    data() {
      return {
        destination: null,
        latitude: null,
        longitude: null,
        yourDestinations: null,
        publicDestinations: null,
        showCreateDestDialog: false,
        viewOption: "your",
        snackbarModel: {
          show: false,
          text: "",
          color: "error",
          duration: 3000,
          snackbarId: 1
        }
      };
    },
    mounted() {
      this.getYourDestinations();
    },
    methods: {
      /**
       * Sets the latitude and longitude coordinates to the given coordinates
       */
      addCoordinates(latitude, longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
      },
      refreshDestinations() {
        if (this.viewOption === "your") {
          this.getYourDestinations();
        } else {
          this.getPublicDestinations();
        }
      },
      /**
       * Gets destinations for the logged in user
       */
      async getYourDestinations() {
        try {
          const yourDestinations = await getYourDestinations();
          this.yourDestinations = yourDestinations;
        } catch (e) {
          console.log("Could not get your destinations");
        }
      },
      /**
       * Gets all public destinations
       */
      async getPublicDestinations() {
        try {
          this.publicDestinations = await getPublicDestinations();
        } catch (e) {
          console.log("Could not get public destinations");
        }
      },
      /**
       * Emit event from sidebar indicating that the user has swapped the type of
       * destinations to view
       */
      viewOptionChanged(viewOption) {
        this.viewOption = viewOption;
        // If user wants to load public destinations and they haven't been loaded, then load
        if (viewOption === "public" && !this.publicDestinations) {
          this.getPublicDestinations();
        }
      },
      /**
       * Shows create destination dialog
       */
      addDestinationClicked() {
        this.showCreateDestDialog = true;
      },
      /**
       * Add a new destination to the system. Push the undo/redo commands to the stack.
       * @param destination new destination.
       */
      addNewDestination(destination) {
        this.yourDestinations.push(destination);
        this.getYourDestinations();
        this.getPublicDestinations();

        const undoCommand = async (destination) => {
          try {
            await sendDeleteDestination(destination.destinationId);
            this.yourDestinations.splice(this.yourDestinations.indexOf(destination));
            this.getYourDestinations();
            this.getPublicDestinations();
          } catch (error) {
            this.snackbarModel.text = error.message;
            this.snackbarModel.color = "error";
            this.snackbarModel.show = true;
          }
        };

        const redoCommand = async (destination) => {
          try {
            await sendUndoDeleteDestination(destination.destinationId);
            this.yourDestinations.push(destination);
            this.getYourDestinations();
            this.getPublicDestinations();
            [].shift()
          } catch (error) {
            this.snackbarModel.text = error.message;
            this.snackbarModel.color = "error";
            this.snackbarModel.show = true;
          }
        };

        const updateDestCommand = new Command(undoCommand.bind(null, destination), redoCommand.bind(null, destination));
        this.$refs.sidebar.addUndoRedoCommand(updateDestCommand);
        this.showCreateDestDialog = false;

      },
      addDestDialogChanged(dialogValue) {
        this.resetCoordinates();
        this.showCreateDestDialog = dialogValue;
      },
      /**
       * Show either your destinations or public destinations depending on view option.
       * @returns {*}
       */
      getDestinationsCurrentlyViewing() {
        const destinations = this.viewOption === "your" ? this.yourDestinations : this.publicDestinations;
        if (!destinations) return [];
        return destinations;
      }
    }
  }
</script>

<style lang="scss" scoped>
  #destinations {
    width: 100%;
  }

  #map {
    width: calc(100% - 555px);
    display: inline-block;
    height: 100%;
    position: fixed;
  }

</style>




