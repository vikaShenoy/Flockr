<template>
  <div id="messages">
    <div
      v-for="message in messages" 
      v-bind:key="message.messageId"
      class="message"
    >        
    <v-avatar size="30" :class="{'your-avatar': message.user.userId === userStore.data.userId}">
      <img :src="getPhotoUrl(message.user)" />
    </v-avatar>
    <div
      
      color="secondary"
      text-color="white"
      :class="{'your-message' : message.user.userId === userStore.data.userId, 'other-message': message.user.userId !== userStore.data.userId}"
    ><span>{{ message.contents }}</span>
    </div>
    </div>
  </div>
</template>

<script>
import UserStore from "../../../../../stores/UserStore";
import { endpoint } from "../../../../../utils/endpoint";

export default {
  props: {
    messages: Array,
  },
  data() {
    return {
      userStore: UserStore
    };
  },
  methods: {
    /**
     * Gets a photo URL for a user
     */
    getPhotoUrl(user) {
      if (!user.profilePhoto) {
        return "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
      }
      return endpoint(`/users/photos/${user.profilePhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
    },
  }
};
</script>

<style lang="scss">




</style>

<style lang="scss" scoped>
@import "../../../../../styles/_variables.scss";

#messages {
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.message {
    margin-bottom: 20px;
}

.your-message {
  float: right;
  background-color: $secondary;
}

.your-avatar {
  float: right;
}

.your-message, .other-message {
  border-radius: 28px;
  color: #FFF;
  padding: 8px;
  max-width: 270px;
  word-wrap: break-word;
  white-space: pre-line;
}

.other-message {
  background-color: $text-light-grey !important;
  color: black !important;
}

</style>