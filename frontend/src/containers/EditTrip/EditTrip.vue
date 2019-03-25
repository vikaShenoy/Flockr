<template>
  <v-card
    id="edit-trip"
    class="col-lg-10 offset-lg-1"
  >

    <h2>Edit Trip</h2>

    <v-form ref="editTripForm">
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
        id="edit-trip-btn"
        @click="editTrip()" 
      >Save</v-btn>
    </v-form>
  </v-card>
</template>

<script>
import TripTable from "../../components/TripTable/TripTable";
import { transformTripResponse, editTrip, getTrip } from "./EditTripService.js";

const rules = {
  required: field => !!field || "Field required" 
};

// empty tripDestinations to add when the user presses "+"
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
     tripDestinations: [],
     tripNameRules: [rules.required],
    };
  },
  mounted() {
    this.getTrip();
  },
  methods: {
    /**
     * Gets a users trip for editing
     */
    async getTrip() {
      try {
        const tripId = this.$route.params.id;
        const trip = await getTrip(tripId);

        const { tripName, tripDestinations } = transformTripResponse(trip);

        this.tripName = tripName;
        this.tripDestinations = tripDestinations;
      } catch (e) {
        console.log(e);
        // Add error handling later
      }
    },
    /**
     * Adds an empty destination
     */
    addDestination() {
      // ... spread operator clones object instead of referencing
      this.tripDestinations.push({...tripDestination});
    },
    /**
     * Iterates through destinations and check and renders error message
     * if destinations are contiguous
     */
    contiguousDestinations() {
      let foundContiguousDestination = false;
      for (let i = 1; i < this.tripDestinations.length; i++) {

        if (this.tripDestinations[i].destinationId === this.tripDestinations[i - 1].destinationId) {
          this.$set(this.tripDestinations[i], "destinationErrors", ["Destination is same as last destination"]);
          foundContiguousDestination = true;
          continue;
        }
        this.tripDestinations[i].destinationErrors = [];
      }
      return foundContiguousDestination;
    },
    /**
     * Validates fields before sending a request to add a trip
     */
    async editTrip() {
      const validFields = this.$refs.editTripForm.validate();
      const contiguousDestinations = this.contiguousDestinations();
      if (!validFields || contiguousDestinations)  {
        return;
      }

      try {
        const tripId = this.$route.params.id;
        await editTrip(tripId, this.tripName, this.tripDestinations);
      } catch (e) {
        console.log(e);
        // Add error handling here later
      }
    }
  }
};
</script>

<style lang="scss" scoped>
#edit-trip {
  align-self: center;
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

#edit-trip-btn {
  float: right;
}
</style>


