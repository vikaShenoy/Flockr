<template>
  <v-dialog width="600px" v-model="isShowingDialog">
    <v-card>
           <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">Manage Trip</h2>
          </v-spacer>
        </v-layout>

      </v-card-title>

    <v-combobox :items="users" :item-text="formatName" v-model="selectedUsers" label="Users" multiple class="col-md-6"></v-combobox>
      
    </v-card>
  </v-dialog> 
</template>

<script>
import { getAllUsers } from '../../../AddTrip/AddTripService';
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
      users: []
    };
  },
  methods: {
    async getAllUsers() {
      const users = await getAllUsers();
      this.users = users;
    }
  },
  mounted() {
    
  },
  watch: {
    isShowingDialog() {
      this.selectedUsers = [...this.trip.users];
      this.$emit("update:isShowing", value);
    },
    isShowing(value) {
      this.isShowingDialog = value;
    }
  }
}
</script>

<style lang="scss" scoped>
.light-text {
  color: #FFF; 
}
</style>


