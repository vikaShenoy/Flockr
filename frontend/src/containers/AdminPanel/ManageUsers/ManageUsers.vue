<template>
  <div class=manage-users>
    <v-card>
      <v-list two-line>
        <v-subheader class="manage-users-row">
          <div class="manage-users-text">
            <p>Manage users</p>
          </div>
          <v-text-field label="Search User" color="secondary" @input="searchAdminChange">
          </v-text-field>
          <v-btn class="logout-user-button" v-on:click.stop="logoutUsersButtonClicked" :disabled="this.selectedUsers.length != 1"
            @click="logoutUsersButtonClicked">
            Log Out User
          </v-btn>

          <v-btn class="new-user-button"
            @click="signupButtonClicked">
            Sign Up User
          </v-btn>

          <v-btn class="edit-trips-button" :disabled="this.selectedUsers.length === 0"
            @click="viewTripsButtonClicked">
            View Trips
          </v-btn>

          <v-btn class="edit-user-button" v-on:click.stop="editUserButtonClicked" :disabled="this.selectedUsers.length != 1">
            Edit user
          </v-btn>

          <v-btn class="delete-users-button" :disabled="this.selectedUsers.length === 0"
            @click="showPrompt('Are you sure?', deleteUsersButtonClicked)">
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

      <v-dialog v-model="showSignup" max-width="800">
      <v-card>
        <SignUp @exit="closeSignupModal()"></SignUp>
        <v-card-actions>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <prompt-dialog
    :message=prompt.message
    :onConfirm="prompt.onConfirm"
    :dialog="prompt.show" 
    v-on:promptEnded="prompt.show=false"></prompt-dialog>
  </div>

</template>


<script>
import {deleteUsers, getAllUsers} from "../AdminPanelService";
import {endpoint} from "../../../utils/endpoint.js";
import moment from "moment";
import SignUp from "../../Signup/Signup";
import PromptDialog from "../../../components/PromptDialog/PromptDialog.vue";

export default {
  components: {
    PromptDialog,
    SignUp
  },
  mounted () {
    this.items = this.mapUsers();
  },
  data() {
    return {
      items: [],
      showSignup: false,
      prompt: {
        message: "",
        onConfirm: null,
        show: false
      }
    };
  },
  computed: {
    
    /**
     * Get the user ids of selected users.
     */
    selectedUsers: function() {
      return this.items.filter((item) => item.selected).map((user) => user.userId);
    },
    
    /**
     * Return whether the edit button is enabled.
     */
    isEditButtonEnabled: function() {
      // only enable the button when one user is selected
      return this.selectedUsers.length == 0;
    }
  },
  methods: {

    /**
     * Emit a function call, indicates search admin 
     * has changed.
     */
    searchAdminChange(searchAdmin) 
    {
      this.$emit('update:adminSearch',searchAdmin);
    },

    /**
     * Show a prompt to the user.
     */
    showPrompt(message, onConfirm) {
      this.prompt.message = message;
      this.prompt.onConfirm = onConfirm;
      this.prompt.show = true;
    },

    /**
     * Close the modal containing the signup component.
     */
    closeSignupModal() {
      this.showSignup = false;
      this.$parent.getAllUsers();
    },

    /**
     * Open the modal component containing the signup component.
     */
    signupButtonClicked: function() {
      this.showSignup = true;
    },

    /**
     * Event handler for when the button to edit an user is clicked.
     * Emit an event for the parent to handle editing users.
     */
    editUserButtonClicked: function() {
      const userId = this.selectedUsers[0];
      this.$emit('wantToEditUserById', userId);
    },
    /**
     * Call the admin panel service to logout the given user ids.
     */
    logoutUsersButtonClicked: async function() {
      const userIds = this.selectedUsers;
      this.$emit("logoutUsersByIds", userIds);
    },
    /**
     * Call the admin panel service to delete the given user ids.
     */
    deleteUsersButtonClicked: async function() {
      const userIds = this.selectedUsers;
      this.$emit("deleteUsersByIds", userIds);
    },

    /**
     * Open the trips page for the selected user.
     */
    viewTripsButtonClicked: function() {
      const userId = this.selectedUsers[0];
      this.$router.push(`/travellers/${userId}/trips`);
    },
    /**
     * Format the user data to be displayed on the admin panel. 
     * Use a generic avatar untill photos are implemented.
     */
    mapUsers: function() {
        for (let thing of this.users) {
            console.log(thing);
        }
      return this.users.map((user) => ({
          avatar: this.photoUrl(user.profilePhoto),
          userId: user.userId,
          title: user.firstName + ' ' + user.lastName,
          subtitle: 'Joined on ' + moment(user.timestamp).format("D/M/YYYY H:mm"),
          selected: false
      }));
    },
    /**
     * Gets the URL of a photo for a user
     * @param {number} photoId the ID of the photo to get
     * @returns {string} the url of the photo
     */
    photoUrl(profilePhoto) {
        if (profilePhoto != null) {
            const authToken = localStorage.getItem("authToken");
            const queryAuthorization = `?Authorization=${authToken}`;
            return endpoint(`/users/photos/${profilePhoto.photoId}${queryAuthorization}`);
        } else {
            return "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";

        }
    }
  },
  props: ["users"],
  watch: {
    users(newUsers) {
      this.items = this.mapUsers();
    }
  },
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

    .edit-trips-button {
      justify-self: end;
      width: fit-content;
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
