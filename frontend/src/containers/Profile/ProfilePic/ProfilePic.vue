<template>
  <div id="profile-pic-box">
    <img v-if="profilePhoto" id="profile-pic" :src="photoUrl(profilePhoto.photoId)" alt="Profile Picture" />
    <img v-else src="./defaultProfilePicture.png" id="profile-pic" alt="Default Profile Picture" />
    <div id="undo-redo">
      <UndoRedo ref="undoRedo" />
    </div>
    <v-btn @click="showProfilePhotoDialog" id="edit-btn" v-if="hasPermissionToView()" outline  color="secondary">Edit</v-btn>
    <ProfilePhotoDialog :dialog="dialog" :photos="photos" v-on:closeDialog="hideProfilePhotoDialog" v-on:showError="showError"/>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";
import ProfilePhotoDialog from "./ProfilePhotoDialog/ProfilePhotoDialog";
import { endpoint } from "../../../utils/endpoint";
import UndoRedo from "../../../components/UndoRedo/UndoRedo";
import Command from "../../../components/UndoRedo/Command";
import { setProfilePictureToOldPicture } from "./ProfilePicService";

export default {
  components: {
    ProfilePhotoDialog,
    UndoRedo
  },
  props: {
    userId: Number,
    photos: Array,
    profilePhoto: Object
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
     * Also adds command to undo stack to allow for undo and redo events
     *
     * @param imageObject the new profile picture object.
     */
    hideProfilePhotoDialog(imageObject) {
      this.dialog = false;
      if (imageObject) {

        const undoCommand = async (profilePhoto) => {
          if (profilePhoto) {
            await setProfilePictureToOldPicture(profilePhoto);
          }

          this.$emit("newProfilePic", profilePhoto);
        };

        const redoCommand = async (imageObject) => {
          await setProfilePictureToOldPicture(imageObject);
          this.$emit("newProfilePic", imageObject);
        };
        
        const profilePhotoCommand = new Command(undoCommand.bind(null, this.profilePhoto), redoCommand.bind(null, imageObject));
        this.$refs.undoRedo.addUndo(profilePhotoCommand);
        this.$emit("newProfilePic", imageObject);
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
#profile-pic-box {
  margin-top: 10px;
}

#profile-pic {
  max-width: 200px;
  margin: 0 auto;
  display: block;
}

#edit-btn {
  margin: 0 auto;
  display: block;
  margin-top: 10px;
}

#undo-redo {
  margin: 0 auto; 
  width: 60px;
}
</style>

