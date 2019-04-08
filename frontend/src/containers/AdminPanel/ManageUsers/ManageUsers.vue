<template>
  <div class=manage-users>
    <v-card>
      <v-list two-line>
        <v-subheader class="manage-users-row">
          <div class="manage-users-text">
            <p>Manage users</p>
          </div>

          <v-btn class="edit-user-button" :disabled="this.selectedUsers.length != 1" @click="editUserButtonClicked">
            Edit user
          </v-btn>

          <v-btn class="delete-users-button" :disabled="this.selectedUsers.length === 0"
            @click="deleteUsersButtonClicked">
            Delete users
          </v-btn>
        </v-subheader>

        <!-- User tile -->
        <v-list-tile v-for="(item) in items" :key="item.userId" avatar @click="item.selected = !item.selected">
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

export default {
  data() {
    return {
      items: [
        {
          avatar: "https://cdn.vuetifyjs.com/images/lists/1.jpg",
          userId: 1,
          title: "Tom Belmonte",
          subtitle: "Joined on 12/03/2007",
          selected: false
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/2.jpg',
          userId: 2,
          title: 'Roger Belmonte',
          subtitle: "Joined on 12/03/2008",
          selected: false
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/3.jpg',
          userId: 42,
          title: 'Teresa Rojas',
          subtitle: "Joined on 12/03/2004",
          selected: true
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/4.jpg',
          userId: 5,
          title: 'Juan Galindo',
          subtitle: "Joined on 12/03/2017",
          selected: false
        }
      ]
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
    editUserButtonClicked: function() {
      const userId = this.selectedUsers[0];
      console.log(`User wants to edit user: ${userId}`);
    },
    deleteUsersButtonClicked: async function() {
      const userIds = this.selectedUsers;
      console.log(`User wants to delete users: ${userIds}`)
      try {
        await deleteUsers(userIds);
      } catch (err) {
        console.error(`Could not delete users in admin panel: ${err}`);
      }
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
