<template>
  <div v-if="!isAddingATrip" class="trips-container" :key="tripListKey">
    <TripList :userId="userId" />

    <v-btn
      id="add-trip-button"
      fab
      dark
      color="secondary"
    >
      <v-icon
        dark
        @click="isAddingATrip = true"
      >add</v-icon>
    </v-btn>
  </div>

  <AddTrip
    v-else
    @new-trip="newTrip"
    @cancel-trip-creation="isAddingATrip = false"
  />
</template>


<script>
import TripList from "../../components/TripList/TripList";
import AddTrip from "../AddTrip/AddTrip";

export default {
  components: {
    AddTrip,
    TripList
  },
  data() {
    return {
      // Used to know what user to get trips from
      userId: localStorage.getItem("userId"),
      isAddingATrip: false,
      tripListKey: 0 // used to force rerenders of the component when trips are added
    };
  },
  methods: {
    /**
     * Called when the list of trips changes and we need the list of trips
     * to be updated.
     */
    newTrip() {
      this.tripListKey += 1; // force re render of trips list
      this.isAddingATrip = false;
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
#add-trip-button {
  position: fixed;
  right: 30px;
  bottom: 30px;
}
</style>
