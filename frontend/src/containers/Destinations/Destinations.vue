<template>
  <div>
    <v-card
      v-for="dest in destinations"
      v-bind:key="dest.id"
      >
      <v-card-title><h2>{{ dest.name }}</h2></v-card-title>
      </v-card>
      </div>
</template>

<script>
export default {
  data() {
    return {
      destinations: [
        {
          "id": 15043,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15044,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15045,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        }
      ]
    }
  },
  methods: {
    // Methods here
  }
}
</script>

<style lang="scss" scoped>

</style>




