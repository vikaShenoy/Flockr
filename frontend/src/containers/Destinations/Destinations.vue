<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap 
        :destinations="getDestinationsCurrentlyViewing()"
      />
    </div>

    <DestinationSidebar 
      :viewOption="viewOption"
      :yourDestinations="yourDestinations"
      :publicDestinations="publicDestinations"
      v-on:viewOptionChanged="viewOptionChanged"
      v-on:addDestinationClicked="addDestinationClicked"
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
import { getYourDestinations, getPublicDestinations } from "./DestinationsService";

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
      viewOption: "your"
    };
  },
  mounted() {
    this.getYourDestinations();
  },
  methods: {
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
        const publicDestinations = await getPublicDestinations();
        this.publicDestinations = publicDestinations;
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
        console.log("Did I make it here");
        this.getPublicDestinations();
      }
    },
    /**
     * Shows create destination dialog
     */
    addDestinationClicked() {
      this.showCreateDestDialog = true;      
    },
    addNewDestination(destination) {
      this.yourDestinations.push(destination); 
    },
    addDestDialogChanged(dialogValue) {
      this.showCreateDestDialog = dialogValue; 
    },
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
    width: calc(100% - 300px);
    display: inline-block;
    height: 100%;
    position: fixed;
  }

</style>




