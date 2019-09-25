<template>
  <div>
    <v-carousel class="carousel" :cycle="false">
      <v-carousel-item v-for="(photo, index) in destinationPhotos" :key="photo.photoId" lazy>
        <v-img
          :src="photo.endpoint"
          class="dest-image"
          alt="Some Image"
          @click="openPhotoDialog(photo, index)"
        />
      </v-carousel-item>

      <v-img
        v-if="destinationPhotos.length === 0"
        class="dest-image"
        :src="defaultDestinationPhoto"
      />

      <v-btn
        class="carousel"
        id="add-button"
        color="blue-grey darken-3"
        fab
        @click="openAddPhotoDialog()"
      >
        <v-icon>add</v-icon>
      </v-btn>
    </v-carousel>

    <destination-photo-panel
      :photo="currentPhoto"
      :showDialog="showPhotoDialog"
      :hasOwnerRights="hasOwnerRights"
      @closeDialog="closePhotoPanel"
      @displayError="displayError"
      @permissionUpdated="permissionUpdated"
      @displayRemovePrompt="displayRemovePrompt"
    />
    <AddPhotoDialog
      :destinationPhotos="destinationPhotos"
      :destinationId="destinationId"
      :showDialog="showAddPhotoDialog"
      :userPhotos="userPhotos"
      @closeAddPhotoDialog="closeAddPhotoDialogHandler"
      @addPhoto="addPhoto"
    />
  </div>
</template>

<script>
import DestinationPhotoPanel from "./DestinationPhotoPanel/DestinationPhotoPanel";
import AddPhotoDialog from "./AddDestinationPhotoDialog/AddDestinationPhotoDialog";
import defaultDestinationPhoto from "./defaultDestinationPhoto.png";
import * as superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";

export default {
  components: { AddPhotoDialog, DestinationPhotoPanel },
  props: {
    destinationPhotos: Array,
    destinationId: Number,
    hasOwnerRights: {
      type: Boolean,
      required: false
    }
  },
  data() {
    return {
      showPhotoDialog: false,
      currentPhoto: null,
      currentPhotoIndex: null,
      addPhotoButton: false,
      inButton: false,
      showAddPhotoDialog: false,
      defaultDestinationPhoto: defaultDestinationPhoto,
      userPhotos: []
    };
  },
  mounted() {
    this.getUserPhotos();
  },
  methods: {
    /**
     * Get all of the user's photos.
     * Emit a call to parent on error.
     */
    getUserPhotos: async function() {
      let authToken = localStorage.getItem("authToken");
      let userFromUrl = this.$route.params.userId;
      let userId = userFromUrl ? userFromUrl : localStorage.getItem("userId");
      try {
        this.userPhotos = [];
        const response = await superagent(
          endpoint(`/users/${userId}/photos`)
        ).set("Authorization", authToken);
        let userPhotos = response.body;
        this.userPhotos = userPhotos.filter(userPhoto => {
          for (const destinationPhoto of this.destinationPhotos) {
            if (userPhoto.photoId === destinationPhoto.personalPhoto.photoId) {
              return false;
            }
          }
          return true;
        });
      } catch (e) {
        this.$emit("displayError", e.message);
      }
    },
    /**
     * Called when a photo is added by the user.
     * emits an event to the parent element to add the photo to the list.
     */
    addPhoto(photo) {
      this.$emit("addPhoto", photo);
      this.getUserPhotos();
    },
    /**
     * Called when the photo is selected.
     * Opens the photo dialog and sets the current photo values.
     *
     * @param photo {POJO} the photo object to be displayed in the dialog.
     * @param index {Number} the index of the photo.
     */
    openPhotoDialog(photo, index) {
      if (!this.inButton) {
        this.showPhotoDialog = true;
        this.currentPhoto = photo;
        this.currentPhotoIndex = index;
      }
    },
    /**
     * Called when the photo dialog emits a closeDialog event.
     * Updates the value of showPhotoDialog to match the child.
     *
     * @param newVal {Boolean} the new value of showPhotoDialog.
     */
    closePhotoPanel(newVal) {
      this.showPhotoDialog = newVal;
      if (!newVal) {
        this.currentPhoto = null;
        this.currentPhotoIndex = null;
      }
    },
    /**
     * Called when the remove photo button is selected in the photo panel.
     * Emits a displayRemovePrompt event with a closePhotoPanel function the photoId and index of the current photo.
     *
     * @param photo {Object} the photo being removed
     */
    displayRemovePrompt(photo) {
      this.userPhotos.push(photo);
      this.$emit(
        "displayRemovePrompt",
        this.closePhotoPanel,
        photo.photoId,
        this.currentPhotoIndex
      );
    },
    /**
     * Called when the destination photo emits an error to display.
     * Emits the given error to it's parent to display.
     *
     * @param message {String} the error message
     */
    displayError(message) {
      this.$emit("displayError", message, "red");
    },
    /**
     * Called when the permission of a photo has been updated.
     * Emits an event to notify the parent to update its value.
     *
     * @param newValue {Boolean} the new value of isPrimary.
     */
    permissionUpdated(newValue) {
      this.$emit("permissionUpdated", newValue, this.currentPhotoIndex);
    },
    openAddPhotoDialog: function() {
      this.showAddPhotoDialog = true;
    },
    closeAddPhotoDialogHandler(newVal) {
      this.showAddPhotoDialog = newVal;
    }
  }
};
</script>

<style lang="scss" scoped>
.carousel {
  max-height: 300px;

  .dest-image {
    height: 50%;
  }
}

#add-button {
  visibility: hidden;
  position: absolute;
  left: 5px;
  top: 5px;
}

.carousel :hover #add-button {
  visibility: visible;
}
</style>


