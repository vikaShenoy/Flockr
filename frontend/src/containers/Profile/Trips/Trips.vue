<template>
    <div>
        <div id="header">
            <h3 class="header-text">Trips</h3>
        </div>
        <v-card v-if="trips" id="trips-card" max-height="315px" class="scroll">
            <h3 v-if="!trips.length"><v-icon>directions_walk</v-icon> No Trips Available</h3>
            <TripItem v-for="trip in trips" v-bind:key="trip.tripId" :trip="trip" class="trip-filter"/>
        </v-card>
    </div>
</template>

<script>
import { getTrips, transformTrips } from "../../Trips/TripsService";
import TripItem from '../../Trips/TripItem/TripItem.vue'

export default {
    components: {
        TripItem
    },
    data() {
        return {
            trips: null
        };
    },
    mounted() {
        this.getTrips();
    },
    methods: {
        /**
         * Gets all the user's trips
         */
        async getTrips() {
            try {
                const userId = localStorage.getItem("userId");
                const trips = await getTrips(userId);
                this.trips = transformTrips(trips);
            } catch (e) {
                console.log(e);
            }
        }
    }
};
</script>

<style lang="scss" scoped>
.header-text {
    margin-top: 10px;
    margin-bottom: 10px;
}

#header {
    width: 100%;
    display: flex;
    flex-flow: row nowrap;
    justify-content: center;
    align-items: center;
    > * {
        flex-grow: 1;
    }
}

#trips-card {
    padding: 10px 5px 10px 5px;
}

.trip-filter {
    margin-bottom: 17px;
}

.scroll {
    overflow-y: auto;
}
</style>


