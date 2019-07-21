<template>

    <v-flex>
          <v-expansion-panel class="panel">
            <v-expansion-panel-content
            v-for="(item, i) in treasureHunts"
            :key="i"
            class="content"
            >
            <template v-slot:header>
                <div id="header">{{item.treasureHuntName}}
                    <v-btn :visible="true" color="secondary" style="" fab small
                           v-on:hover="showDialog = true" v-if="isOwner(item.ownerId)"
                    >
                        <v-icon>edit</v-icon>
                    </v-btn>
                </div>
                <v-spacer></v-spacer>
                Ends in: {{getTimeToGo(item.endDate)}}
            </template>
            <v-card class="card">
                <v-card-text>Riddle: {{item.riddle}}</v-card-text>
                <v-card-text>Start Date: {{formatDate(item.startDate)}}<br>End Date: {{formatDate(item.endDate)}}<br>Time Zone: {{getTimeZone(item.treasureHuntDestinationId)}}</v-card-text>
            </v-card>
            </v-expansion-panel-content>
        </v-expansion-panel>
        <v-btn color="secondary" style="margin: 20px; float: right" fab small
        v-on:click="showDialog = true"
        >
            <v-icon>add</v-icon>
        </v-btn>

        <AddTreasureHunt
                :toggle="showDialog"
                @closeDialog="closeDialog"
                @updateToggle="updateToggle"
                @updateList="updateList"
        ></AddTreasureHunt>
    </v-flex>


</template>

<script>
import AddTreasureHunt from "./AddTreasureHunt";
import {getAllTreasureHunts} from "./TreasureHuntsService";
import moment from "moment";

export default {
    components: {AddTreasureHunt},
    mounted() {
        this.getTreasureHunts();
    },
    data() {
        return {
            showDialog: false,
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
    },
    methods: {
        closeDialog() {
            this.showDialog = false
        },
        async getTreasureHunts() {
            this.treasureHunts = await getAllTreasureHunts();
            console.log(this.treasureHunts)
        },
        formatDate(date) {
            return moment(date).format("D/M/YYYY H:mm")
        },
        getTimeToGo(date) {
            let now  = moment();
            let then = moment(date);
            let ms = moment(then,"DD/MM/YYYY HH:mm:ss").diff(moment(now,"DD/MM/YYYY HH:mm:ss"));
            let d = moment.duration(ms);
            //let s = ms.format("hh:mm:ss");
            return d.years() + " years, " + d.months() + " months, " + d.days() + " days, " + d.hours() + " hours"
        },
        getTimeZone(destinationId) {
            console.log("getting time zone")
        },
        isOwner(ownerId) {
            return localStorage.getItem("userId") == ownerId
        },
        updateToggle(newVal) {
            this.showDialog = newVal;
        },
        updateList() {
            this.getTreasureHunts();
        }
    }

}
</script>

<style lang="scss" scoped>
    @import "../../styles/_variables.scss";

    #header {
        color: $secondary;
        font-weight: bold;
    }

    .testing {
        background-color: $darker-white !important;
    }

</style>


