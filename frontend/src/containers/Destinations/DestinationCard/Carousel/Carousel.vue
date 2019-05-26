<template>
  <v-carousel class="carousel" :cycle="false">
    <v-carousel-item
      v-for="photo in photos"
      :key="photo.photoId"
    >
      <img
        :src="getThumbnailUrl(photo.photoId)"
        style="width:300px;height:300px"
        alt="Some Image"
      />
    </v-carousel-item>
  </v-carousel>

</template>

<script>
import { getThumbnailUrl } from "../../../../utils/photos";

export default {
  props: {
    photos: Array
  },
  methods: {
    /**
     * Gets a thumbnail for a URL based on it's photo ID
     * @param {number} photoId The ID of the photo
     * @returns {string} The URL of the photo
     */
    getThumbnailUrl(photoId) {
      // return getThumbnailUrl(photoId);

      // Returns temporary URL while endpoint is being developed
      return "https://i2.wp.com/digital-photography-school.com/wp-content/uploads/2012/10/image1.jpg?fit=500%2C500&ssl=1";
    }
  }
};
</script>

<style lang="scss" scoped>
.carousel {
  max-height: 300px;
}
</style>


