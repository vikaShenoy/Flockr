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

        <div id="undo-redo-btns">
          <UndoRedo ref="undoRedo" />
        </div>

        <v-btn
          color="secondary" 
          depressed          
          v-if="destination.isPublic"
          @click="isShowingTravellerTypesDialog = true"
        >
         Request Traveller Types 
        </v-btn>


 

        <v-btn
          color="secondary"
          depressed
          @click="showingEditDestDialog = true"
          v-if="userStore.methods.isAdmin() || destination.destinationOwner === userStore.data.userId"
        >
          <v-icon>edit</v-icon>
        </v-btn>


       </div>
      </v-flex>
      <v-flex xs12 sm6 lg4 xl4 style="padding-bottom: 0px">
        
        <Carousel
          v-if="destinationPhotos"
          :destinationPhotos="destinationPhotos"
          :destinationId="destination.destinationId"
          :hasOwnerRights="hasOwnerRights"
          v-on:addPhoto="addPhoto"
          v-on:displayRemovePrompt="displayRemovePrompt"
          v-on:permissionUpdated="permissionUpdated"
        /> 
      </v-flex>


      <v-flex xs12 sm6 lg8 xl8 style="padding-bottom: 0px">
        <DestinationDetails
          :destination="destination"
        />
      </v-flex>
    </v-layout>
    </v-container>
  </div>

  <Snackbar
    :snackbarModel="snackbarModel"
     v-on:dismissSnackbar="dismissSnackbar"
     />

  <RequestTravellerTypes 
    :isShowingTravellerTypesDialog.sync="isShowingTravellerTypesDialog" 
    :destination="destination"
    v-on:sendingProposal="sendingProposal"
    v-on:showError="showError"
  />

  <prompt-dialog
      :onConfirm="promptDialog.deleteFunction"
      :dialog="promptDialog.show"
      :message="promptDialog.message"
      @promptEnded="promptEnded"/>

  </div>
</template>

<script>
import Command from "../../components/UndoRedo/Command";
import { getDestination, getDestinationPhotos, deleteDestination, removePhotoFromDestination, undoRemovePhotoFromDestination, rejectProposal, undeleteProposal } from "./DestinationService";
import ModifyDestinationDialog from "../Destinations/ModifyDestinationDialog/ModifyDestinationDialog";
import DestinationMap from "../../components/DestinationMap/DestinationMap";
import DestinationDetails from "./DestinationDetails/DestinationDetails";
import Carousel from "./Carousel/Carousel";
import PromptDialog from "../../components/PromptDialog/PromptDialog";
import Snackbar from "../../components/Snackbars/Snackbar";
import RequestTravellerTypes from "./RequestTravellerTypes/RequestTravellerTypes";
import { sendUpdateDestination } from "../Destinations/DestinationsService";
import UserStore from '../../stores/UserStore';
import UndoRedo from "../../components/UndoRedo/UndoRedo"
import { sendProposal } from "./RequestTravellerTypes/RequestTravellerTypesService";
import superagent from 'superagent';
import {endpoint} from '../../utils/endpoint';


export default {
  components: {
    DestinationMap,
    Carousel,
    DestinationDetails,
    ModifyDestinationDialog,
    Snackbar,
    PromptDialog,
    RequestTravellerTypes,
    UndoRedo
  },
  data() {
    return {
      userStore: UserStore,
      destination: null,
      destinationPhotos: [],
      hasOwnerRights: false,
      showingEditDestDialog: false,
      snackbarModel: {
        show: false,
        timeout: 3000,
        text: "",
        color: null,
        snackbarId: 1
      },
      isShowingTravellerTypesDialog: false,
      promptDialog: {
        show: false,
        message: "",
        deleteFunction: null
      },
      index: null,
    };
  },
  async mounted() {
    try {
      const destinationId = this.$route.params.destinationId;
      const destinationPromise = getDestination(destinationId);
      const destinationPhotosPromise = getDestinationPhotos(destinationId); 
      const [destination, destinationPhotos] = await Promise.all([destinationPromise, destinationPhotosPromise]);
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
      

      const undoCommand = async (destination) => {
        await sendUpdateDestination(destination, this.destination.destinationId)
        this.destination = destination;
      };

      const redoCommand = async (destination) => {
        await sendUpdateDestination(destination, updatedDestination.destinationId);
        this.destination = destination;
      };

      const updateDestCommand = new Command(undoCommand.bind(null, this.destination), redoCommand.bind(null, updatedDestination));

      this.$refs.undoRedo.addUndo(updateDestCommand);
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
    async sendingProposal(travellerTypeIds) {
      const proposal = await sendProposal(this.destination.destinationId, travellerTypeIds);

      const undoCommand = async (destinationProposalId) => {
        await rejectProposal(destinationProposalId);
      };

      const redoCommand = async (destinationProposalId) => {
        await undeleteProposal(destinationProposalId);
      };

      const sendProposalCommand = new Command(undoCommand.bind(null, proposal.destinationProposalId), redoCommand.bind(null, proposal.destinationProposalId));
      this.$refs.undoRedo.addUndo(sendProposalCommand);

      this.snackbarModel.color = "success";
      this.snackbarModel.text = "Proposal Sent";
      this.snackbarModel.show = true;
    },
    /**
     * Adds the photo to a destination with undo and redo functionalities
    */
    async addPhoto(photoId) {
      let data = {
        photoId: photoId
      };
      let authToken = localStorage.getItem('authToken');
      const res = await superagent.post(endpoint(`/destinations/${this.destination.destinationId}/photos`))
        .set('Authorization', authToken)
        .send(data);
      let photo = res.body;
      photo["endpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}?Authorization=${localStorage.getItem("authToken")}`);
      photo["thumbEndpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);

      const undoCommand = async () => {
        await removePhotoFromDestination(this.destination.destinationId, photoId);
        this.removePhoto(this.index);
      };

      const redoCommand = async () => {
        console.log("Redo", photoId);
        const res = await superagent.post(endpoint(`/destinations/${this.destination.destinationId}/photos`))
          .set('Authorization', authToken)
          .send(data);
        let photo = res.body;
        photo["endpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}?Authorization=${localStorage.getItem("authToken")}`);
        photo["thumbEndpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
        this.destinationPhotos.push(photo)
      };

      const addDestinationPhotoCommand = new Command(undoCommand.bind(null, photoId), redoCommand.bind(null, photoId));
      this.$refs.undoRedo.addUndo(addDestinationPhotoCommand);
      this.destinationPhotos.push(photo)
    },
    /**
     * Displays a message using the snackbar.
     */
    displayMessage(text, color) {
      this.snackbarModel.text = text;
      this.snackbarModel.color = color;
      this.snackbarModel.show = true;
    },
    displayRemovePrompt(closeDialog, photoId, index) {
      const destinationId = this.destination.destinationId;
      const removePhoto = this.removePhoto;
      const displayMessage = this.displayMessage;
      this.index = index;
      const removeFunction = async function () {
        try {
          await removePhotoFromDestination(destinationId, photoId);
          removePhoto(index);
          closeDialog(false);
          displayMessage("The photo has been successfully removed.", "green");

          const undoCommand = async (destinationId, photoId) => {
            console.log("undo dest", destinationId);
            console.log("undo photo", photoId);
            await undoRemovePhotoFromDestination(destinationId, photoId);
          };

          const redoCommand = async (destinationId, photoId, index) => {
            console.log("redo dest", destinationId);
            console.log("redo photo", photoId);
            await removePhotoFromDestination(destinationId, photoId);
            removePhoto(index);
            closeDialog(false);
            displayMessage("The photo has been successfully removed.", "green");

          };

          const removeDestinationPhotoCommand = new Command(undoCommand.bind(null, destinationId, photoId, index), redoCommand.bind(null, destinationId, photoId, index));
          this.$refs.undoRedo.addUndo(removeDestinationPhotoCommand);
        } catch (error) {
          console.log(error.message);
          console.log(error);
          displayMessage(error.message, "red");
        }
      };

      this.promptDialog.deleteFunction = removeFunction;
      this.promptDialog.message = 'Are you sure that you would like to delete this photos?';
      this.promptDialog.show = true;
    },
    /**
     * Removes a photo at the given index from the photos array.
     *
     * @param index {Number} the index of the photo.
     */
    removePhoto(index) {
      this.destinationPhotos.splice(index, 1);
    },
    async deleteDestination() {
      const destinationId = this.$route.params.destinationId;
      try {
        await deleteDestination(destinationId);
        this.$router.push("/destinations");
      } catch (e) {
        this.showError("Could not delete destination");
      }
    },
    /* Called when the prompt dialog has finished.
      * Closes the prompt dialog and resets the values to defaults.
    */
    promptEnded() {
      this.promptDialog.deleteFunction = null;
      this.promptDialog.show = false;
    },

    permissionUpdated(newValue, index) {
      this.destinationPhotos[index].personalPhoto.isPublic = newValue;
      if (newValue) {
        this.displayMessage("This photo is now public", "green");
      } else {
        this.displayMessage("This photo is now private", "green");
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

  #undo-redo-btns {
    float: left;
    margin-top: 10px;
  }
</style>


