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
                           v-if="isOwner(item.ownerId) || isAdmin()" v-on:click="showEditDialog(item)"
                    >
                        <v-icon>edit</v-icon>
                    </v-btn>
                </div>
                <v-spacer></v-spacer>
                Ends in: {{getTimeToGo(item.endDate)}}
            </template>
            <v-card class="card">
                <v-card-text>Riddle: {{item.riddle}}</v-card-text>
                <v-card-text>Start Date: {{formatDate(item.startDate)}}<br>End Date: {{formatDate(item.endDate)}}<br>Time Zone: {{getTimeZoneYa(item.treasureHuntDestinationId)}}</v-card-text>
                <v-card-text v-if="isOwner(item.ownerId) || isAdmin()">{{getDestinationName(item.treasureHuntDestinationId)}}</v-card-text>
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
import {getAllTreasureHunts, getDestination, getTimeZone} from "./TreasureHuntsService";
import moment from "moment";
import UserStore from "../../stores/UserStore";

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

        /**
         * Hides the create treasure hunt modal
         */
        closeDialog() {
            this.showDialog = false
        },

        /**
         * Hides the edit treasure hunt modal
         */
        closeEditDialog() {
            this.showEditForm = false
        },

        /**
         * Calls the treasure hunt service to set the populate the list of treasure hunts with all valid treasure hunts
         */
        async getTreasureHunts() {
            this.treasureHunts = await getAllTreasureHunts();
        },

        /**
         * Takes in a date number retrieved from the database and converts it into a displayable string
         * @param date from treasure hunt object
         * @returns {string} formatted by moment
         */
        formatDate(date) {
            return moment(date).format("D/M/YYYY H:mm")
        },

        /**
         * Calculates the remaining time left to solve a treasure hunt, currently doesn't take into account the time zones
         * @param date - the ending time of the treasure hunt
         * @returns {string} A formatted string displaying the time remaining
         */
        getTimeToGo(date) {
            let now  = moment();
            let then = moment(date);
            let ms = moment(then,"DD/MM/YYYY HH:mm:ss").diff(moment(now,"DD/MM/YYYY HH:mm:ss"));
            let d = moment.duration(ms);
            //let s = ms.format("hh:mm:ss");
            return d.years() + " years, " + d.months() + " months, " + d.days() + " days, " + d.hours() + " hours"
        },

        /**
         * UNDER CONSTRUCTION
         * Function that takes a destinationId and returns the local timezone of that destination
         * @param destinationId - Id of a destination present in the Travel EA database
         */
        async getTimeZoneYa(destinationId) {
            let timezone = await getTimeZone(destinationId);
            return timezone;
        },

        /**
         * UNDER CONSTRUCTION
         * Function that takes a destination id and returns the name of that destination to be displayed to
         * the owner of the treasure hunt and any admin users
         */
        async getDestinationName(destinationId) {
            let destination = await getDestination(destinationId);
            console.log(destination);
            return destination.destinationName;
        },

        /**
         * Function to check if the logged in user is the owner of the treasure hunt
         * @param ownerId
         * @returns {boolean} true if the owner of the treasureHunt is the logged in user
         */
        isOwner(ownerId) {
            return localStorage.getItem("userId") == ownerId
        },

        /**
         * Function to that checks if the user is an admin or not
         */
        isAdmin() {
            return UserStore.methods.isAdmin()
        },

        /**
         * Function to update the value of the toggle, emitted from child
         * @param newVal
         */
        updateToggle(newVal) {
            this.showDialog = newVal;
        },

        /**
         * Function called from child to update the list of treasure hunts
         */
        updateList() {
            this.getTreasureHunts();
        },

        /**
         * Function to show the edit treasure hunt form
         * @param treasureHunt
         */
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


