<template>
  <div id="profile-pic-box">
    <img src="./tempProfilePic.jpg" id="profile-pic">

    <v-btn id="edit-btn" v-if="userStore.userId === userId" outline  color="secondary">Edit</v-btn>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";

export default {
  props: ["userId"],
  data() {
    return {
      userStore: UserStore.data
    };
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

