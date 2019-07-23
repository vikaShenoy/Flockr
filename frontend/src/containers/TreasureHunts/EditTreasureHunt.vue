<template>
    <v-dialog
            v-model="visible"
            width="500"
    >

        <v-tabs
                centered
                color="secondary"
                dark
                icons-and-text
                hide-slider
        >

            <v-tab href="#tab-1">
                Edit Treasure Hunt
                <v-icon>zoom_out_map</v-icon>
            </v-tab>

            <v-tab-item
                    key="1"
                    value="tab-1"
            >
                <v-card flat>
                    <v-card-text>
                        <v-form
                                ref="treasureHuntForm"
                        >
                            <v-container grid-list-md>
                                <v-layout wrap>

                                    <v-flex xs12>
                                        <v-text-field v-model="editTreasureHuntName" label="Name" required ></v-text-field>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-select v-model="editTreasureHuntDestination" required label="Destination" :items="destinations" item-text="destinationName" item-value="destinationId">

                                        </v-select>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-textarea v-model="editTreasureHuntRiddle" label="Riddle" required></v-textarea>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-text-field
                                                v-model="startDate"
                                                label="Start Date"
                                                prepend-icon="event"
                                                type="date"
                                        ></v-text-field>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-text-field
                                                v-model="endDate"
                                                label="End Date"
                                                prepend-icon="event"
                                                type="date"
                                        ></v-text-field>
                                    </v-flex>
                                </v-layout>
                            </v-container>
                        </v-form>
                    </v-card-text>
                    <v-card-actions>
                        <v-spacer></v-spacer>
                        <v-btn color="blue darken-1" flat @click="closeDialog">Close</v-btn>
                        <v-btn color="blue darken-1" :disabled="validTreasureHunt" flat v-on:click="editTreasureHunt()" >Update</v-btn>
                    </v-card-actions>
                </v-card>

            </v-tab-item>
        </v-tabs>



    </v-dialog>
</template>

<script>
    import moment from "moment";
    import {getPublicDestinations, editTreasureHunt} from "./TreasureHuntsService"
    export default {
        name: "EditTreasureHunt",
        props: {
            toggle: Boolean,
            data: Object
        },
        mounted() {
            this.getDestinations();
            this.editTreasureHuntName = this.data.treasureHuntName;
            this.editTreasureHuntRiddle = this.data.riddle;
            this.editTreasureHuntDestination = this.data.treasureHuntDestinationId;
            this.startDate = moment(this.data.startDate).format("YYYY-MM-DD");;
            this.endDate = moment(this.data.endDate).format("YYYY-MM-DD");
            this.visible = this.toggle;


        },
        data() {
            return {
                visible: false,
                destinations: [],
                editTreasureHuntName: "",
                editTreasureHuntDestination: -1,
                editTreasureHuntRiddle: "",
                startDate: null,
                endDate: null,

            }
        },
        methods: {
            closeDialog() {
                this.$emit("closeEditDialog");
                this.editTreasureHuntName = "";
                this.editTreasureHuntRiddle = "";
                this.editTreasureHuntDestination = null;
                this.startDate = null;
                this.endDate = null;
            },
            async getDestinations() {
                this.destinations = await getPublicDestinations()
            },
            async editTreasureHunt() {
                let treasureHunt = {

                    treasureHuntId: this.data.treasureHuntId,
                    treasureHuntName: this.editTreasureHuntName,
                    treasureHuntDestinationId: this.editTreasureHuntDestination,
                    riddle: this.editTreasureHuntRiddle,
                    startDate: this.startDate,
                    endDate: this.endDate

                };
                await editTreasureHunt(treasureHunt);

                this.closeDialog();
                this.$emit("updateList");
            }
        },
        computed: {
            validTreasureHunt() {
                return ! ( this.editTreasureHuntName.length > 0 && this.editTreasureHuntDestination != null && this.editTreasureHuntRiddle.length > 0 && this.startDate != null && this.endDate != null)
            }
        },
        watch: {
            toggle(newVal, oldVal) {
                this.visible = newVal
            },
            visible(newVal, oldVal) {
                this.$emit("updateToggle", newVal)
            }
        }
    }
</script>

<style scoped>

</style>