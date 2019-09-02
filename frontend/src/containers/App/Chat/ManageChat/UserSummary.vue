<template>
  <div>
    <v-avatar>
      <img
            :src="getUserPhoto(user)"
            alt="avatar"
            class="avatar"
      />
    </v-avatar>
    <span>{{ user.firstName }}</span>
    <v-icon>delete</v-icon>
  </div>
  
</template>

<script>
  import { endpoint } from "../../../../utils/endpoint";
  export default {
    name: "UserSummary",
    data() {
      return {
        imageSrc: "https://www.tibs.org.tw/images/default.jpg"
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
      getUserPhoto(user) {
        if (!user.profilePhoto) {
          return  "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
        }
        try {
          return endpoint(`/users/photos/${user.profilePhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
        } catch (e) {
          return  "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
        }
      }
    }
  }
</script>

<style scoped>

</style>