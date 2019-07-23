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
                           v-if="isOwner(item.ownerId)" v-on:click="showEditDialog(item)"
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

        <EditTreasureHunt
                :toggle="showEditForm"
                :data="treasureHunt"
                :key="refreshEditForm"
                @closeEditDialog="closeEditDialog"
                @updateList="updateList"
        ></EditTreasureHunt>
    </v-flex>


</template>

<script>
import AddTreasureHunt from "./AddTreasureHunt";
import EditTreasureHunt from "./EditTreasureHunt"
import {getAllTreasureHunts} from "./TreasureHuntsService";
import moment from "moment";

export default {
    components: {AddTreasureHunt, EditTreasureHunt},
    mounted() {
        this.getTreasureHunts();
    },
    data() {
        return {
            showDialog: false,
            showEditForm: false,
            treasureHunts: [],
            treasureHunt: {},
            refreshEditForm: 0
        }
    },
    methods: {
        closeDialog() {
            this.showDialog = false
        },
        closeEditDialog() {
            this.showEditForm = false
        },
        async getTreasureHunts() {
            this.treasureHunts = await getAllTreasureHunts();
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
            console.log("in here")
            this.getTreasureHunts();
        },
        showEditDialog(treasureHunt) {
            this.treasureHunt = treasureHunt;
            this.refreshEditForm += 1;
            this.showEditForm = true;
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


