<template>
  <v-carousel class="carousel" :cycle="false">
    <v-carousel-item
      v-for="(photo, index) in photos"
      :key="photo.photoId"
    >
      <v-img
        :src="photo.thumbEndpoint"
        class="dest-image"
        alt="Some Image"
        @click="openPhotoDialog(photo, index)"/>
    </v-carousel-item>
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
            :destinationPhotos="photos"
            :destinationId="destinationId"
            :showDialog="showAddPhotoDialog"
            @closeAddPhotoDialog="closeAddPhotoDialogHandler"
    />
    <v-img
      v-if="photos.length === 0"
      class="dest-image"
      src="https://picsum.photos/300/300"/>
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

</template>

<script>
import DestinationPhotoPanel from "./DestinationPhotoPanel/DestinationPhotoPanel";
import AddPhotoDialog from "./AddDestinationPhotoDialog/AddDestinationPhotoDialog";

export default {
  components: {AddPhotoDialog, DestinationPhotoPanel},
  props: {
    photos: Array,
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
      showAddPhotoDialog: false
    }
  },
  methods: {
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
     * @param photoId {Number} the id of the current photo.
     */
    displayRemovePrompt(photoId) {
      this.$emit("displayRemovePrompt", this.closePhotoPanel, photoId, this.currentPhotoIndex);
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
    openAddPhotoDialog: function () {
      this.showAddPhotoDialog = true;
    },
    closeAddPhotoDialogHandler(newVal) {
      this.showAddPhotoDialog = newVal
    }
  }
};
</script>

<style lang="scss" scoped>

  .carousel {
    max-height: 300px;
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

  .dest-image {
    width: 300px;
    height: 300px;
  }

</style>


