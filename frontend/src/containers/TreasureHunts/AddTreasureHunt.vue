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
                Create Treasure Hunt
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
                                        <v-text-field v-model="createTreasureHuntName" label="Name" required ></v-text-field>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-select v-model="createTreasureHuntDestination" required label="Destination" :items="destinations" item-text="destinationName" item-value="destinationId">

                                        </v-select>
                                    </v-flex>
                                    <v-flex xs12>
                                        <v-textarea v-model="createTreasureHuntRiddle" label="Riddle" required></v-textarea>
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
                        <v-btn color="blue darken-1" :disabled="validTreasureHunt" flat v-on:click="createTreasureHunt()" >Create</v-btn>
                    </v-card-actions>
                </v-card>

            </v-tab-item>
        </v-tabs>



    </v-dialog>
</template>

<script>
    import {getPublicDestinations, createTreasureHunt} from "./TreasureHuntsService"
    export default {
        name: "AddTreasureHunt",
        props: {
            toggle: Boolean
        },
        mounted() {
          this.getDestinations();
        },
        data() {
            return {
                visible: false,
                destinations: [],
                createTreasureHuntName: "",
                createTreasureHuntDestination: -1,
                createTreasureHuntRiddle: "",
                startDate: null,
                endDate: null,

            }
        },
        methods: {
            closeDialog() {
                this.$emit("closeDialog");
                this.createTreasureHuntName = "";
                this.createTreasureHuntRiddle = "";
                this.createTreasureHuntDestination = null;
                this.startDate = null;
                this.endDate = null;
            },
            async getDestinations() {
                this.destinations = await getPublicDestinations()
            },
            async createTreasureHunt() {
                console.log("Create the treasure hunt in this function");
                let treasureHunt = {

                    treasureHuntName: this.createTreasureHuntName,
                    treasureHuntDestinationId: this.createTreasureHuntDestination,
                    riddle: this.createTreasureHuntRiddle,
                    startDate: this.startDate,
                    endDate: this.endDate

                };
                await createTreasureHunt(treasureHunt);

                this.closeDialog();
                this.$emit("updateList");
            }
        },
        computed: {
            validTreasureHunt() {
                return ! ( this.createTreasureHuntName.length > 0 && this.createTreasureHuntDestination != null && this.createTreasureHuntRiddle.length > 0 && this.startDate != null && this.endDate != null)
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