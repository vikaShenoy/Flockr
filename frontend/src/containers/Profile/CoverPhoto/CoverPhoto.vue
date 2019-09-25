<template>
  <div class="cover-photo">
    <v-img
        :src="coverPhotoEndpoint"
        aspect-ratio="2.75"
        class="cover-photo"
    >
      <v-btn @click="openCoverPhotoDialog" id="edit-btn" v-if="hasPermissionToEdit" color="secondary" fab><v-icon>edit</v-icon></v-btn>
    </v-img>

    <cover-photo-dialog
      :dialog="displayCoverPhotoDialog"
      :photo="userProfile.coverPhoto"
      :photos="photos"
      :userId="userProfile.userId"
      @closeDialog="displayCoverPhotoDialog = false"
      @deleteCoverPhoto="$emit('deleteCoverPhoto')"
      @photoSelected="coverPhotoSelected"/>
  </div>
</template>

<script>
  import {endpoint} from "../../../utils/endpoint";
  import UserStore from "../../../stores/UserStore";
  import CoverPhotoDialog from "./CoverPhotoDialog/CoverPhotoDialog";
  import {requestChangeCoverPhoto} from "../ProfileService";

  export default {
    name: "cover-photo",
    components: {
      CoverPhotoDialog
    },
    data() {
      return {
        displayCoverPhotoDialog: false
      }
    },
    props: {
      userProfile: Object,
      photos: Array
    },
    computed: {
      /**
       * Gets the cover photo endpoint for the cover photo.
       * @return {string} the filename of the cover photo (null if none)
       */
      coverPhotoEndpoint() {
        return this.userProfile.coverPhoto ? endpoint(
            `/users/photos/${this.userProfile.coverPhoto.photoId}?Authorization=${localStorage.getItem(
                "authToken")}`) : endpoint(
            `/photos/cover/default?Authorization=${localStorage.getItem(
                "authToken")}`);
      },
      /**
       * Gets whether the viewer has permission to edit this profile.
       * @return {boolean} true if viewer has permission to edit profile.
       */
      hasPermissionToEdit() {
        return UserStore.methods.hasPermission(this.$route.params.id);
      }
    },
    methods: {
      /**
       * Changes the user's cover photo with the new chosen cover photo
       */
      async coverPhotoSelected(newCoverPhoto) {
        try {
          const coverPhoto = await requestChangeCoverPhoto(this.userProfile.userId,
              newCoverPhoto.photoId);
          this.closeCoverPhotoDialog();
          this.$emit("coverPhotoUpdated", coverPhoto)
        } catch (e) {
          this.$emit("show-snackbar", {
            timeout: 5000,
            message: "Cannot update cover photo",
            color: "error"
          })
        }
      },
      /**
       * Closes the cover photo dialog.
       */
      closeCoverPhotoDialog() {
        this.displayCoverPhotoDialog = false;
      },
      /**
       * Opens the cover photo dialog.
       */
      openCoverPhotoDialog() {
        this.displayCoverPhotoDialog = true;
      },
      /**
       * Emits an event to update the profile picture.
       *
       * @param profilePhoto {Object} the current photo object.
       * @param imageObject {Object} the new photo object
       */
      updateProfilePic(profilePhoto, imageObject) {
        this.$emit("updateProfilePic", profilePhoto, imageObject);
      },
      /**
       * Emits an event to show an error.
       *
       * @param text the text to display in the error.
       */
      showError(text) {
        this.$emit("showError", text);
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  .cover-photo {
    height: 350px;
    min-width: 800px;
  }

  .cover-photo:hover #edit-btn {
    visibility: visible;
    .v-icon {
      visibility: visible;
    }
  }

  #edit-btn {
    position: absolute;
    right: 10px;
    bottom: 30px;
    visibility: hidden;
    .v-icon {
      visibility: hidden;
    }
  }

</style>