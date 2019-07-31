<template>
  <span v-if="this.country.isValid"> {{ this.country.countryName }}</span>
  <span class="invalid" v-else>Formerly {{ this.country.countryName }}</span>
</template>

<script>

  export default {
    props: {
      country: {
        type: Object 
      }
    },
    data() {
      return {
        countries: null
      }
    }
  }
</script>

<style scoped>
  .invalid {
    color: red;
    font-weight: bold;
    font-style: italic;
  }
</style>