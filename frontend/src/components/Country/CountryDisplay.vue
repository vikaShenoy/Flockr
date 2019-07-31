<template>
  <span v-if="isValid"> {{ this.country }}</span>
  <span class="invalid" v-else>Formerly {{ this.country }}</span>
</template>

<script>
  import {getCountries} from "./CountryService";

  export default {
    props: {
      country: {
        type: String
      }
    },
    data() {
      return {
        countries: null,
        isValid: false
      }
    },
    methods: {
      /**
       * Check whether a country is valid by looking for it in the master
       * country list.
       * Set the valid flag.
       */
      countryIsValid() {
        const foundCountry = this.countries.find((c) => {
          return c.countryName === this.country
        });
        this.isValid = foundCountry.isValid;
      }
    },
    computed: {},
    /**
     * Populate the list of countries from backend.
     * Check whether the country being displayed in valid.
     * @returns {Promise<void>}
     */
    async mounted() {
      const countryPromise = getCountries();
      this.countries = await Promise.resolve(countryPromise);
      this.countryIsValid();
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
