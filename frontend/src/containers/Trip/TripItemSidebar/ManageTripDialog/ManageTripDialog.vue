<template>
  <v-dialog width="600px" v-model="isShowingDialog">
    <v-card id="manage-trip-dialog">
           <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">Manage Trip</h2>
          </v-spacer>
        </v-layout>

      </v-card-title>

    <div id="manage-trip-contents">
      <v-container grid-list-md text-center>
      <v-layout wrap>
        <v-flex xs10 offset-xs1>
          <v-combobox id="selected-users" :items="users" :item-text="formatName" v-model="selectedUsers" label="Users" multiple></v-combobox>
        </v-flex>
      </v-layout>
      </v-container>
      
      <v-card-actions>
        <v-spacer align="right">
          <v-btn
            flat
            color="secondary"
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
  </v-dialog> 
</template>

<script>
import { getAllUsers } from '../../../AddTrip/AddTripService';
import UserStore from "../../../../stores/UserStore";
import { editTrip } from '../../TripService';

export default {
  props: {
    isShowing: Boolean,
    trip: Object
  },
  data() {
    return {
      isShowingDialog: false,
      // copy selected users
      selectedUsers: [],
      users: [],
      isLoading: false 
    };
  },
  methods: {
    async getAllUsers() {
      // Filter out user's own ID
      const users = (await getAllUsers())
        .filter(user => user.userId !== UserStore.data.userId);


      this.users = users;
    },
    formatName(user) {
      return `${user.firstName} ${user.lastName}`;
    },
    async saveUsersInTrip() {
      const users = [...this.selectedUsers, UserStore.data]
      this.isLoading = true;
      await editTrip(this.trip.tripId, this.trip.tripName, this.trip.tripDestinations, users);
      this.isLoading = false;
      this.$emit("newUsers");
      this.isShowingDialog = false;
    }
  },
  mounted() {
    this.getAllUsers(); 
  },
  watch: {
    isShowingDialog(value) {
      this.selectedUsers = [...this.trip.users]
        .filter(user => user.userId !== UserStore.data.userId);
      
      this.$emit("update:isShowing", value);
    },
    isShowing(value) {
      this.isShowingDialog = value;
    }
  }
}
</script>

<style lang="scss" scoped>

#manage-trip-contents {
  padding-top: 20px;
}

.light-text {
  color: #FFF; 
}

#selected-users {
  width: 100%;
}
</style>


