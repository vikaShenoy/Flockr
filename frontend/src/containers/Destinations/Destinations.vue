<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap
        :destinations="getDestinationsCurrentlyViewing()"
        :panOn="panOn"
      />
    </div>

    <DestinationSidebar
      :viewOption="viewOption"
      :yourDestinations="yourDestinations"
      :publicDestinations="publicDestinations"
      v-on:viewOptionChanged="viewOptionChanged"
      v-on:addDestinationClicked="addDestinationClicked"
      v-on:setPan="setPan"
      @refreshDestinations="refreshDestinations"
      ref="sidebar"
    />

    <ModifyDestinationDialog
      :dialog="showCreateDestDialog"
      :editMode="false"
      v-on:addNewDestination="addNewDestination"
      v-on:dialogChanged="addDestDialogChanged"
    >

    </ModifyDestinationDialog>

  </div>
</template>

<script>
  import DestinationSidebar from "./DestinationSidebar/DestinationSidebar";
  import DestinationMap from "../../components/DestinationMap/DestinationMap";
  import ModifyDestinationDialog from "./ModifyDestinationDialog/ModifyDestinationDialog";
  import {
    getPublicDestinations,
    getYourDestinations,
    sendDeleteDestination,
    sendUndoDeleteDestination
  } from "./DestinationsService";
  import Command from "../../components/UndoRedo/Command";

  export default {
    components: {
      DestinationSidebar,
      DestinationMap,
      ModifyDestinationDialog
    },
    data() {
      return {
        yourDestinations: null,
        publicDestinations: null,
        showCreateDestDialog: false,
        viewOption: "your",
        panOn: false
      };
    },
    mounted() {
      this.getYourDestinations();
    },
    methods: {
      /**
       * Sets the pan on to the emitted value.
       * True if the pan is on, false if the pan is off.
       */
      setPan(panOn) {
        this.panOn = panOn;
      },
      /**
       * Refreshes the sidebar when the button is toggled from your destinations and public destinations and vice versa.
       */
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
          this.showSnackbar("Could not get your destinations", "error", 3000);
        }
      },
      /**
       * Gets all public destinations
       */
      async getPublicDestinations() {
        try {
          this.publicDestinations = await getPublicDestinations();
        } catch (e) {
          this.showSnackbar("Could not get public destinations", "error", 3000);
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
       * @param {String} message the message to show in the snackbar
       * @param {String} color the colour for the snackbar
       * @param {Number} the amount of time (in ms) for which we show the snackbar
       */
      showSnackbar(message, color, timeout) {
        this.$root.$emit({
          message: message,
          color: color,
          timeout: timeout
        });
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
            this.showSnackbar("Could not undo the action", "error", 3000);
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
            this.showSnackbar("Could not redo the action", "error", 3000);
          }
        };

        const updateDestCommand = new Command(undoCommand.bind(null, destination), redoCommand.bind(null, destination));
        this.$refs.sidebar.addUndoRedoCommand(updateDestCommand);
        this.showCreateDestDialog = false;

      },
      addDestDialogChanged(dialogValue) {
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
    display: flex;
    width: 100%;
    height: 100%;

    #map {
      flex-grow: 1;
      height: 100%;
    }
  }

</style>




