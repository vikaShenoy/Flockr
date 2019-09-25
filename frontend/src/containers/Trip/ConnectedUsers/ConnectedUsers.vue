<template>
     <v-expansion-panel id="connected-users">
    <v-expansion-panel-content
      id="header"
    >
      <template v-slot:header >
        <h4 id="title">Connected Users</h4>
      </template>

       <v-icon slot="actions" color="secondary">$vuetify.icons.expand</v-icon>

      <v-card>
        <v-list>
        <v-list-tile
          clas="user"  
          v-for="user in users"
          v-bind:key="user.userId"
        >

        <v-avatar size="30">
          <img :src="getPhotoUrl(user)" :class="{'connected-user': userIsConnected(user.userId)}">
        </v-avatar>

        
        <span class="name">{{ user.firstName }} {{ user.lastName }}</span>

        
          
        </v-list-tile>
        </v-list>
      </v-card>

    </v-expansion-panel-content>
  </v-expansion-panel>
</template>

<script>
import { endpoint } from '../../../utils/endpoint';
import defaultPic from "../../Profile/ProfilePic/defaultProfilePicture.png";
export default {
  props: {
    users: Array,
    connectedUsers: Array
  },
  methods: {
    /**
     * Gets the user photo
     * @param user the user
     * @returns {string} if the user has a photo, it returns the user's profile pic. Otherwise, returns default pic
     */
    getPhotoUrl(user) {
      if (!user.profilePhoto) {
        return defaultPic;
      }
      return endpoint(`/users/photos/${user.profilePhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
    },
    /**
     * Returns true if the user is connected. Otherwise returns false.
     * @param userId the user id to be checked
     * @returns {boolean} true if the user is connected, otherwise returns false.
     */
    userIsConnected(userId) {
      const userIds = this.connectedUsers.map(user => user.userId);
      const userIdSet = new Set(userIds);
      return userIdSet.has(userId);
    }
  },
}
</script>

<style lang="scss" scoped>
@import "../../../styles/_variables.scss";

  #connected-users {
    width: 200px;
    z-index: 5;
    position: absolute;
    right: 0px;
    bottom: 0px;

    #header {
      background-color: $primary;
    }

    #title {
      color: $secondary;
    }

    .name {
      font-size: 1rem;
      margin-left: 10px;
    }

    .connected-user {
      border: 2px solid rgb(150, 230, 107);
    }

  }


  .light-text {
    color: #FFF;
  }
</style>