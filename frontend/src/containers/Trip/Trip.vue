<template>
  <div id="trip-container">
    <v-btn
      color="secondary"
      id="edit-trip"
      @click="$router.push(`/trips/${$route.params.id}/edit`)"
    >Edit</v-btn>


    <Timeline :trip="trip" v-if="trip"/>
  </div>
</template>

<script>
import Timeline from "./Timeline/Timeline";
import { getTrip, transformTrip } from "./TripService.js";

export default {
  components: {
    Timeline
  },
  data() {
    return {
      trip: null
    };
  },
  mounted() {
    this.getTrip();
  },
  methods: {
    /**
     * Sends request to get trip and then transforms trip to the right format
     */
    async getTrip() {
      try {
        const tripId = this.$route.params.id;
        const userId = localStorage.getItem("userId");
        const rawTrip = await getTrip(userId, tripId);
        const transformedTrip = transformTrip(rawTrip); 
        this.trip = transformedTrip;
      } catch (e) {
        console.log(e);
        if (e.status === 404) {
          this.$router.go(-1);
        } else {
          // Add error handling later
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
  #trip-container {
        width: 100%;
    }

    #edit-trip {
      float: right;
      margin-top: 10px;
      z-index: 1;
      margin-right: 10px;
    }
</style>