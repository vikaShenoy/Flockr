<!--Handles the selection of the profile photo-->
<template>
  <div id="profile-photos-selection">
    <span>Photos</span>
    <v-divider></v-divider>
    <div v-if="photos.length">
      <img
              class="profile-photo"
              v-bind:class="{'selected-photo': selectedPhoto && photo.photoId == selectedPhoto.photoId}"
              v-for="(photo, index) in photos"
              v-bind:key="photo.photoId"
              :src="thumbnailPhotoUrl(photo.photoId)"
              @click="selectPhoto(index)"
      />
    </div>

    <div v-else>
      <br/>
      <b>You have no photos. Please upload a photo before setting a profile picture</b>
    </div>
  </div>
</template>
<script>
  import {endpoint} from "../../../../../utils/endpoint";

  export default {
    props: {
      photos: {
        type: Array,
        required: true
      },
      selectedPhoto: {
        type: Object,
      }
    },
    methods: {
      /**
       * Gets the full URL of a photo based on a photo ID
       * @param {number} photoIndex The index of the photo to get
       * @returns {string} The photo URL
       */
      selectPhoto(photoIndex) {
        this.$emit("photoSelected", this.photos[photoIndex]);
      },
      thumbnailPhotoUrl(photoId) {
        const authToken = localStorage.getItem("authToken");
        const queryAuthorization = `?Authorization=${authToken}`;
        return endpoint(`/users/photos/${photoId}/thumbnail${queryAuthorization}`);
      }
    }
  }
</script>

<style lang="scss" scoped>

  @import "../../../../../styles/_variables.scss";

  @keyframes fadein {
    from {
      opacity: 0;
      border: none;
    }
    to {
      opacity: 1;
    }
  }

  .profile-photo {
    max-width: 200px;
    max-height: 200px;
    padding-left: 5px;
    padding-right: 5px;
    cursor: pointer;
    animation: fadein 1s;
  }

  #profile-photos-selection {
    padding: 20px;
  }

  .selected-photo {
    border: 2px solid $primary;
  }

</style>


