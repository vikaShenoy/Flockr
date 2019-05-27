<template>
  <v-carousel class="carousel" :cycle="false">
    <v-carousel-item
      v-for="(photo, index) in photos"
      :key="photo.photoId"
    >
      <v-img
        :src="photo.thumbEndpoint"
        style="width:300px; height:300px"
        alt="Some Image"
        @click="openPhotoDialog(photo, index)"
        v-on:mouseenter="addPhotoButton = !addPhotoButton"
        v-on:mouseleave="addPhotoButton = !addPhotoButton"
      >
        <v-btn
          color="blue-grey darken-3"
          v-on:mouseenter="inButton = !inButton"
          v-on:mouseleave="inButton = !inButton"
          fab
          @click="openAddPhotoDialog()"
          v-if="addPhotoButton"
          >
            <v-icon>add</v-icon>
        </v-btn>

      </v-img>


    </v-carousel-item>
    <destination-photo-panel
            :photo="currentPhoto"
            :showDialog="showPhotoDialog"
            @closeDialog="closePhotoPanel"
            @displayError="displayError"
            @permissionUpdated="permissionUpdated"
    />
    <AddPhotoDialog
            :destinationId="destinationId"
            :showDialog="showAddPhotoDialog"
            @closeAddPhotoDialog="closeAddPhotoDialogHandler"
    />
  </v-carousel>

</template>

<script>
import DestinationPhotoPanel from "./DestinationPhotoPanel/DestinationPhotoPanel";
import AddPhotoDialog from "./AddDestinationPhotoDialog/AddDestinationPhotoDialog";

export default {
  components: {AddPhotoDialog, DestinationPhotoPanel},
  props: {
    photos: Array,
    destinationId: Number
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
    openPhotoDialog(photo, index) {
      if (!this.inButton) {
        this.showPhotoDialog = true;
        this.currentPhoto = photo;
        this.currentPhotoIndex = index;
      }

    },
    closePhotoPanel(newVal) {
      this.showPhotoDialog = newVal;
      if (!newVal) {
        this.currentPhoto = null;
        this.currentPhotoIndex = null;
      }
    },
    /**
     * Called when the destination photo emits an error to display.
     * Emits the given error to it's parent to display.
     *
     * @param message {String} the error message
     */
    displayError(message) {
      this.$emit("displayError", message);
    },
    /**
     * Called when the permission of a photo has been updated.
     * Emits an event to notify the parent to update its value.
     *
     * @param newValue {Boolean} the new value of isPrimary.
     * @param index {Number} the index of the photo.
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
</style>


