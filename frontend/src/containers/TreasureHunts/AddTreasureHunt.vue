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
                    <v-text-field v-model="createTreasureHuntName" label="Name" required></v-text-field>
                  </v-flex>
                  <v-flex xs12>
                    <GenericCombobox
                      label="Destination"
                      :required="true"
                      item-text="destinationName"
                      :get-function="getPublicDestinationsFunction"
                      v-model="createTreasureHuntDestination"
                    />
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
                            :max="today"
                    ></v-text-field>
                  </v-flex>
                  <v-flex xs12>
                    <v-text-field
                            v-model="endDate"
                            label="End Date"
                            prepend-icon="event"
                            type="date"
                            :min="today"
                    ></v-text-field>
                  </v-flex>
                </v-layout>
              </v-container>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" flat @click="closeDialog">Close</v-btn>
            <v-btn color="blue darken-1" :disabled="validTreasureHunt" flat v-on:click="createTreasureHunt()">Create
            </v-btn>
          </v-card-actions>
        </v-card>

      </v-tab-item>
    </v-tabs>


  </v-dialog>
</template>

<script>
  import {
    createTreasureHunt,
    deleteTreasureHuntData,
    undoDeleteTreasureHuntData
  } from "./TreasureHuntsService"
  import { getPublicDestinations } from "../Destinations/DestinationsService";
  import Command from "../../components/UndoRedo/Command";
  import GenericCombobox from "../../components/GenericCombobox/GenericCombobox";

  export default {
    name: "AddTreasureHunt",
    components: { GenericCombobox },
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
        createTreasureHuntDestination: null,
        createTreasureHuntRiddle: "",
        startDate: null,
        endDate: null,
        today: new Date().toISOString().split("T")[0],
        treasure: null,
        getPublicDestinationsFunction: search => getPublicDestinations(search, 0) // used by GenericCombobox component
      }
    },
    methods: {
      /**
       * Function called by the close button in the dialog,
       *  emits an event to the parent to close the modal,
       *  and also resets the local data
       */
      closeDialog() {
        this.$emit("closeDialog");
        this.createTreasureHuntName = "";
        this.createTreasureHuntRiddle = "";
        this.createTreasureHuntDestination = null;
        this.startDate = null;
        this.endDate = null;
      },

      /**
       * Calls the treasure hunt service to update the list of public destinations displayed in the dropdown box
       */
      async getDestinations() {
        try {
          this.destinations = await getPublicDestinations();
        } catch (err) {
          this.$root.$emit("show-error-snackbar", "Could not get public destinations", 3000);
        }
      },

      /**
       * Calls the treasure hunt service to create a treasure hunt
       */
      async createTreasureHunt() {

        let treasureHunt = {

          treasureHuntName: this.createTreasureHuntName,
          treasureHuntDestinationId: this.createTreasureHuntDestination.destinationId,
          riddle: this.createTreasureHuntRiddle,
          startDate: this.startDate,
          endDate: this.endDate + " 23:59:59"

        };

        try {

          this.treasure = await createTreasureHunt(treasureHunt);

          const undoCommand = async () => {
            await deleteTreasureHuntData(this.treasure.treasureHuntId);
            this.$emit("updateList");
          };

          const redoCommand = async () => {
            await undoDeleteTreasureHuntData(this.treasure.treasureHuntId);
            this.$emit("updateList");
          };

          const createTreasureHuntCommand = new Command(undoCommand.bind(null, this.treasure.treasureHuntId), redoCommand.bind(null, this.treasure.treasureHuntId));
          this.$emit("createTreasureHuntCommand", createTreasureHuntCommand);
          this.closeDialog();
          this.$emit("updateList");
        } catch (error) {
          this.$emit("showError", error.message)
        }
      }
    },
    computed: {

      /**
       * Returns true when all fields of the input form contain something
       * @returns {boolean}
       */
      validTreasureHunt() {
          return !(this.createTreasureHuntName.length > 0 && this.createTreasureHuntDestination != null && this.createTreasureHuntRiddle.length > 0 && this.startDate != null && this.endDate != null)
      }
    },
    watch: {
      toggle(newVal) {
        this.visible = newVal
      },
      visible(newVal) {
        this.$emit("updateToggle", newVal)
      }
    }
  }
</script>

<style scoped>

</style>