<template>
    <v-flex>
          <v-expansion-panel>
            <v-expansion-panel-content
            v-for="(item, i) in treasureHunts"
            :key="i"
            >
            <template v-slot:header>
                <div>{{item.name}}</div>
            </template>
            <v-card>
                <v-card-text>{{item.riddle}}</v-card-text>
            </v-card>
            </v-expansion-panel-content>
        </v-expansion-panel>
    </v-flex>
</template>

<script>
export default {

    data() {
        return {
            treasureHunts: [
                {
                    name: "The curse of the Krusty Krab",
                    riddle: "Where is the Krusty Krab located?",
                    timezone: "GMC+13",
                    destination: "Bikini Bottom"
                },
                {
                    name: "Pointy stick",
                    riddle: "This city has a pointy stick in it that recently had Saturn V projected onto it",
                    timezone: "GMC+13",
                    destination: "Washington DC"
                },
                {
                    name: "A place to drink",
                    riddle: "Live band, a fat man, scissorhands",
                    timezone: "GMC+13",
                    destination: "Fat Eddies"
                },
                {
                    name: "Treasure",
                    riddle: "People live there, work there, study there, cry there, eat there",
                    timezone: "GMC+13",
                    destination: "Erskine"
                }
            ]
        }
    }
    
}
</script>

<style>

</style>


