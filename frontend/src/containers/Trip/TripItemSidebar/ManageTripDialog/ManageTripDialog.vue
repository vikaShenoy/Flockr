<template>
  <v-dialog width="600px" v-model="isShowingDialog">

    <v-card v-if="!showAlertCard" id="manage-trip-dialog">
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">Manage Trip</h2>
          </v-spacer>

        </v-layout>
        <v-btn @click="showAlertCard = true; leaveOrDelete" class="red--text leave-button" flat>Leave</v-btn>
      </v-card-title>

    <div id="manage-trip-contents">
      <v-container grid-list-md text-center>
      <v-layout wrap>
        <v-flex xs10 offset-xs1>
          <div v-if="trip.users.length > 1">
          <h4>Chat</h4>
          <v-layout row >
            <v-text-field v-if="!chatExists" v-model="chatNameToCreate" placeholder="Chat name" />
            <v-btn v-if="!chatExists" color="info" @click="chatButtonClicked" :loading="isChatButtonLoading">
              <v-icon left>chat</v-icon>
              Create chat
            </v-btn>

            <p v-if="chatExists">To access your chat, please open the chat window</p>
          </v-layout>
          </div>


          <h4>Selected users</h4>
          <v-layout column>
            <v-flex
              v-for="userRole in userRoles"
              v-bind:key="userRole.user.userId"
              class="selected-user"
            >
              <h3>{{ formatName(userRole.user) }}</h3>
              <v-spacer/>
             <v-select v-model="userRole.role" :items="roleTypes" class="role-type" color="secondary" item-text="name" item-value="value"></v-select>
            </v-flex>
          </v-layout>

          <div id="selected-users">

            <GenericCombobox
              label="Users"
              :get-function="searchUser"
              :item-text="(user) => user.firstName + ' ' + user.lastName"
              multiple
              v-model="selectedUsers"
              @items-selected="updateSelectedUsers"
              :filter-function="user => user.userId !== userStore.data.userId"
            ></GenericCombobox>
            <!--<v-combobox :items="users" :item-text="formatName" v-model="selectedUsers" label="Users" multiple></v-combobox>-->
          </div>
        </v-flex>
      </v-layout>
      </v-container>
      
      <v-card-actions>
        <v-spacer align="right">
          <v-btn
            flat
            color="secondary"
            @click="isShowingDialog = false"
            >Cancel</v-btn>

          <v-btn
            color="success"
            flat
            @click="saveUsersInTrip"
            :loading="isLoading"
            >Save</v-btn>
        </v-spacer>
      </v-card-actions>
    </div>
    </v-card>

    <v-card v-if="showAlertCard" id="manage-trip-alert">
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">Warning</h2>
          </v-spacer>

        </v-layout>
      </v-card-title>

      <div id="manage-trip-alert-contents">
        <v-container grid-list-md text-center>
          <v-layout wrap>
            <v-flex xs10 offset-xs1>
              {{ onlyUser ? "You are the only user in the trip. Proceeding will delete the trip" : "Are you sure you want to leave the trip?" }}
            </v-flex>
          </v-layout>
        </v-container>

        <v-card-actions>
          <v-spacer align="right">
            <v-btn
                    flat
                    color="secondary"
                    @click="showAlertCard = false"
            >Cancel</v-btn>

            <v-btn
              color="red"
              flat
              :loading="isLoading"
              @click="leaveOrDelete"
            >{{ onlyUser ? 'Delete' : 'Leave' }}</v-btn>
          </v-spacer>
        </v-card-actions>
      </div>
    </v-card>

  </v-dialog> 
</template>

<script>
import { getUsers } from '../../../AddTrip/AddTripService';
import UserStore from "../../../../stores/UserStore";
import { editTrip } from '../../TripService';
import { deleteTripFromList } from '../../../Trips/OldTripsService';
import GenericCombobox from "../../../../components/GenericCombobox/GenericCombobox";
import { getChatWithUsers, createChat } from '../../../App/Chat/ChatService';
import roleType from '../../../../stores/roleType';

export default {
  components: {GenericCombobox},
  props: {
    isShowing: Boolean,
    trip: Object
  },
  data() {
    return {
      isShowingDialog: false,
      userRoles: [],
      selectedUsers: [],
      users: [],
      userStore: UserStore,
      isLoading: false,
      showAlertCard: false,
        chat: null,
        chatNameToCreate: '',
        isChatButtonLoading: false,
      roleTypes: [
        {
          name: "Trip Manager",
          value: roleType.TRIP_MANAGER
        },
        {
          name: "Trip Member",
          value: roleType.TRIP_MEMBER
        },
        {
          name: "Trip Owner",
          value: roleType.TRIP_OWNER
        }
      ]
    };
  },
  methods: {
    /**
     * Updates the users in the trip with the chosen users
     */
    updateSelectedUsers(newUsers) {
      this.selectedUsers = newUsers
    },
    /**
     * Searches the users with the given name search string
     */
    searchUser: async name => await getUsers(name),
    /**
     * Attempt to get the chat with the users of the trip.
     */
    async tryToGetChat() {
      try {
        const userIds = this.trip.users.map(user => user.userId);
        this.chat = await getChatWithUsers(userIds);
      } catch (err) {
        // could not find a chat with those users ¯\_(ツ)_/¯
      }
    },
    /**
     * Called when the user clicks in the chat button
     */
    async chatButtonClicked() {
      this.isChatButtonLoading = true;
      await this.tryToGetChat(); // refresh the chat to confirm it doesn't exist
      this.isChatButtonLoading = false;
      if (!this.chatExists) {
        const creatingUserId = UserStore.data.userId;
        const userIds = this.trip.users.map(user => user.userId).filter(id => id !== creatingUserId);
        try {
          this.isChatButtonLoading = true;
          this.chat = await createChat(userIds, this.chatNameToCreate);
          this.isChatButtonLoading = false;
          this.$root.$emit("show-success-snackbar", "Created the chat", 3000);
          this.$root.$emit("add-chat");
        } catch (err) {
          // could not create the chat
          this.$root.$emit("show-error-snackbar", "Could not create the chat", 3000);
          this.isChatButtonLoading = false;
        }
      }
    },
    /**
     * Gets all users and filters out own user ID
     */
    async getAllUsers(name) {
      // Filter out user's own ID
      const users = (await getUsers(name))
        .filter(user => user.userId !== UserStore.data.userId);
      this.users = users;
      return users;
    },
    /**
     * Formats full name for combobox input
     */
    formatName(user) {
      return `${user.firstName} ${user.lastName}`;
    },
    /**
     * Either deletes a trip if only owner or leaves trip if not
     */
    async leaveOrDelete() {
      try {
        if (this.onlyUser) {
          this.isLoading = true;
          await deleteTripFromList(this.trip.tripNodeId);
          this.isLoading = false;
          this.$router.push("/trips");
        } else {
          const usersWithoutCurrent = this.trip.users
            .filter(user => user.userId !== UserStore.data.userId);
          this.isLoading = true;

          const trip = {...this.trip};
          trip.users = usersWithoutCurrent;
          await editTrip(trip);
          this.isLoading = false;
          this.$router.push("/trips");
        }
      } catch (error) {
        console.log(error)
      }
    },
    /**
     * Saves the current selected users that are in the trip
     */
    async saveUsersInTrip() {
        const oldUsers = this.trip.users;
        const oldRoles = this.trip.userRoles;
      const users = [...this.selectedUsers, UserStore.data];
      this.isLoading = true;
      this.trip.users = users;
      const ownUserRole = this.trip.userRoles.find(role => role.user.userId === UserStore.data.userId);

      // NOTE: this is mutating a prop and is not good practice, but it does get changed in the parent
      // add new user roles for other users
      this.trip.userRoles = this.userRoles.map(userRole => ({
        user: userRole.user,
        role: {
          roleType: userRole.role
        }
      }));
      // add the own user's role too
      this.trip.userRoles.push(ownUserRole);
      const newRoles = this.trip.userRoles;

      await editTrip(this.trip);
      this.isLoading = false;
      this.isShowingDialog = false;
      this.$emit("newUsers", users, oldUsers, newRoles, oldRoles);
    },
  },
  mounted() {
    this.getAllUsers();
    this.tryToGetChat();
  },
  watch: {
    /**
     * Refreshes userRoles when opening up modal
     */
    isShowingDialog(value) {
      if (value) {
        this.userRoles = [...this.trip.userRoles]
          .filter(userRole => userRole.user.userId !== UserStore.data.userId)
          .map(userRole => ({user: userRole.user, role: userRole.role.roleType}));

        this.selectedUsers = [...this.trip.users]
          .filter(user => user.userId !== UserStore.data.userId);

      }
      this.$emit("update:isShowing", value);
    },
    selectedUsers: {
      handler() {
        this.userRoles = this.selectedUsers.map(user => {
          const userRole = this.userRoles.find(userRole => userRole.user.userId === user.userId);
          if (userRole) {
            return userRole;
          } else {
            return {
              user,
              role: "TRIP_MEMBER"
            };
          }
        });
      },
      deep: true
    },
    /**
     * The function changes the value of the showing dialog
     */
    isShowing(value) {
      this.isShowingDialog = value;
    }
  },
  computed: {
    /**
     * Checks if the user is the only one in the Trip
     */
    onlyUser() {
      return this.trip.users.length === 1;
    },
    /**
     * Return whether a chat for the trip exists
     * @returns {Boolean} true if chat exists, false otherwise
     */
    chatExists() {
      return !!this.chat;
    }
  }
}
</script>

<style lang="scss" scoped>
@import "../../../../styles/_variables.scss";

#manage-trip-contents {
  padding-top: 20px;
}

.light-text {
  color: #FFF; 
}

#selected-users {
  margin-top: 25px;
  width: 100%;
}

.leave-button {
  position: absolute;
  right: 0;
  top: 7px;
}

#selected-users-title {
  text-align: left;
  color: $secondary;
}

.role-type {
  margin-left: 10px;
  flex: none;
  width: 150px;
}


.selected-user {
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
}

</style>


