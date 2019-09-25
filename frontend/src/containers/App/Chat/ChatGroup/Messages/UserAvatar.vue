<template>
  <v-avatar
    :size="size"
    :class="avatarClass"
  >
    <img alt="User Image" :src="getPhotoUrl(user)" />
  </v-avatar>    
</template>

<script>
import { endpoint } from "../../../../../utils/endpoint";
import defaultPic from "../../../../Profile/ProfilePic/defaultProfilePicture.png";

export default {
  props: {
    isConnected: Boolean,
    isOwnUser: Boolean,
    user: Object,
    size: Number
  },
  methods: {
    /**
     * Gets a photo URL for a user
     */
    getPhotoUrl(user) {
      if (!user.profilePhoto) {
        return defaultPic;
      }
      return endpoint(`/users/photos/${user.profilePhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
    }
  },
  computed: {
    /**
     * Get the CSS class for the avatar
     * @returns {String} the CSS class for the avatar
     */
    avatarClass() {
      const avatarOwnership = this.isOwnUser ? "your-avatar" : "other-avatar";
      const connectionStatus = this.isConnected ? "connected" : "disconnected";
      return `${avatarOwnership} ${connectionStatus}`;
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../../../../styles/_variables.scss";

.connected > img {
  border: solid 2px $success;
}


.your-avatar {
  float: right;
  margin-left: 5px;
  margin-top: 5px;
}

.other-avatar {
  float: left;
}
</style>
