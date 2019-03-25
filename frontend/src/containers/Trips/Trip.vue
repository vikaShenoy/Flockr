<template>
    <div class="trip-container">
        <v-card>
            <h2>{{trip.tripName}}</h2>
            <v-timeline>
                <TimelineDestination
                    v-for="(destination, index) in trip.destinations"
                    :key="destination.destinationName"
                    :destination="destination"
                    :alignRight="index % 2 === 0 ? true : false"
                />
            </v-timeline>
        </v-card>
    </div>    
</template>


<script>
import TimelineDestination from './TimelineDestination';
export default {
    name: 'Trip',
    props: {
        trip: {
            tripName: String,
            destinations: Object
        }
    },
    components: {
        TimelineDestination
    }
}
</script>

<style lang="scss" scoped>
    .trip-container {
        width: 100%;
    }

    h2 {
        text-align: center;
        padding: 15px;
    }
</style>