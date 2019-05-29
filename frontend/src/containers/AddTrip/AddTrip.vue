<template>
  <v-card
    id="add-trip"
    class="col-lg-10 offset-lg-1"
  >

    <h2>Add Trip</h2>

    <v-form ref="addTripForm">
      <v-text-field
        v-model="tripName"
        label="Trip Name"
        color="secondary"
        class="col-md-6"
        :rules="tripNameRules"
      >
      </v-text-field>

      <TripTable :tripDestinations="tripDestinations" />

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
        color="secondary"
        id="add-trip-btn"
        @click="addTrip()" 
      >Create</v-btn>
    </v-form>
  </v-card>
</template>

<script>
import TripTable from "../../components/TripTable/TripTable";
import { addTrip } from "./AddTripService.js";

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
  data() {
    return {
     tripName: "",
     tripDestinations: [{...tripDestination, id: 0}, {...tripDestination, id: 1}],
      tripNameRules: [rules.required],
    };
  },
  methods: {
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
      if (!validFields || contiguousDestinations)  {
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
      try {
        
        const travellerId = this.$route.params.travellerId;
        // Use travellerId from URL only if creating a trip for another traveller
        const userId = travellerId ? travellerId : localStorage.getItem("userId");
        const tripId = await addTrip(this.tripName, this.tripDestinations, userId);

        // If admin is adding trip, go to admin trip page
        if (travellerId) {
          this.$router.push(`/travellers/${travellerId}/trips/${tripId}`); 
        } else {
          this.$router.push(`/trips/${tripId}`); 
        }
      } catch (e) {
        console.log(e);
        // Add error handling here later
      }
    },
    /**
     * Sets the trip destinations list to empty.
     */
    update(hello) {
      this.tripDestinations = [];

    }
  }
};
</script>

<style lang="scss" scoped>
#add-trip {
  margin-top: 30px;
  padding: 20px;

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


