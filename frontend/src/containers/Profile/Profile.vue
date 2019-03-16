<template>

  <div>
    <v-select 
        v-model="travellerTypeSelected"
        :items="travellerTypes"
        color="secondary"
        label="Traveller Type"
      >
    </v-select>

    <v-btn color="secondary" depressed @click="addTravellerType()">Add</v-btn>
  
    <li v-for="selectedType in selectedTypes" v-bind:key="selectedType">
      {{ selectedType }} <v-btn @click="deleteTravellerType(selectedType)">Delete</v-btn>
    </li>
    
  </div>
</template>

<script>
export default {
  data() {
    return {
      travellerTypes: ["Backpacker", "Somethingelse"],
      selectedTypes: [],
      travellerTypeSelected: null,
    };
  },
  methods: {
    addTravellerType() {
      this.travellerTypes.splice(this.travellerTypes.indexOf(this.travellerTypeSelected), 1);
      this.selectedTypes.push(this.travellerTypeSelected);
      this.travellerTypeSelected = null;
    },
    deleteTravellerType(selectedType) {
      console.log(selectedType);
      this.selectedTypes.splice(this.selectedTypes.indexOf(selectedType), 1); 
      this.travellerTypes.push(selectedType);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>


