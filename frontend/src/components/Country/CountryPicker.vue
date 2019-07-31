<template>
    <v-combobox :items="countries" item-text="countryName" item-value="countryId" v-model="selectedValue" label="Country"></v-combobox>
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
      this.countries = await getCountries();
      this.countries = this.countries.filter(e => e.isValid === true);
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
