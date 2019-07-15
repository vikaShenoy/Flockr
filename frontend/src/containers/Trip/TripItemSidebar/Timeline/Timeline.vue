<template>
    <div id="trip-container">
            <v-timeline
              dense
            >
                <!--data-destinationId is used to disable sorting items with the same destinationID-->
                <TripDestination
                    v-for="tripDestination in trip.tripDestinations"
                    :key="tripDestination.destinationName"
                    :tripDestination="tripDestination"
                    alignRight
                />
            </v-timeline>
    </div>    
</template>


<script>
import Sortable from "sortablejs";
import TripDestination from "./TripDestination/TripDestination";
export default {
    name: "Trip",
    props: {
        trip: {
            tripName: String,
            tripDestinations: Object
        }
    },
    components: {
       TripDestination 
    },
    mounted() {
        this.initSorting();
    },
    methods: {
        initSorting() {
        const table = document.querySelector(".v-timeline");
        Sortable.create(table, {
            animation: 150,
            onEnd: ({ newIndex, oldIndex }) => {
                this.$emit("destinationOrderChanged", {newIndex, oldIndex});
            }
        });
        }
   },
    watch: {
        data(newData)  {
            console.log(newData);
        }
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
