<template>
  <v-carousel class="carousel" :cycle="false">
    <v-carousel-item
      v-for="(photo, index) in photos"
      :key="photo.photoId"
    >
      <img
        :src="getThumbnailUrl(photo.photoId)"
        style="width:300px;height:300px"
        alt="Some Image"
        @click="openPhotoDialog(photo, index)"
      />
    </v-carousel-item>
    <destination-photo-panel
            :photo="currentPhoto"
            :showDialog="showPhotoDialog"
            @closeDialog="closePhotoPanel"
            :photoIndex="currentPhotoIndex"
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
    print(message) {
      console.log(message);
    },
    openPhotoDialog(photo, index) {

      this.showPhotoDialog = true;
      this.currentPhoto = photo;
      this.currentPhotoIndex = index;
    },
    /**
     * Gets a thumbnail for a URL based on it's photo ID
     * @param {number} photoId The ID of the photo
     * @returns {string} The URL of the photo
     */
    getThumbnailUrl(photoId) {
      // return getThumbnailUrl(photoId);

      // Returns temporary URL while endpoint is being developed
      return "https://i2.wp.com/digital-photography-school.com/wp-content/uploads/2012/10/image1.jpg?fit=500%2C500&ssl=1";
    },
    closePhotoPanel(newVal) {
      this.showPhotoDialog = newVal;
    }
  }
};
</script>

<style lang="scss" scoped>
.carousel {
  max-height: 300px;
}
</style>


