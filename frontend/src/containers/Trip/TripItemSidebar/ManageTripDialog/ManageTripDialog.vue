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
              v-for="userRole in userRoles"
              v-bind:key="userRole.user.userId"
              class="selected-user"
            >
            {{ formatName(userRole.user) }} <v-select v-model="getUserPermission" :items="roleTypes" class="role-type" item-text="name" item-value="value" color="secondary"></v-select>
            </li>
          </ul>

          <div id="selected-users">
            <v-combobox :items="users" :item-text="formatName" v-model="selectedUsers" label="Users" multiple></v-combobox>
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
import { getAllUsers } from '../../../AddTrip/AddTripService';
import UserStore from "../../../../stores/UserStore";
import { editTrip } from '../../TripService';
import { deleteTripFromList } from '../../../Trips/OldTripsService';
import roleType from '../../../../stores/roleType';

export default {
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
      isLoading: false,
      showAlertCard: false,
      roleTypes: [
        {name: "Trip Manager", value: roleType.TRIP_MANAGER},
        {name: "Trip Member", value: roleType.TRIP_MEMBER},
        {name: "Trip Owner", value: roleType.TRIP_OWNER}
      ]
    };
  },
  methods: {
    /**
     * Gets all users and filters out own user ID
     */
    async getAllUsers() {
      // Filter out user's own ID
      const users = (await getAllUsers())
        .filter(user => user.userId !== UserStore.data.userId);


      this.users = users;
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
    },
    getUserPermission(user) {
      const userRole = this.trip.userRoles.find(userRole => userRole.user === user.userId);
      return userRole.role.roleType;
    }
  },
  mounted() {
    this.getAllUsers(); 
  },
  watch: {
    /**
     * Refreshes userRoles when opening up modal
     */
    isShowingDialog(value) {
      if (value) {
        this.userRoles = [...this.trip.userRoles]
          .filter(userRole => userRole.user.userId !== UserStore.data.userId);

        this.selectedUsers = [...this.trip.users]
          .filter(user => user.userId !== UserStore.data.userId);

      }
      
      this.$emit("update:isShowing", value);
    },
    selectedUsers() {
      const userRoles = this.selectedUsers.map(user => {
        const userRole = this.userRoles.find(userRole => userRole.user.userId === user.userId);
        if (userRole) {
          return userRole;
        } else {
          return {
            
          };
        }
      });
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

.role-type {
  margin-left: 10px;
  flex: none;
  width: 150px;
}


.selected-user {
  display: flex;
  align-items: center;
}

</style>


