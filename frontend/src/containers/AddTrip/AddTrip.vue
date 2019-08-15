<template>
  <v-card
          id="add-trip"
          class="col-lg-10 offset-lg-1"
					:flat="!!isSidebarComponent"
  >
    <h2 v-if="!isSidebarComponent">Add Trip</h2>
		<h3 v-else>Create New Subtrip</h3>
    <v-form ref="addTripForm">
      <v-text-field
              v-model="tripName"
              label="Trip Name"
              color="secondary"
              class="col-md-6"
              :rules="tripNameRules"
      >
      </v-text-field>

    <v-combobox v-if="!isSidebarComponent"
								:items="users" :item-text="formatName"
								v-model="selectedUsers" label="Users" multiple class="col-md-6"></v-combobox>


      <TripTable :tripDestinations="tripDestinations"/>

      <v-btn
              depressed
              color="secondary"
              small
              id="add-destination"
              @click="addDestination"
      >
        <v-icon>add</v-icon>
      </v-btn>


      <v-btn
              depressed
              color="error"
              id="cancel-trip-creation-btn"
              @click="$emit('cancel-trip-creation')"
      >
        Cancel
      </v-btn>
      <v-btn
              depressed
              color="secondary"
              id="add-trip-btn"
              @click="addTrip"
      >
        Create
      </v-btn>
    </v-form>
  </v-card>
</template>

<script>
  import TripTable from "../../components/TripTable/TripTable";
  import {createTrip, getAllUsers} from "./AddTripService.js";

  import UserStore from "../../stores/UserStore";
  import {editTrip} from "../Trip/TripService";

  const rules = {
    required: field => !!field || "Field required"
  };

  const tripDestination = {
    destinationId: null,
    arrivalDate: null,
    arrivalTime: null,
    departureDate: null,
    departureTime: null,
  };

  export default {
    components: {
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
        tripDestinations: [{...tripDestination, id: 0}, {...tripDestination, id: 1}],
        tripNameRules: [rules.required],
        selectedUsers: [],
      };
    },
    mounted() {
      this.getUsers();
    },
    methods: {
      async getUsers() {
        const users = (await getAllUsers())
          .filter(user => user.userId !== UserStore.data.userId);
        this.users = users;
      },
      /**
       * Adds an empty destination
       */
      addDestination() {
        this.tripDestinations.push({...tripDestination, id: this.tripDestinations.length});
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
          if (this.tripDestinations[i].destinationId === this.tripDestinations[i - 1].destinationId && this.tripDestinations[i].destinationId) {
            this.$set(this.tripDestinations[i], "destinationErrors", ["Destination is same as last destination"]);
            foundContiguousDestination = true;
            continue;
          }
          this.tripDestinations[i].destinationErrors = [];
        }
        return foundContiguousDestination;
      },
      /**
       * Validates and renders errors if there are any
       * @returns {boolean} True if fields are valid, false otherwise
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
       * Validates fields before sending a request to add a trip
       */
      async addTrip() {
        const validFields = this.validate();
        if (!validFields) return;
        // Specifies the extra users that should be added to the trip
        const userIds = this.selectedUsers.map(selectedUser => selectedUser.userId);
        const subTrip = await createTrip(this.tripName, this.tripDestinations, userIds);
        subTrip.isShowing = false;
        this.parentTrip.tripNodes.push(subTrip);
        // If this is happening on the sidebar, the new trip is a subtrip. This uses PUT to add it to parent trip.
        if (this.isSidebarComponent) {
					await editTrip(this.parentTrip);
				}
        this.$emit("close-dialog");
      },
      formatName(user) {
        return `${user.firstName} ${user.lastName}`;
      }
    }
  };
</script>

<style lang="scss" scoped>
  #add-trip {
    margin-top: 30px;

    h2 {
      text-align: center;
    }
  }

  #add-destination {
    margin-top: 10px !important;
    display: block;
    margin: 0 auto;
  }

  #add-trip-btn {
    float: right;
  }
</style>


