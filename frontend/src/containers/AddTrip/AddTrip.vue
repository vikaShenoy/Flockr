<template>
  <v-card id="add-trip" class="col-lg-10 offset-lg-1" :flat="!!isSidebarComponent">
    <h2 v-if="!isSidebarComponent">Add Trip</h2>
    <h3 id="title-subtrip" v-else>Create New Subtrip</h3>
    <v-form ref="addTripForm">
      <v-text-field
        v-model="tripName"
        label="Trip Name"
        color="secondary"
        class="col-md-6"
        :rules="tripNameRules"
      ></v-text-field>

      <GenericCombobox
        class="col-md-6"
        v-if="!isSidebarComponent"
        label="Users"
        :get-function="searchUser"
        :item-text="(user) => user.firstName + ' ' + user.lastName"
        multiple
        v-model="selectedUsers"
        @items-selected="updateSelectedUsers"
        :filter-function="user => user.userId !== userStore.data.userId"
      ></GenericCombobox>
      <ul>
        <li v-for="user in selectedUsers" v-bind:key="user.userId" class="selected-user">
          {{ formatName(user) }}
          <v-select
            v-model="user.userRole"
            label="Permission"
            :items="roleTypes"
            class="role-type"
            color="secondary"
            item-text="name"
            item-value="value"
            :rules="tripUserRules"
          ></v-select>
        </li>
      </ul>

      <TripTable
        :tripDestinations="tripDestinations"
        @updateSelectedDestination="updateSelectedDestination"
      />

      <v-btn depressed color="secondary" small id="add-destination" @click="addDestination">
        <v-icon>add</v-icon>
      </v-btn>

      <v-btn depressed color="secondary" id="add-trip-btn" @click="addTrip" :loading="isLoading">Create</v-btn>

      <v-btn
        depressed
        color="error"
        id="cancel-trip-creation-btn"
        @click="$emit('cancel-trip-creation')"
      >Cancel</v-btn>
    </v-form>
  </v-card>
</template>

<script>
import TripTable from "../../components/TripTable/TripTable";
import { createTrip, getUsers } from "./AddTripService.js";
import UserStore from "../../stores/UserStore";
import GenericCombobox from "../../components/GenericCombobox/GenericCombobox";
import roleType from "../../stores/roleType";

const rules = {
  required: field => !!field || "Field required"
};

const tripDestination = {
  destinationId: null,
  arrivalDate: null,
  arrivalTime: null,
  departureDate: null,
  departureTime: null
};

export default {
  components: {
    GenericCombobox,
    TripTable
  },
  props: {
    isSidebarComponent: {
      type: Boolean,
      required: false
    },
    parentTrip: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      tripName: "",
      tripDestinations: [
        { ...tripDestination, id: 0 },
        { ...tripDestination, id: 1 }
      ],
      tripNameRules: [rules.required],
      tripUserRules: [rules.required],
      selectedUsers: [],
      users: null,
      destinations: [],
      isLoading: false,
      userStore: UserStore,
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
  mounted() {
    this.getUsers();
  },
  methods: {
    /**
     * Updates the trip destinations variable to the trip destinations selected by the user
     */
    updateSelectedDestination(destination) {
      this.tripDestinations = destination;
    },
    /**
     * Updates the selected users variable to the selected users by the user creating the trip
     */
    updateSelectedUsers(newUsers) {
      this.selectedUsers = newUsers;
    },
    /**
     * This function searches users by the given name that is written in the Combo Box
     */
    searchUser: name => getUsers(name),

    /**
     * Gets all users and filters out the logged in user
     */
    async getUsers() {
      this.users = (await getUsers()).filter(
        user => user.userId !== UserStore.data.userId
      );
    },
    /**
     * Adds an empty destination
     */
    addDestination() {
      this.tripDestinations.push({
        ...tripDestination,
        id: this.tripDestinations.length
      });
    },
    /**
     * Iterates through destinations and check and renders error message
     * if destinations are contiguous.
     * @returns {boolean} True if contiguous destinations are found, false otherwise.
     */
    contiguousDestinations() {
      let foundContiguousDestination = false;

      this.$set(this.tripDestinations[0], "destinationErrors", []);

      for (let i = 1; i < this.tripDestinations.length; i++) {
        if (
          this.tripDestinations[i].destinationId ===
            this.tripDestinations[i - 1].destinationId &&
          this.tripDestinations[i].destinationId
        ) {
          this.$set(this.tripDestinations[i], "destinationErrors", [
            "Destination is same as last destination"
          ]);
          foundContiguousDestination = true;
          continue;
        }
        this.tripDestinations[i].destinationErrors = [];
      }
      return foundContiguousDestination;
    },
    /**
     * Validates and renders errors if there are any.
     * @returns {boolean} True if fields are valid, false otherwise.
     */
    validate() {
      const validFields = this.$refs.addTripForm.validate();
      const contiguousDestinations = this.contiguousDestinations();
      if (!validFields || contiguousDestinations) {
        return false;
      }
      return true;
    },

    /**
     * Validates fields before sending a request to add a trip.
     */
    async addTrip() {
      this.isLoading = true;
      const validFields = this.validate();
      if (!validFields) {
        this.$root.$emit('show-error-snackbar', 'Select at least 2 destinations and a trip name', 5000);
      }

      const tripDestinations = this.tripDestinations.map(tripDestination => {
        tripDestination.destinationId =
          tripDestination.destination.destinationId;
        return tripDestination;
      });

      let userIds = [];

      // Specifies the extra users that should be added to the trip
      this.selectedUsers.forEach(function(selectedUser) {
        userIds.push({
          userId: selectedUser.userId,
          role: selectedUser.userRole
        });
      });

      try {
        const subTrip = await createTrip(
          this.tripName,
          tripDestinations,
          userIds
        );

        // If this is happening on the sidebar, the new trip is a sub-trip. This adds it to parent trip.
        if (this.isSidebarComponent) {
          subTrip.isShowing = false;
          this.$emit("close-dialog");
        }

        this.$emit("newTripAdded", subTrip);
        this.isLoading = false;
      } catch (e) {
        this.$root.$emit("show-snackbar", {
          color: "error",
          message: "Error creating trip",
          time: 2000
        });
        this.isLoading = false;
      }
    },

    /**
     * Formats a users full name by their first name and last name
     */
    formatName(user) {
      return `${user.firstName} ${user.lastName}`;
    }
  }
};
</script>

<style lang="scss" scoped>
#add-trip {
  margin-top: 30px;
  min-height: 467px;
  overflow: hidden;

  h2 {
    text-align: center;
    padding-top: 10px;
  }
}

#add-destination {
  margin-top: 10px !important;
  display: block;
  margin: 0 auto;
}

#cancel-trip-creation-btn {
  float: right;
}

#add-trip-btn {
  float: right;
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


