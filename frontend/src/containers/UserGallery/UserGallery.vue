<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-card id="user-gallery">
      <v-card-title>
        <v-spacer align="center">
          <h1>Photo Gallery</h1>
        </v-spacer>
      </v-card-title>
      <v-responsive>
        <v-container grid-list fluid >
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
            v-on:click="openPhotoDialog(photo, index)"
          >
            <v-img
              :src="photo.thumbEndpoint"
              aspect-ratio="1"
              class="grey lighten-2"
              >
              <template v-slot:placeholder>
                <v-layout
                  fill-height
                  align-center
                  justify-center
                  >
                    <v-progress-circular indeterminate color="grey lighten-5"></v-progress-circular>
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
        v-on:closeDialog="closePhotoDialog"
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
  import {getPhotosForUser, deleteUserPhoto} from "./UserGalleryService";
  import Snackbar from "../../components/Snackbars/Snackbar";
  import PromptDialog from "../../components/PromptDialog/PromptDialog";
  export default {

    name: "UserGallery",
    components: {PromptDialog, Snackbar, PhotoPanel},
    data() {
      return {
        userId: null,
        photos: [],
        allPhotos: [],
        currentPhoto: {
          thumbEndpoint: null,
          endpoint: null,
          primary: false,
          public: false,
          deleteFunction: null
        },
        currentPhotoIndex: null,
        showPhotoDialog: false,
        snackbarModel: {
          show: false, // whether the snackbar is currently shown or not
          timeout: 5000, // how long the snackbar will be shown for, it will not update the show property automatically though
          text: '', // the text to show in the snackbar
          color: '', // green, red, yellow, red, etc
          snackbarId: 0 // used to know which snackbar was manually dismissed
        },
        promptDialog: {
          show: false,
          message: "Are you sure you would like to delete this photo?",
          onConfirm: null
        }
      }
    },

    methods: {
      /**
       * Closes the photo dialog
       */
      closePhotoDialog(newValue) {
        this.showPhotoDialog = newValue;
        if (!newValue) {
          this.currentPhoto = {
            endpoint: null,
            primary: false,
            public: false
          };
        }
      },

      changedPermission(newValue, index) {
        this.photos[index].public = newValue;
      },

      /**
       * Sets the current photo to the one clicked on and opens the photo dialog
       *
       * @param photo the object containing the photo data
       */
      openPhotoDialog(photo, index) {
        this.currentPhoto = photo;
        this.showPhotoDialog = true;
        this.currentPhotoIndex = index;
      },

      /**
       * Called when the user selects the delete button on a photo.
       * Shows the prompt dialog and passes in the photo's delete function.
       */
      showPromptDialog(deleteFunction) {
        this.promptDialog.show = true;
        this.promptDialog.onConfirm = deleteFunction;
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
       */
      displayErrorMessage(message) {
        this.snackbarModel.text = message;
        this.snackbarModel.color = "red";
        this.snackbarModel.show = true;
      },

      /**
       * Gets a list of photos for the user from the server.
       */
      async getUsersPhotos() {
        try {
          this.allPhotos = await getPhotosForUser(this.userId);
          this.allPhotos.map(photo => {
            const displayErrorMessage = this.displayErrorMessage;
            photo.deleteFunction = async function () {
              try {
                await deleteUserPhoto(photo);
              } catch (error) {
                displayErrorMessage(error.message);
              }
            };
          })
        } catch (error) {
          console.log(error);
          this.displayErrorMessage(error.message);
        }
      }
    },

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
    h1{
      color: $primary;
    }
    width:100%;
    margin:1.5em;
  }

  .photo-tile {
    padding: 0.2em;
  }

  .photo-tile :hover {
    cursor: pointer;
  }

</style>