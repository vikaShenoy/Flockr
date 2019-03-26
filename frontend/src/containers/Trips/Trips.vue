<template>
  <div class="trips-container">
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
export default {
  data() {
    return {
      trips: [
      ]
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
