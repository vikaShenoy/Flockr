<template>
    <span>{{ this.country }}</span>
</template>

<script>
export default {
    props: {
        country: {
            type: String
        }
    },
    data() {
        return {
            countries: null
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
        }
    },
    computed: {
        countryName() {
            return this.countries.find((c) => {
                return c.alpha2Code === this.country
            })
        }
    },
    mounted() {
        await setCountries();
    }
}
</script>

<style>

</style>
