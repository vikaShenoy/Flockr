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
          <h4 id="selected-users-title">Selected users</h4>

          <ul>
            <li
              v-for="user in selectedUsers"
              v-bind:key="user.userId"
            >
            {{ formatName(user) }}
            </li>
          </ul>

          <div id="selected-users">

            <GenericCombobox
              label="Users"
              :get-function="searchUser"
              :item-text="(user) => user.firstName + ' ' + user.lastName"
              multiple
              v-model="selectedUsers"
              @items-selected="updateSelectedUsers"
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
        <v-btn @click="leaveOrDelete" class="red--text leave-button" flat>{{ onlyUser ? "Delete" : "Leave" }}</v-btn>
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
                    color="success"
                    flat
                    :loading="isLoading"
                    @click="leaveOrDelete"
            >Continue</v-btn>
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

export default {
  components: {GenericCombobox},
  props: {
    isShowing: Boolean,
    trip: Object
  },
  data() {
    return {
      isShowingDialog: false,
      selectedUsers: [],
      users: [],
      isLoading: false,
      showAlertCard: false
    };
  },
  methods: {

    updateSelectedUsers(newUsers) {
      this.selectedUsers = newUsers
    },

    searchUser: async name => await getUsers(name),

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
    },
    /**
     * Saves the current selected users that are in the trip
     */
    async saveUsersInTrip() {
      const users = [...this.selectedUsers, UserStore.data]
      this.isLoading = true;
      this.trip.users = users;
      await editTrip(this.trip);
      this.isLoading = false;
      this.isShowingDialog = false;
      this.$emit("newUsers", users);
    }
  },
  mounted() {
    this.getAllUsers(); 
  },
  watch: {
    /**
     * Refreshes selectedUsers when opening up modal
     */
    isShowingDialog(value) {
      if (value) {
        this.selectedUsers = [...this.trip.users]
          .filter(user => user.userId !== UserStore.data.userId);
      }
      
      this.$emit("update:isShowing", value);
    },
    isShowing(value) {
      this.isShowingDialog = value;
    }
  },
  computed: {
    onlyUser() {
      return this.trip.users.length === 1;
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

</style>


