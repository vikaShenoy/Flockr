<template>
  <div class=manage-users>
    <v-card>
      <v-list two-line>
        <v-subheader class="manage-users-row">
          <div class="manage-users-text">
            <p>Manage users</p>
          </div>

          <v-btn class="edit-user-button" v-on:click.stop="editUserButtonClicked" :disabled="this.selectedUsers.length != 1">
            Edit user
          </v-btn>

          <v-btn class="delete-users-button" :disabled="this.selectedUsers.length === 0"
            @click="deleteUsersButtonClicked">
            Delete users
          </v-btn>
        </v-subheader>

        <!-- User tile -->
        <v-list-tile v-for="item in items" :key="item.userId" avatar @click="item.selected = !item.selected">
          <v-list-tile-avatar>
            <img :src="item.avatar">
          </v-list-tile-avatar>

          <v-list-tile-content>
            <v-list-tile-title>{{item.title}}</v-list-tile-title>
            <v-list-tile-sub-title>{{item.subtitle}}</v-list-tile-sub-title>
          </v-list-tile-content>

          <!-- Checkmark for when user is selected -->
          <v-icon v-if="!item.selected">check_circle_outline</v-icon>
          <v-icon v-else class="selected-icon">check_circle</v-icon>
        </v-list-tile>
      </v-list>
    </v-card>
  </div>
</template>


<script>
import {deleteUsers} from "../AdminPanelService";
import moment from "moment";

export default {
  mounted () {
    this.items = this.mapUsers();
    //console.log(mapUsers())
  },
  data() {
    return {
      items: []
    };
  },
  computed: {
    // get the user ids that are selected
    selectedUsers: function() {
      return this.items.filter((item) => item.selected).map((user) => user.userId);
    },
    // return whether the edit button is enabled
    isEditButtonEnabled: function() {
      // only enable the button when one user is selected
      return this.selectedUsers.length == 0;
    }
  },
  methods: {
    // event handler for when the button to edit an user is clicked
    // emit an event for the parent to handle editing users
    editUserButtonClicked: function() {
      const userId = this.selectedUsers[0];
      this.$emit('wantToEditUserById', userId);
    },
    // call the admin panel service to delete the given user ids
    deleteUsersButtonClicked: async function() {
      const userIds = this.selectedUsers;
      console.log(userIds);
      this.$emit("deleteUsersByIds", userIds);
    },
    mapUsers: function() {
      return this.users.map((user) => ({
          avatar: 'https://cdn.vuetifyjs.com/images/lists/4.jpg',
          userId: user.userId,
          title: user.firstName + ' ' + user.lastName,
          subtitle: 'Joined on ' + moment(user.timestamp).format("D/M/YYYY H:mm"),
          selected: false
      }));
    }
  },
  props: ["users"],
  watch: {
    users(newUsers) {
      this.items = this.mapUsers();
    }
  }
}
</script>


<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  .manage-users {
    height: 100%;
    width: 100%;
  }

  .selected-icon {
    color: $success;
  }

  .manage-users-row {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;

    .manage-users-text {
      p {
        text-align: left;
      }
      flex-grow: 1;
      justify-self: start;
    }

    .edit-user-button {
      justify-self: end;
      width: fit-content;
    }

    .delete-users-button {
      justify-self: end;
      width: fit-content;
    }
  }

  p {
    margin: 0;
  }
</style>
