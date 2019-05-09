<template>
  <div class="trips-container">

    <TripList :userId="userId" />

    <v-btn
      id="add-trip"
      fab
      dark
      color="secondary"
    >
      <v-icon
        dark
        @click="goToAddTrip"
      >add</v-icon>
    </v-btn>
  </div>
</template>


<script>
import TripList from "../../components/TripList/TripList";

export default {
  components: {
    TripList
  },
  data() {
    return {
      // Used to know what user to get trips from
      userId: localStorage.getItem("userId")
    };
  },
  mounted() {
    const travellerId = this.$route.params.travellerId;

    if (travellerId) {
      this.getTrips(travellerId);
    } else {
      this.getTrips();
    }
  },
  methods: {
    /**
     * Gets a trip for a specific user
     * @param {number | undefined} travellerId The travellerId to get trips from or undefined if viewing own trips
     */
    async getTrips(travellerId) {
      try {
        // Select own userId if not an admin viewing another traveller's trips
        const userId = travellerId ? travellerId : localStorage.getItem("userId");
        const trips = await getTrips(userId);
        const tripsTransformed = transformTrips(trips);

        this.trips = tripsTransformed;
      } catch (e) {
        console.log(e);
        // Add error handling later
      }
    },
    /**
     * If you're an admin, go to page to create trips for other users
     * If you're not an admin, go to create trips page for yourself
     */
    goToAddTrip() {
      const travellerId = this.$route.params.travellerId;

      if (travellerId) {
        this.$router.push(`/travellers/${travellerId}/trips/add`);
      } else {
        this.$router.push("/trips/add");
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.trips-container {
  width: 100%;
  padding-top: 15px;
  padding-bottom: 15px;
}
#add-trip {
  position: fixed;
  right: 30px;
  bottom: 30px;
}
</style>
