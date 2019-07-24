<template>
    <v-select :items="countries" item-text="countryName" item-value="countryId" v-model="selectedValue" label="Country"></v-select>
</template>

<script>
import { getCountries } from './CountryService';
export default {
    props: {
      country: {
        type: Number
      }
    },
    data() {
        return {
            countries: null,
            selectedValue: this.country
        }
    },
    methods: {
    },
    async mounted() {
      const countryPromise = getCountries();
      this.countries = await Promise.resolve(countryPromise);
    },
    watch: {
      selectedValue: function (newValue) {
        this.$emit('change', newValue)
      }
    }
}
</script>

<style>

</style>
