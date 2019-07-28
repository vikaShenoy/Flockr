<template>
  <div>
    <div id="header">
      <h3 class="header-text">Trips</h3>
    </div>
    <v-card
      id="trips-card"
      max-height="320px"
    >
      <TripList :userId="userId" :viewOnly="true"/>
    </v-card>
  </div>
</template>

<script>

import TripList from "../../../components/TripList/TripList"; 

export default {
  components: {
    TripList
  },
  data() {
    return {
      // Tells TripList what user to get trips from
      userId: this.$route.params.id
    };
  }
};
</script>

<style lang="scss" scoped>
.header-text {
  margin-top: 10px;
  margin-bottom: 10px;
}

#header {
  width: 100%;
  margin-top: 20px;
  display: flex;
  flex-flow: row nowrap;
  justify-content: center;
  align-items: center;
  > * {
    flex-grow: 1;
  }

  h3 {
    text-align: left;
  }
}

#trips-card {
  padding: 10px 5px 10px 5px;
}

.trip-filter {
  margin-bottom: 17px;
}

.scroll {
  overflow-y: auto;
}
</style>


