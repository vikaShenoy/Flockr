<template>
  <div id="profile-pic-box">
    <img src="./tempProfilePic.jpg" id="profile-pic">
    <v-btn @click="showProfilePhotoDialog" id="edit-btn" v-if="userStore.userId === userId" outline  color="secondary">Edit</v-btn>

    <ProfilePhotoDialog :dialog="dialog" :photos="photos" v-on:closeDialog="hideProfilePhotoDialog" v-on:showError="showError"/>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";
import ProfilePhotoDialog from "./ProfilePhotoDialog/ProfilePhotoDialog";

export default {
  components: {
    ProfilePhotoDialog
  },
  props: {
    userId: {
      type: Number,
      required: true
    },
    photos: {
      type: Array,
      required: true
    }
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
     *
     * @param imageObject the new profile picture object.
     */
    hideProfilePhotoDialog(imageObject) {
      this.dialog = false;
      if (imageObject !== null) {
        this.$emit("newProfilePic", imageObject);
      }
    },
    /**
     * Emits an event to profile to display error
     * @param text the text to display
     */
    showError(text) {
      this.$emit("showError", text);
    }
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
</style>

