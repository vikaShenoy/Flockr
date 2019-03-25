<template>
    <div>
        <v-timeline-item :style="{width: '90%', marginLeft: '5%'}" color="primary" small :right="alignRight">
            <v-card>
                <v-card-title class="primary">
                    <h3 class="white--text font-weight-light">{{destination.destinationName}}</h3>
                </v-card-title>
                <v-container>
                    <v-layout>
                        <v-flex xs2>
                            <v-icon size="30">flight_takeoff</v-icon>
                        </v-flex>
                        <v-flex xs10>
                            <p>{{destination.arrivalDate}}</p>
                        </v-flex>
                    </v-layout>
                    <v-layout>
                        <v-flex xs2>
                            <v-icon size="30">flight_landing</v-icon>
                        </v-flex>
                        <v-flex xs10>
                            <p>{{destination.departureDate}}</p>
                        </v-flex>
                    </v-layout>
                </v-container>
            </v-card>
        </v-timeline-item>
    </div>
</template>


<script>
export default {
    name: 'TimelineDestination',
    props: {
        destination: {
            destinationName: String,
            arrivalDate: String,
            departureDate: String,
        },
        alignRight: Boolean
    }
}
</script>
