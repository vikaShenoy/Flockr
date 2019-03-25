<template>
    <div class="trips-container">
        <Trip 
            v-for="trip in trips"
            :key="trip.name"
            :trip="trip" />
    </div>
</template>


<script>
import Trip from './Trip';
export default {
    data() {
        return {
            trips: [
                {
                    tripName: 'Visiting big cities',
                    destinations: [
                        {
                            destinationName: 'Dunedin',
                            arrivalDate: '25/03/19 at 3:45PM',
                            departureDate: '29/03/19 at 7:45AM'
                        },
                        {
                            destinationName: 'Ottawa',
                            arrivalDate: '25/03/19 at 3:45PM',
                            departureDate: '29/03/19 at 7:45AM'
                        },
                        {
                            destinationName: 'Santiago',
                            arrivalDate: '25/03/19 at 3:45PM',
                            departureDate: '29/03/19 at 7:45AM'
                        }
                    ]
                }
            ]
        }
    },
    components: {
        Trip
    }
}
</script>

<style lang="scss" scoped>
    .trips-container {
        width: 100%;
        padding-top: 15px;
        padding-bottom: 15px;
    }
</style>