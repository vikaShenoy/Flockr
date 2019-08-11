<template>
  <div id="destination">
    <div id="destination-map">
      <DestinationMap
              :destinations="destination ? [destination] : []"
              :destinationTitle="destination ? destination.destinationName : ''"
              :destinationPhotos="destinationPhotos"
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
              @dialogChanged="editDestDialogChanged"
              @updateDestination="updateDestination"
              @showError="showError"
      />
      <v-container grid-list-lg style="padding-top: 0px">
        <v-layout row wrap>
          <v-flex xs12 style="padding-bottom: 0px">

            <div style="float: right">

              <div id="undo-redo-btns">
                <UndoRedo ref="undoRedo"/>
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
                    @addPhoto="addPhoto"
                    @displayRemovePrompt="displayRemovePrompt"
                    @permissionUpdated="permissionUpdated"
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

    <RequestTravellerTypes
            :isShowingTravellerTypesDialog.sync="isShowingTravellerTypesDialog"
            :destination="destination"
            @sendingProposal="sendingProposal"
            @showError="showError"
            v-if="destination"
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
  import {
    getDestination,
    getDestinationPhotos,
    rejectProposal,
    removePhotoFromDestination,
    undeleteProposal,
    undoRemovePhotoFromDestination
  } from "./DestinationService";
  import ModifyDestinationDialog from "../Destinations/ModifyDestinationDialog/ModifyDestinationDialog";
  import DestinationMap from "../../components/DestinationMap/DestinationMap";
  import DestinationDetails from "./DestinationDetails/DestinationDetails";
  import Carousel from "./Carousel/Carousel";
  import PromptDialog from "../../components/PromptDialog/PromptDialog";
  import RequestTravellerTypes from "./RequestTravellerTypes/RequestTravellerTypes";
  import {sendUpdateDestination} from "../Destinations/DestinationsService";
  import UserStore from '../../stores/UserStore';
  import UndoRedo from "../../components/UndoRedo/UndoRedo"
  import {sendProposal} from "./RequestTravellerTypes/RequestTravellerTypesService";
  import superagent from 'superagent';
  import {endpoint} from '../../utils/endpoint';
  import {deleteDestination} from "../Destinations/DestinationSidebar/DestinationSidebarService";


  export default {
    components: {
      DestinationMap,
      Carousel,
      DestinationDetails,
      ModifyDestinationDialog,
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
      /**
       * Update a destination. Add the undo command to the undo/redo stack.
       * @param updatedDestination updated destination object.
       */
      updateDestination(updatedDestination) {
        const undoCommand = async (destination) => {
          await sendUpdateDestination(destination, this.destination.destinationId);
          this.destination = destination;
        };

        const redoCommand = async (destination) => {
          await sendUpdateDestination(destination, updatedDestination.destinationId);
          this.destination = destination;
        };

        const updateDestCommand = new Command(undoCommand.bind(null, this.destination),
            redoCommand.bind(null, updatedDestination));

        this.$refs.undoRedo.addUndo(updateDestCommand);
        this.destination = updatedDestination;
        this.showingEditDestDialog = false;
        this.showSnackbar("Updated the destination", "success", 3500)
      },
      showError(errorMessage) {
        this.showSnackbar(errorMessage, "error", 3000);
      },
      /**
       * Send a proposal for traveller types for a destination.
       * Add the undo/redo commands to the undo/redo stack.
       * @param travellerTypeIds ids to be proposed for the destination.
       */
      async sendingProposal(travellerTypeIds) {
        const proposal = await sendProposal(this.destination.destinationId, travellerTypeIds);

        const undoCommand = async (destinationProposalId) => {
          await rejectProposal(destinationProposalId);
        };

        const redoCommand = async (destinationProposalId) => {
          await undeleteProposal(destinationProposalId);
        };

        const sendProposalCommand = new Command(undoCommand.bind(null, proposal.destinationProposalId),
            redoCommand.bind(null, proposal.destinationProposalId));
        this.$refs.undoRedo.addUndo(sendProposalCommand);

        this.showSnackbar("Proposal sent", "success", 2500);
      },
      /**
       * Adds the photo to a destination with undo and redo functionality.
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
        photo["endpoint"] = endpoint(
            `/users/photos/${photo.personalPhoto.photoId}?Authorization=${localStorage.getItem("authToken")}`);
        photo["thumbEndpoint"] = endpoint(
            `/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);

        const destinationPhotoId = photo.destinationPhotoId;
        const photoIndex = this.destinationPhotos.length;

        const undoCommand = async () => {
          await removePhotoFromDestination(this.destination.destinationId, destinationPhotoId);
          this.removePhoto(photoIndex);
        };

        const redoCommand = async () => {
          await undoRemovePhotoFromDestination(this.destination.destinationId, destinationPhotoId);
          this.addPhotoToDisplay(photoIndex, photo);
        };

        const addDestinationPhotoCommand = new Command(
            undoCommand.bind(null, destinationPhotoId, photoIndex),
            redoCommand.bind(null, destinationPhotoId, photoIndex));
        this.$refs.undoRedo.addUndo(addDestinationPhotoCommand);
        this.destinationPhotos.push(photo)
      },
      /**
       * @param {String} message the message to show in the snackbar
       * @param {String} color the colour for the snackbar
       * @param {Number} the amount of time (in ms) for which we show the snackbar
       */
      showSnackbar(message, color, timeout) {
        window.$vue.$emit({
          message: message,
          color: color,
          timeout: timeout
        });
      },

      /**
       * Delete a photo from a destination. Add undo/redo commands to the stack.
       * @param closeDialog flag to close the popup
       * @param photoId id of the photo to delete
       * @param index index of the photo in the list of destination photos.
       */
      displayRemovePrompt(closeDialog, photoId, index) {
        const destinationId = this.destination.destinationId;
        const removePhoto = this.removePhoto;
        const addPhotoToDisplay = this.addPhotoToDisplay;
        this.index = index;
        const removeFunction = async () => {
          try {
            const photoData = this.destinationPhotos[index];
            const destinationPhotoId = photoData.destinationPhotoId;

            const undoCommand = async (destinationId, destinationPhotoId, index, photoData) => {
              await undoRemovePhotoFromDestination(destinationId, destinationPhotoId);
              addPhotoToDisplay(index, photoData);
            };

            const redoCommand = async (destinationId, destinationPhotoId, index) => {
              await removePhotoFromDestination(destinationId, destinationPhotoId);
              removePhoto(index);
            };

            const removeDestinationPhotoCommand = new Command(
                undoCommand.bind(null, destinationId, destinationPhotoId, index, photoData),
                redoCommand.bind(null, destinationId, destinationPhotoId, index));
            this.$refs.undoRedo.addUndo(removeDestinationPhotoCommand);

            await removePhotoFromDestination(destinationId, destinationPhotoId);
            removePhoto(index);
            closeDialog(false);
            this.showSnackbar('The photo was removed', 'success', 3000);

          } catch (error) {
            this.showSnackbar('Could not remove the photo', 'error', 3000);
          }
        };

        this.promptDialog.deleteFunction = removeFunction;
        this.promptDialog.message = 'Are you sure that you would like to delete this photos?';
        this.promptDialog.show = true;
      },
      /**
       * Removes a photo at the given index from the photos array.
       * @param index {Number} the index of the photo.
       */
      removePhoto(index) {
        this.destinationPhotos.splice(index, 1);
      },

      /**
       * Delete a destination. Route the user back to the main destinations page.
       */
      async deleteDestination() {
        const destinationId = this.$route.params.destinationId;
        try {
          await deleteDestination(destinationId);
          this.$router.push("/destinations");
        } catch (e) {
          this.showSnackbar('Could not delete the destination', 'error', 3000);
        }
      },

      addPhotoToDisplay(index, photo) {
        this.destinationPhotos.splice(index, 0, photo);
      },

      /* Called when the prompt dialog has finished.
        * Closes the prompt dialog and resets the values to defaults.
      */
      promptEnded() {
        this.promptDialog.deleteFunction = null;
        this.promptDialog.show = false;
      },

      /**
       * Called when the user updates the photo permissions on one of their destination photos.
       * @param newValue updated permission value
       * @param index index of the photo in the destination photos list.
       */
      permissionUpdated(newValue, index) {
        this.destinationPhotos[index].personalPhoto.isPublic = newValue;
        if (newValue) {
          this.showSnackbar("This photo is now public", "success", 2500);
        } else {
          this.showSnackbar("This photo is now public", "success", 2500);
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


