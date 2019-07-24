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
      countryIsValid() {
        const foundCountry = this.countries.find((c) => {
          return c.countryName === this.country
        });
        this.isValid = foundCountry.isValid;
      }
    },
    computed: {
    },
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
