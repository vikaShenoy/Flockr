<template>
    <v-select :items='countries' :item-text='name' :v-model='selectedValue'></v-select>
</template>

<script>
import { getCountries } from './CountryService';
export default {
    data() {
        return {
            countries: null,
            selectedValue: null
        }
    },
    methods: {
        async setCountries() {
            try {
                const countries = await getCountries();
                this.countries = countries;
            } catch (e) {
                // add handling later
            }
        },
        async emitChange() {
            this.$emit('change', this.selectedValue);
        }
    },
    mounted() {
        await setCountries();
    }
}
</script>

<style>

</style>
