<template>
  <div id="destination">
    <div id="destination-map">
      <DestinationMap
        :destinations="destination ? [destination] : []"
        :destinationTitle="destination ? destination.destinationName : ''"
        shouldShowOverlay
       />
    </div>
    <div v-if="!destination" id="loading">
      <v-progress-circular
        indeterminate
        color="secondary"/>
    </div>

  <div v-if="destination">
    <ModifyDestinationDialog
      :dialog="showingEditDestDialog" 
      :destinationToEdit="destination"
      :editMode="true"
      v-on:dialogChanged="editDestDialogChanged"
      v-on:updateDestination="updateDestination"
      v-on:showError="showError"
    />
    <v-container grid-list-lg style="padding-top: 0px"> 
    <v-layout row wrap>
      <v-flex xs12 style="padding-bottom: 0px">
        <div style="float: right">
        <v-btn
          color="secondary"
          depressed
          @click="showingEditDestDialog = true"
        >
          <v-icon>edit</v-icon>
        </v-btn>

        <v-btn
          color="error"
          depressed
          @click="isShowingDeleteDestDialog = true"
        >
          <v-icon>delete</v-icon>
        </v-btn>
        </div>
      </v-flex>
      <v-flex xs12 sm6 lg4 xl4 style="padding-bottom: 0px">
        
        <Carousel
          v-if="destinationPhotos"
          :destinationPhotos="destinationPhotos"
          :destinationId="destination.destinationId"
          :hasOwnerRights="hasOwnerRights"
        /> 
      </v-flex>

      <v-flex xs12 sm6 lg8 xl8 style="padding-bottom: 0px">
        <DestinationDetails :destination="destination"/>
      </v-flex>
    </v-layout>
    </v-container>
  </div>

  <Snackbar
    :snackbarModel="snackbarModel"
    v-on:dismissSnackbar="dismissSnackbar"
     />

  <PromptDialog
    :dialog="isShowingDeleteDestDialog" 
    message="Are you sure you want to delete the destination?"
    :onConfirm="deleteDestination"
    v-on:promptEnded="isShowingDeleteDestDialog = false"
  />
  </div>
</template>

<script>
import { getDestination, getDestinationPhotos, deleteDestination } from "./DestinationService";
import ModifyDestinationDialog from "../Destinations/ModifyDestinationDialog/ModifyDestinationDialog";
import DestinationMap from "../../components/DestinationMap/DestinationMap";
import DestinationDetails from "./DestinationDetails/DestinationDetails";
import Carousel from "./Carousel/Carousel";
import PromptDialog from "../../components/PromptDialog/PromptDialog";
import Snackbar from "../../components/Snackbars/Snackbar";


export default {
  components: {
    DestinationMap,
    Carousel,
    DestinationDetails,
    ModifyDestinationDialog,
    Snackbar,
    PromptDialog
  },
  data() {
    return {
      destination: null,
      destinationPhotos: [],
      hasOwnerRights: false,
      showingEditDestDialog: false,
      isShowingDeleteDestDialog: false,
      snackbarModel: {
        show: false,
        timeout: 3000,
        text: "",
        color: null,
        snackbarId: 1
      }
    };
  },
  async mounted() {
    try {
      const destinationId = this.$route.params.destinationId;
      const destinationPromise = getDestination(destinationId);
      const destinationPhotosPromise = getDestinationPhotos(destinationId); 
      const [destination, destinationPhotos] = await Promise.all([destinationPromise, destinationPhotosPromise]);; 
      this.hasOwnerRights = destination.destinationOwner === Number(localStorage.getItem("userId"));
      this.destination = destination;
      this.destinationPhotos = destinationPhotos;
    } catch (e) {
      this.$router.go(-1);
   }
  },
  methods: {
    editDestDialogChanged(dialogValue) {
      this.showingEditDestDialog = dialogValue;
    },
    updateDestination(updatedDestination) {
      this.destination = updatedDestination;
      this.showingEditDestDialog = false;
      this.snackbarModel.color = "success";
      this.snackbarModel.text = "Updated destination";
      this.snackbarModel.show = true;
    },
    showError(errorMessage) {
      this.snackbarModel.text = errorMessage;
      this.snackbarModel.color = "error";
      this.snackbarModel.show = true; 
    },
    dismissSnackbar() {
      this.snackbarModel.show = false;
    },
    async deleteDestination() {
      const destinationId = this.$route.params.destinationId;
      try {
        await deleteDestination(destinationId);
        this.$router.push("/destinations");
      } catch (e) {
        console.log(e);
        this.showError("Could not delete destination");
      }
    } 
  }
}
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #destination-map {
    width: 100%;
    height: 30vh;
  }

  #loading {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
  }

  #destination {
    width: 100%; 
    background-color: #fafafa;
  }
</style>

