<template>
  <div>
    <div id="undo-redo">
      <UndoRedo ref="undoRedo" />
    </div>
    <h3 style="margin-bottom: 4px;margin-top: 4px;text-align:left; width: 50%">Photos</h3>

    <v-card id="photos">
      <v-card-actions>
        <v-spacer align="left">
          <v-btn v-if="photos.length" color="secondary" outline v-on:click="openGallery">View Gallery</v-btn>
        </v-spacer>
      </v-card-actions>
      <v-responsive>
        <v-container grid-list-sm fluid>
          <!-- users photos -->
          <v-layout v-if="photos && photos.length" row wrap>
            <v-flex sm12 md6 lg4 v-for="(photo, index) in photos" v-bind:key="photo.photoId">
              <v-img v-if="index < 6" class="photo clickable"
                     :src="thumbnailUrl(photo.photoId)"
                      @click="openViewPhotoDialog(photo, index)"/>
            </v-flex>
          </v-layout>

          <!-- default photos -->
          <v-layout v-else row wrap>
            <v-spacer align="center">
              <h3 class="font-weight-regular">This user has no photos.</h3>
            </v-spacer>
          </v-layout>

        </v-container>
      </v-responsive>

      <v-card-actions>
        <v-spacer align="center">
          <add-photo-dialog
                  :dialog="addPhotoDialog"
                  v-on:closeDialog="closeAddPhotoDialog"
                  v-on:addImage="addImage"
          ></add-photo-dialog>
        </v-spacer>
      </v-card-actions>
    </v-card>

    <photo-panel
            :photo="currentPhoto"
            :showDialog="viewPhotoDialog"
            :currentPhotoIndex="currentPhotoIndex"
            :deleteFunction="currentPhotoDeleteFunction"
            v-on:closeDialog="updatePhotoDialog"
            @deletePhoto="showPromptDialog"
            @displayErrorMessage="displayErrorMessage"
            @changedPermission="changedPermission"/>
    <snackbar :snackbarModel="this.snackbarModel" @dismissSnackbar="snackbarModel.show=false"/>
    <prompt-dialog
            :onConfirm="promptDialog.deleteFunction"
            :dialog="promptDialog.show"
            :message="promptDialog.message"
            @promptEnded="promptEnded"/>

  </div>
</template>

<script>
  import AddPhotoDialog from "./AddPhotoDialog/AddPhotoDialog";
  import { endpoint } from "../../../utils/endpoint";
  import PhotoPanel from "../../UserGallery/PhotoPanel/PhotoPanel";
  import PromptDialog from "../../../components/PromptDialog/PromptDialog";
  import Snackbar from "../../../components/Snackbars/Snackbar";
  import {deleteUserPhoto, undoDeleteUserPhoto} from "../../UserGallery/UserGalleryService";
  import UndoRedo from "../../../components/UndoRedo/UndoRedo";
  import Command from "../../../components/UndoRedo/Command";

  console.log(undoDeleteUserPhoto);

  export default {
    props: {
      photos: Array 
    },
    components: {
      Snackbar,
      PromptDialog,
      PhotoPanel,
      AddPhotoDialog,
      UndoRedo
    },

    data() {
      return {
        addPhotoDialog: false,
        userId: null,
        currentPhoto: null,
        currentPhotoIndex: null,
        currentPhotoDeleteFunction: null,
        viewPhotoDialog: false,
        hasPhotos: false,
        promptDialog: {
          show: false,
          deleteFunction: null,
          message: "Are you sure you would like to delete this photo?"
        },
        snackbarModel: {
          show: false,
          timeout: 5000,
          text: "",
          color: "green",
          snackbarId: 1
        }
      };
    },

    methods: {
      /**
       * Called when the show value in the photo dialog is changed.
       * Updates the showPhotoDialog boolean to match the photo dialog.
       * Sets the current photo data to default values if the dialog is being closed.
       *
       * @param newValue {Boolean} the new value of showPhotoDialog.
       */
      updatePhotoDialog(newValue) {
        this.viewPhotoDialog = newValue;
        if (!newValue) {
          this.currentPhoto = {
            endpoint: null,
            primary: false,
            public: false
          };
        }
      },
      /**
       * Called when the prompt dialog has finished.
       * Closes the prompt dialog and resets the values to defaults.
       */
      promptEnded() {
        this.promptDialog.deleteFunction = null;
        this.promptDialog.show = false;
      },
      /**
       * Displays an error message using the snack bar component.
       *
       * @param message {String} the text to be displayed.
       */
      displayErrorMessage(message) {
        this.snackbarModel.text = message;
        this.snackbarModel.color = "red";
        this.snackbarModel.show = true;
      },
      /**
       * Called when the permission of a photo is updated in the photo panel.
       * Updates the permission of the photo in the photos list to match.
       *
       * @param newValue {Boolean} the new value of public for this photo.
       * @param index {Number} the index of the photo in the photos list.
       */
      changedPermission(newValue, index) {
        this.photos[index].public = newValue;
      },
      /**
       * Creates and returns a delete function for the given photo and index.
       *
       * @param photo {Object} the photo to be deleted.
       * @param index {Number} the index of the photo in the photos list.
       */
      getDeleteFunction(photo, index) {
        return async () => {
          try {
            const undoCommand = async (index, photo) => {
              await undoDeleteUserPhoto(photo);
              this.$emit("undoDeletePhoto", index, photo);
            };

            const redoCommand = async (index, photo) => {
              await deleteUserPhoto(photo);
              this.$emit("deletePhoto", index);
            };

            await deleteUserPhoto(photo);
            this.updatePhotoDialog(false);
            this.$emit("deletePhoto", index, true);

            const deleteCommand = new Command(undoCommand.bind(null, index, photo), redoCommand.bind(null, index, photo));
            this.$refs.undoRedo.addUndo(deleteCommand);
            
          } catch (error) {
            this.$emit("showError", error.message);
          }
        };
      },
      /**
       * Called when the user selects the delete button on a photo.
       * Shows the prompt dialog and passes in the photo's delete function.
       *
       * @param onConfirm {Function} the function to call on confirmation
       */
      showPromptDialog(onConfirm) {
        this.promptDialog.show = true;
        this.promptDialog.deleteFunction = onConfirm;
      },
      /**
       * Called when a photo is clicked on.
       * Sets the current photo as the one clicked on.
       * Opens the view photo dialog by setting viewPhotoDialog to true
       *
       * @param photo {Object} the photo clicked on by the user
       * @param index {Number} the index of the photo
       */
      openViewPhotoDialog: function (photo, index) {
        this.currentPhoto = photo;
        this.currentPhotoIndex = index;
        this.currentPhotoDeleteFunction = this.getDeleteFunction(photo, index);
        this.viewPhotoDialog = true;
      },
      /**
       * Called when the add photo dialog is closed by the user.
       * Closes the add photo dialog by setting addPhotoDialog to false.
       */
      closeAddPhotoDialog: function () {
        this.addPhotoDialog = false;
      },
      /**
       * Called when the view gallery button is clicked.
       * Directs user to the photos page for this user.
       */
      openGallery() {
        this.$router.push(`/profile/${this.userId}/photos`);
      },
      thumbnailUrl(photoId) {
        return endpoint(`/users/photos/${photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
      },
      addImage(image) {
        this.$emit("addImage", image);
      }
    },

    mounted: async function () {
      this.userId = this.$route.params.id;
    }
  }
</script>

<style lang="scss" scoped>

  #photos {
    padding: 20px;
  }

  .photo {
    width: 100%;
    height: auto;
  }

  .clickable :hover {
    cursor: pointer;
  }

  #undo-redo {
    float: right;
  }

</style>