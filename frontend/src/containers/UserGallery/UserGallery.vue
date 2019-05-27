<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <v-card id="user-gallery">
    <v-card-title>
      <v-spacer align="center">
        <h1>Photo Gallery</h1>
      </v-spacer>
    </v-card-title>
    <v-responsive>
      <v-container grid-list fluid>
        <v-layout row wrap>
          <v-flex
                  v-for="(photo, index) in photos"
                  :key="photo.filename"
                  d-flex
                  lg3
                  md4
                  sm6
                  xs12
                  class="photo-tile"
                  v-on:click="openPhotoDialog(photo, index)">
            <v-img
                    :src="photo.thumbEndpoint"
                    aspect-ratio="1"
                    class="grey lighten-2">
              <template v-slot:placeholder>
                <v-layout
                        fill-height
                        align-center
                        justify-center>
                  <v-progress-circular
                          indeterminate
                          color="grey lighten-5"/>
                </v-layout>
              </template>
            </v-img>
          </v-flex>
        </v-layout>
      </v-container>
    </v-responsive>
    <photo-panel
            :photo="currentPhoto"
            :showDialog="showPhotoDialog"
            v-on:closeDialog="updatePhotoDialog"
            @deletePhoto="showPromptDialog"
            :currentPhotoIndex="currentPhotoIndex"
            @displayErrorMessage="displayErrorMessage"
            @changedPermission="changedPermission"/>
    <snackbar :snackbarModel="this.snackbarModel" @dismissSnackbar="snackbarModel.show=false"/>
    <prompt-dialog
            :dialog="promptDialog.show"
            :message="promptDialog.message"
            :onConfirm="promptDialog.onConfirm"
            @promptEnded="promptDialog.show=false"
            @displayError="displayErrorMessage"/>
  </v-card>
</template>

<script>

  import PhotoPanel from "./PhotoPanel/PhotoPanel";
  import {deleteUserPhoto, getPhotosForUser} from "./UserGalleryService";
  import Snackbar from "../../components/Snackbars/Snackbar";
  import PromptDialog from "../../components/PromptDialog/PromptDialog";

  export default {

    name: "UserGallery",
    components: {PromptDialog, Snackbar, PhotoPanel},
    data() {
      return {
        userId: null,
        photos: [],                         // The photos currently being displayed.
        allPhotos: [],                      // The photos not yet displayed. Added when the user nears the bottom of the page.
        currentPhoto: {
          thumbEndpoint: null,
          endpoint: null,
          primary: false,
          public: false,
          deleteFunction: null
        },                // The photo being displayed in the photo panel, resets to default values when the panel is closed.
        currentPhotoIndex: null,            // The index in the photos list of the current photo being displayed in the photo panel.
        showPhotoDialog: false,             // Whether or not to show the photo panel dialog.
        snackbarModel: {
          show: false, // whether the snackbar is currently shown or not
          timeout: 5000, // how long the snackbar will be shown for, it will not update the show property automatically though
          text: '', // the text to show in the snackbar
          color: '', // green, red, yellow, red, etc
          snackbarId: 0 // used to know which snackbar was manually dismissed
        },       // The snack bar variables
        promptDialog: {
          show: false,
          onConfirm: null,
          message: "Are you sure you would like to delete this photo?"
        }     // the prompt dialog variables
      }
    },

    /**
     * A list of methods to be used in this page.
     */
    methods: {
      /**
       * Called when the show value in the photo dialog is changed.
       * Updates the showPhotoDialog boolean to match the photo dialog.
       * Sets the current photo data to default values if the dialog is being closed.
       *
       * @param newValue {Boolean} the new value of showPhotoDialog.
       */
      updatePhotoDialog(newValue) {
        this.showPhotoDialog = newValue;
        if (!newValue) {
          this.currentPhoto = {
            endpoint: null,
            primary: false,
            public: false
          };
        }
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
       * Sets the current photo to the one clicked on and opens the photo dialog
       *
       * @param photo {POJO} the object containing the photo data
       * @param index {Number} the index of the photo in the photos list.
       */
      openPhotoDialog(photo, index) {
        this.currentPhoto = photo;
        this.showPhotoDialog = true;
        this.currentPhotoIndex = index;
      },
      /**
       * Called when the user selects the delete button on a photo.
       * Shows the prompt dialog and passes in the photo's delete function.
       *
       * @param onConfirm {Function} the function to call on confirmation
       */
      showPromptDialog(onConfirm) {
        this.promptDialog.show = true;
        this.promptDialog.onConfirm = onConfirm;
      },
      /**
       * Called when scrolling reaches near the bottom of the page.
       * Adds another 12 photos to the photos list or adds the rest of the photos if there is less than 12 left.
       */
      getMorePhotos() {
        this.photos = this.photos.concat(this.allPhotos.slice(0, 12));
        this.allPhotos = this.allPhotos.slice(12);
      },
      /**
       * Sets the event listener for scrolling near the bottom of the page and adds more photos if available.
       */
      setScroll() {
        window.onscroll = () => {
          let atBottom = document.documentElement.scrollTop + window.innerHeight >= document.documentElement.offsetHeight * 0.8;

          if (atBottom) {
            this.getMorePhotos();
          }
        }
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
       * Called after a photo is successfully deleted.
       * Displays a success message, removes the photo from the photo list and closes the photo dialog.
       *
       * @param index {Number} the index of the photo to be removed.
       */
      afterDelete(index) {
        this.photos.splice(index, 1);
        this.updatePhotoDialog(false);
        this.snackbarModel.text = "Photo Successfully Deleted.";
        this.snackbarModel.color = "green";
        this.snackbarModel.show = true;
      },
      /**
       * Gets a list of photos for the user from the server.
       */
      async getUsersPhotos() {
        try {
          this.allPhotos = await getPhotosForUser(this.userId);
          this.allPhotos.map((photo, index) => {
            const displayErrorMessage = this.displayErrorMessage;
            const afterDelete = this.afterDelete;
            photo.deleteFunction = async function () {
              try {
                await deleteUserPhoto(photo);
                afterDelete(index);
              } catch (error) {
                displayErrorMessage(error.message);
              }
            };
          })
        } catch (error) {
          this.displayErrorMessage(error.message);
        }
      }
    },

    /**
     * Called once the html has been parsed.
     * Sets up the data for the page to display by retrieving it from the server.
     */
    mounted: async function () {
      this.userId = parseInt(this.$route.params.id);
      await this.getUsersPhotos();
      this.getMorePhotos();
      this.setScroll();
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #user-gallery {
    h1 {
      color: $primary;
    }

    width: 100%;
    margin: 1.5em;
  }

  .photo-tile {
    padding: 0.2em;
  }

  .photo-tile :hover {
    cursor: pointer;
  }

</style>