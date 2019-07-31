<template>
    <v-combobox :items="countries" item-text="countryName" v-model="selectedValue" label="Country"></v-combobox>
</template>

<script>
import { getCountries } from './CountryService';
export default {
    props: {
      country: {
        type: Object 
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
        if (!newValue) return;
        this.$emit('change', newValue)
      }
    }
}
</script>

<style>

</style>
