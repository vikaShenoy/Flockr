<template>
  <v-carousel class="carousel" :cycle="false">
    <v-carousel-item
      v-for="(photo, index) in photos"
      :key="photo.photoId"
    >
      <img
        :src="photo.thumbEndpoint"
        style="width:300px; height:300px"
        alt="Some Image"
        @click="openPhotoDialog(photo, index)"/>
    </v-carousel-item>
    <destination-photo-panel
            :photo="currentPhoto"
            :showDialog="showPhotoDialog"
            @closeDialog="closePhotoPanel"
            @displayError="displayError"
            @permissionUpdated="permissionUpdated"
    />
  </v-carousel>

</template>

<script>
import { getThumbnailUrl } from "../../../../utils/photos";
import DestinationPhotoPanel from "./DestinationPhotoPanel/DestinationPhotoPanel";

export default {
  components: {DestinationPhotoPanel},
  props: {
    photos: Array
  },
  data() {
    return {
      showPhotoDialog: false,
      currentPhoto: null,
      currentPhotoIndex: null

    }
  },
  methods: {
    openPhotoDialog(photo, index) {

      this.showPhotoDialog = true;
      this.currentPhoto = photo;
      this.currentPhotoIndex = index;
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
    }
  }
};
</script>

<style lang="scss" scoped>
.carousel {
  max-height: 300px;
}
</style>


