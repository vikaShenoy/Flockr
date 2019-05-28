<template>
  <v-card elevation="10"
          class="destination-card row col-md-12">
    <div class="title-card">
      <h2 class="name-header">{{ destination.destinationName }}</h2>
      <div class="body-card col-md-12">
        <Carousel
                :photos="photos"
                :destinationId="destination.destinationId"
                v-if="photos"
                @displayError="displayMessage"
                @permissionUpdated="permissionUpdated"
                :hasOwnerRights="hasOwnerRights"
                @displayRemovePrompt="displayRemovePrompt"
                @addPhoto="addPhoto"
        />
      </div>
    </div>
    <div class="destination-content">
      <div class="row">
        <div class="col-md-6">
          <div class="row">
            <div class="basic-info-label"><p><b>Type</b></p></div>
            <div class="basic-info-label">{{ destination.destinationType.destinationTypeName }}</div>
          </div>
          <hr class="divider"/>
          <div class="row">
            <div class="basic-info-label"><p><b>District</b></p></div>
            <div class="basic-info-label">{{ destination.destinationDistrict.districtName }}</div>
          </div>
          <hr class="divider"/>
        </div>
        <div class="col-md-6">
          <h2 class="name-header">{{ destination.destinationCountry.countryName }}</h2>
          <v-img class="image"
                 src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </div>
      </div>
    </div>
    <v-btn v-if="hasOwnerRights" fab class="edit-button" id="edit-destination-button" @click="editDestination">
      <v-icon>edit</v-icon>
    </v-btn>
    <v-btn v-if="hasOwnerRights" fab class="delete-button" id="delete-destination-button" @click="deleteDestination">
      <v-icon>remove</v-icon>
    </v-btn>
  </v-card>
</template>

<script>

  import Carousel from "./Carousel/Carousel";
  import {getDestinationPhotos, removePhotoFromDestination} from "./DestinationCardService";
  import UserStore from "../../../stores/UserStore";

  export default {
    name: "destination-card",
    components: {
      Carousel
    },
    data() {
      return {
        photos: null,
        hasOwnerRights: false
      };
    },
    props: {
      destination: {
        destinationId: {
          type: Number,
          required: true
        },
        destinationName: {
          type: String,
          required: true
        },
        destinationType: {
          destinationTypeName: {
            type: String,
            required: true
          },
          destinationTypeId: {
            type: Number,
            required: true
          }
        },
        destinationDistrict: {
          districtName: {
            type: String,
            required: true
          },
          districtId: {
            type: Number,
            required: true
          }
        },
        destinationCountry: {
          countryName: {
            type: String,
            required: true
          },
          countryId: {
            type: Number,
            required: true
          }
        },
        destinationLat: {
          type: Number,
          required: true
        },
        destinationLon: {
          type: Number,
          required: true
        }
      }
    },
    async mounted() {
      try {
        this.photos = await getDestinationPhotos(this.destination.destinationId);
        this.hasOwnerRights = UserStore.methods.isAdmin() || this.destination.destinationOwner === Number(localStorage.getItem("userId"));
      } catch (error) {
        this.$emit("displayMessage", {
          text: error.message,
          color: "red"
        })
      }
    },
    methods: {
      /**
       * Called when the carousels emits an add photo event.
       * Adds the photo to the photos list.
       */
      addPhoto(photo) {
        this.photos.push(photo);
      },
      /**
       * Removes a photo at the given index from the photos array.
       *
       * @param index {Number} the index of the photo.
       */
      removePhoto(index) {
        this.photos.splice(index, 1);
      },
      /**
       * Called when the remove photo button is selected in the destination photo panel.
       * Compiles a callback function to do the following:
       *    - Send a request to the back end to remove the photo from the destination.
       *    - Remove the photo from the list of photos.
       *    - Close the photo panel.
       *    - Display any error or confirmation messages.
       * Then emits an event to display a confirmation message to the user and call the function on confirm.
       *
       * @param closeDialog {Function} closes the photo panel.
       * @param photoId {Number} the id of the photo to be removed.
       * @param index {Number} the index of the photo to be removed.
       */
      displayRemovePrompt(closeDialog, photoId, index) {
        const destinationId = this.destination.destinationId;
        const removePhoto = this.removePhoto;
        const displayMessage = this.displayMessage;
        const removeFunction = async function () {
          try {
            await removePhotoFromDestination(destinationId, photoId);
            removePhoto(index);
            closeDialog(false);
            displayMessage("The photo has been successfully removed.", "green");
          } catch (error) {
            displayMessage(error.message, "red");
          }
        };
        this.$emit("displayRemovePrompt", removeFunction);
      },
      /**
       * Called when the edit button is selected.
       * Emits an editDestination event.
       */
      editDestination() {
        this.$emit("editDestination");
      },
      /**
       * Called when the delete button is selected.
       * Emits a deleteDestination event.
       */
      deleteDestination() {
        this.$emit("deleteDestination");
      },
      /**
       * Called when the destination photo emits an error to display.
       * Emits the given error to it's parent to display.
       *
       * @param message {String} the error message
       * @param color {String} the color the message will be displayed as.
       */
      displayMessage(message, color) {
        this.$emit("displayMessage", {
          text: message,
          color: color
        });
      },
      /**
       * Called when the permission is updated for a photo.
       * Updates the isPublic field of the photo.
       * Displays a notification to the user.
       *
       * @param newValue {Boolean} the new value of isPrimary for the photo.
       * @param index {Number} the index of the photo.
       */
      permissionUpdated(newValue, index) {
        this.photos[index].isPublic = newValue;
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
  @import "../../../styles/_variables.scss";

  .divider {
    margin: 0 0 20px;
  }

  .image {
    margin: 0 0 20px;
  }

  .basic-info {
    text-align: center;
    width: 50%;
  }

  .basic-info-label {
    margin: 10px 0 10px;
    text-align: center;
    width: 50%;
  }

  .name-header {
    margin: 0 0 20px;
    text-align: center;
    width: 100%
  }

  .destination-card {
    width: 100%;
    margin: 10px 0 0;
    padding: 10px;
  }

  .title-card {
    width: 300px;
  }


  .destinations-panel :hover #save-destination-button {
    visibility: visible;
  }

  .destinations-panel :hover #delete-destination-button {
    visibility: visible;
  }

  .destinations-panel :hover #edit-destination-button {
    visibility: visible;
  }

  .destination-card #edit-destination-button {
    position: absolute;
    top: 30px;
    right: 30px;
    visibility: hidden;
  }

  .destination-card #delete-destination-button {
    position: absolute;
    bottom: 30px;
    right: 30px;
    visibility: hidden;
  }

  .destination-card #save-destination-button {
    position: absolute;
    top: 30px;
    right: 30px;
    visibility: hidden;
  }

  .carousel {
    max-height: 200px;
  }

  .destination-content {
    width: calc(100% - 300px);
  }

</style>