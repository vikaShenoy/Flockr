<template>
    <v-dialog
            v-model="toggle"
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
                                        <v-select v-model="createTreasureHuntDestination" required label="Destination" :items="destinatons" item-text="categoryName" item-value="categoryId">

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
                        <v-btn color="blue darken-1" :disabled="!validVenue" flat v-on:click="createVenue()" >Create</v-btn>
                    </v-card-actions>
                </v-card>

            </v-tab-item>
        </v-tabs>



    </v-dialog>
</template>

<script>
    export default {
        name: "AddTreasureHunt",
        props: {
            toggle: Boolean
        },
        data() {
            return {
                visible: true
            }
        },
        methods: {
            closeDialog() {
                this.$emit("closeDialog");
            }
        }
    }
</script>

<style scoped>

</style>