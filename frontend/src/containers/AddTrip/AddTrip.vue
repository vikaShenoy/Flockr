<template>
  <v-card
    id="add-trip"
    class="col-lg-10 offset-lg-1"
  >

    <h2>Add Trip</h2>

    <v-form>
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

export default {
  components: {
    TripTable
  },
  data() {
    return {
     tripName: "",
     tripDestinations: [
        {
          destinationId: null,
          arrivalDate: "",
          arrivalTime: "",
          departureDate: "",
          departureTime: "",
        }
      ],
      tripNameRules: [rules.required] 
    };
  },
  methods: {
    addDestination() {
      this.tripDestinations.push({
        destinationId: null,
        arrivalDate: "",
        arrivalTime: "",
        departureDate: "",
        departureTime: ""
      });
    },
    async addTrip() {
      try {
        await addTrip(this.tripName, this.tripDestinations);
      } catch (e) {
        console.log(e);
        // Add error handling here later
      }
    }
  }
};
</script>

<style lang="scss" scoped>
#add-trip {
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

#add-trip-btn {
  float: right;
}
</style>


