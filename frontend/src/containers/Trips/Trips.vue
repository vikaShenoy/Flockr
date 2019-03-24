<template>
  <div>     
    <v-btn id="add-trip" fab dark color="secondary">
      <v-icon dark @click="$router.push('/trips/add')">add</v-icon>
    </v-btn>
  </div>
</template>


<script>
export default {
  data() {
    return {

    };
  }  
}
</script>

<style lang="scss" scoped>
  #add-trip {
    position: fixed;
    right: 30px;
    bottom: 30px;
  }
</style>
