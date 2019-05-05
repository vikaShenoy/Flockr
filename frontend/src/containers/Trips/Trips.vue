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
        @click="$router.push('/trips/add')"
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
