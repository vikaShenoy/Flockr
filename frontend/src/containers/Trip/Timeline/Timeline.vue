<template>
    <div id="trip-container">
            <h2>{{trip.tripName}}</h2>
            <v-timeline>
                <TripDestination
                    v-for="(tripDestination, index) in trip.tripDestinations"
                    :key="tripDestination.destinationName"
                    :tripDestination="tripDestination"
                    :alignRight="index % 2 === 0 ? true : false"
                />
            </v-timeline>
    </div>    
</template>


<script>
import TripDestination from "./TripDestination/TripDestination";
export default {
    name: "Trip",
    props: {
        trip: {
            tripName: String,
            destinations: Object
        }
    },
    components: {
       TripDestination 
    }
}
</script>

<style lang="scss" scoped>
    #trip-container {
        width: 100%;
    }

    h2 {
        text-align: center;
        padding: 15px;
    }
</style>
