<template>
  <div id="trip-container">
    <v-btn
      color="secondary"
      id="edit-trip"
      @click="goToEditTrip"
      depressed
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
        const travellerId = this.$route.params.travellerId;
        // If the admin is viewing another user, use them. 
        const userId = travellerId ? travellerId : localStorage.getItem("userId");
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
    },
    goToEditTrip() {
      const travellerId = this.$route.params.travellerId;
      const tripId = this.$route.params.id;

      if (travellerId) {
        this.$router.push(`/travellers/${travellerId}/trips/${tripId}/edit`)
      } else {
        this.$router.push(`/trips/${tripId}/edit`);
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