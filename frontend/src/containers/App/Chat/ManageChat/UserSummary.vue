<template>
  <div>
    <v-avatar id="user-avatar">
      <img
            :src="getUserPhoto(user)"
            alt="avatar"
            class="avatar"
      />
    </v-avatar>
    <span id="user-name">{{ user.firstName }}</span>
    <v-icon
      v-on:click="$emit('deleteUser', user.userId)"
    >delete</v-icon>
  </div>
  
</template>

<script>
  import { endpoint } from "../../../../utils/endpoint";
  export default {
    name: "UserSummary",
    data() {
      return {
        imageSrc: "",
      }
    },
    props: {
      user: {
        type: Object,
        required: false,
      }
    },
    mounted() {
    },
    methods: {
      /**
       * Get the avatar photo for each user in the display list.
       * @param user user to get the photo for.
       * @returns {string} src for the photo.
       */
      getUserPhoto(user) {
        if (!user.profilePhoto) {
          return  "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
        }
        try {
          return endpoint(
              `/users/photos/${user.profilePhoto.photoId}/thumbnail?Authorization=${
                localStorage.getItem("authToken")}`);
        } catch (e) {
          return  "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
        }
      },
    }
  }
</script>

<style scoped>

  #user-avatar {
    margin-right: 20px;
  }

  #user-name {
    margin-right: 20px;
  }
</style>