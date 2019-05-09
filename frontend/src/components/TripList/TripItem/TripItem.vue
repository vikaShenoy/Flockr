<template>
  <div>
    <v-card class="trip-item" @click="$router.push(`/trips/${trip.tripId}`)">
    <div class="status">
      <v-icon v-if="trip.status === 'Upcoming'" style="font-size: 40px;color: #FFF;">flight_takeoff</v-icon>
      <v-icon v-else-if="trip.status === 'Passed'" style="font-size: 40px;color: #FFF;">flight_landing</v-icon>
      <v-icon v-else-if="trip.status === 'Ongoing'" style="font-size: 40px;color: #FFF;">flight</v-icon>
      <div v-else style="width: 40px"></div>
      
    </div>

      <div class="content">
        <h2>{{ trip.tripName }}</h2>

        <b>Status: </b> <span>{{ trip.status }}</span>
      </div>

     
    </v-card>  
  </div>
</template>

<script>
export default {
  props: ["trip"]
  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";
  .trip-item {
    margin: 15px;
    cursor: pointer;
    height: 80px;
    
    &:hover {
      background-color: #f4f4f4;
    }

    .status {
      display: flex;
      align-items: center;
      padding-left: 5px;
      padding-right: 5px;
      float: right;
      height: 100%;
      background-color: $primary;
      color: #FFF;
      width: 50px;
    }

    } 

    .content {
      padding-top: 10px;
      padding-left: 10px;
    }



</style>



