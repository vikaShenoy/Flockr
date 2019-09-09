<template>
  <div id="voice-users">
    <div v-for="user in getUsersInVoice">
      {{ user.userId }}}
    </div>
  </div>
</template>


<script>
  export default {
    props: {
      usersInChat: Array,
      userIdsInVoice: Array
    },
    computed: {
        getUsersInVoice() {
            return this.userIdsInVoice.map(userId => {
                console.log("UsersInChat is: ");
                console.log(this.usersInChat);
                return this.usersInChat.find(user => user.userId === userId);
            });
        }
    }
  };
</script>

<style lang="scss" scoped>
  @import "../../../../styles/_variables.scss";

  #voice-users {
    background-color: white;
    height: 40px;
    width: 100%;
    border-bottom: 1px solid $text-light-grey;
  }
</style>