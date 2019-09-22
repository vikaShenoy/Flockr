<template>
  <div id="voice-users" :style="{height: userIdsInVoice.length ? '50px' : '0px'}">
    <div v-for="user in getUsersInVoice" v-bind:key="user.userId">
      <div class="connected-user">
        <UserAvatar
          :isConnected="true"
          :user="user"
          :size="30"
        />

        <span>{{ user.firstName }}</span>
      </div>
    </div>
  </div>
</template>


<script>
import UserAvatar from "../ChatGroup/Messages/UserAvatar";

  export default {
    components: {
      UserAvatar
    },
    props: {
      usersInChat: Array,
      userIdsInVoice: Array,
    },
    computed: {
      /**
       * Maps user ids in voice to users in voice
       * @return {Array} Returns array of user objects in voice
       */
      getUsersInVoice() {
          return this.userIdsInVoice.map(userId => {
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
    width: 100%;
    border-bottom: 1px solid $text-light-grey;
    transition: height 0.1s ease-in;
    padding-left: 5px;
    padding-right: 5px;
    display: flex;
    align-items: center;
  }

  .connected-user {
    width: 40px;
    display: flex;
    align-items: center;
    flex-direction: column;
    margin-top: 4px;


    span {
      color: $text-dark-grey;
      overflow: hidden;
      width: 40px;
      text-overflow: ellipsis;
    }
    
  }
</style>