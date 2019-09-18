<template>
  <div id="profile-pic-box">
    <img v-if="profilePhoto" class="profile-pic" :src="photoUrl(profilePhoto.photoId)" alt="Profile Picture"/>
    <img v-else src="./defaultProfilePicture.png" class="profile-pic" alt="Default Profile Picture"/>
    <v-btn @click="showProfilePhotoDialog" id="edit-btn" v-if="hasPermissionToView()" color="secondary" fab><v-icon>edit</v-icon></v-btn>
    <h1 class="mb-0 name">{{ fullname }}</h1>
    <ProfilePhotoDialog :dialog="dialog" :photos="photos" v-on:closeDialog="hideProfilePhotoDialog"
                        v-on:showError="showError"/>
  </div>
</template>

<script>
  import UserStore from "../../../stores/UserStore";
  import ProfilePhotoDialog from "./ProfilePhotoDialog/ProfilePhotoDialog";
  import {endpoint} from "../../../utils/endpoint";

  export default {
    components: {
      ProfilePhotoDialog,
    },
    props: {
      userId: Number,
      photos: Array,
      profilePhoto: Object,
      fullname: String
    },
    data() {
      return {
        dialog: false,
        userStore: UserStore.data
      };
    },
    methods: {
      showProfilePhotoDialog() {
        this.dialog = true;
      },

      /**
       * Hides the select profile photo dialog and emits the new profile picture object if changed.
       * @param imageObject the new profile picture object.
       */
      hideProfilePhotoDialog(imageObject) {
        this.dialog = false;
        if (imageObject) {
          this.$emit("updateProfilePic", this.profilePhoto, imageObject);
        }
      },
      /**
       * Emits an event to profile to display error
       * @param text the text to display
       */
      showError(text) {
        this.$emit("showError", text);
      },
      /**
       * Checks if the logged in user has permission to view the edit button
       * @returns {boolean} If the user has permission to view the edit button
       */
      hasPermissionToView() {
        return UserStore.methods.hasPermission(this.$route.params.id);
      },
      /**
       * Gets the URL of a photo for a user
       * @param {number} photoId the ID of the photo to get
       * @returns {string} the url of the photo
       */
      photoUrl(photoId) {
        const authToken = localStorage.getItem("authToken");
        const queryAuthorization = `?Authorization=${authToken}`;
        return endpoint(`/users/photos/${photoId}${queryAuthorization}`);
      },

    }
  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #profile-pic-box {
    margin-top: 10px;
  }

  #profile-pic-box:hover #edit-btn {
    visibility: visible;
  }

  #profile-pic-box:hover .v-icon {
    visibility: visible;
  }

  .profile-pic {
    max-width: 250px;
    margin: 0 auto;
    display: block;
    border-radius: 12em;
  }

  #edit-btn {
    position: absolute;
    right: 10px;
    bottom: 10px;
    visibility: hidden;
    .v-icon {
      visibility: hidden;
    }
  }

  #undo-redo {
    margin: 0 auto;
    width: 60px;
  }

  .name {
    position: absolute;
    left: 250px;
    bottom: 125px;
    width: 100%;
    -webkit-text-fill-color: white;
    text-shadow: 0 0 3px rgba(0, 0, 0, 0.8);
  }
</style>

