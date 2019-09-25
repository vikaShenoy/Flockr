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
                    <v-text-field v-model="editTreasureHuntName" label="Name" required>
                    </v-text-field>
                  </v-flex>
                  <v-flex xs12>
                    <GenericCombobox
                      label="Destination"
                      item-text="destinationName"
                      :get-function="getPublicDestinationsFunction"
                      @item-selected="publicDestinationSelected"
                      v-model="editTreasureHuntDestination"
                    />
                  </v-flex>
                  <v-flex xs12>
                    <v-textarea v-model="editTreasureHuntRiddle" label="Riddle" required>
                    </v-textarea>
                  </v-flex>

                  <div class="start-date-time-field">
                    <div>
                      <v-flex xs12>
                        <v-text-field
                          v-model="startDate"
                          label="Start Date"
                          prepend-icon="event"
                          type="date"
                          :max="today"
                        ></v-text-field>
                      </v-flex>
                    </div>

                    <div>
                      <v-flex xs12>
                        <v-text-field
                          v-model="startTime"
                          label="Start Time"
                          prepend-icon="timer"
                          type="time"
                          :min="startTime"
                        ></v-text-field>
                      </v-flex>
                    </div>
                  </div>


                  <div class="end-date-time-field">
                    <div>
                      <v-flex xs12>
                        <v-text-field
                          v-model="endDate"
                          label="End Date"
                          prepend-icon="event"
                          type="date"
                          :min="startDate"
                        ></v-text-field>
                      </v-flex>
                    </div>

                    <div>
                      <v-flex xs12>
                        <v-text-field
                          v-model="endTime"
                          label="End Time"
                          prepend-icon="timer"
                          type="time"
                        ></v-text-field>
                      </v-flex>
                    </div>
                  </div>

                </v-layout>
              </v-container>
            </v-form>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" flat @click="closeDialog">Close</v-btn>
            <v-btn color="blue darken-1" :disabled="validTreasureHunt" flat v-on:click="editTreasureHunt()"
            >Update
            </v-btn>
          </v-card-actions>
        </v-card>

      </v-tab-item>
    </v-tabs>


  </v-dialog>
</template>

<script>
  import moment from "moment";
  import { editTreasureHunt } from "./TreasureHuntsService"
  import { getPublicDestinations } from "../Destinations/DestinationsService";
  import GenericCombobox from "../../components/GenericCombobox/GenericCombobox";
  import Command from "../../components/UndoRedo/Command";

  export default {
    name: "EditTreasureHunt",
    components: { GenericCombobox },
    props: {
      toggle: Boolean,
      data: Object
    },
    mounted() {
      this.getDestinations();
      this.editTreasureHuntName = this.data.treasureHuntName;
      this.editTreasureHuntRiddle = this.data.riddle;
      this.editTreasureHuntDestinationId = this.data.treasureHuntDestinationId;
      this.startDate = moment(this.data.startDate).format("YYYY-MM-DD");
      this.startTime = moment(this.data.startDate).format("HH:mm");
      this.endDate = moment(this.data.endDate).format("YYYY-MM-DD");
      this.endTime = moment(this.data.endDate).format("HH:mm");
      this.visible = this.toggle;
    },
    data() {
      return {
        visible: false,
        destinations: [],
        editTreasureHuntName: "",
        editTreasureHuntDestination: this.data.destination, // the id of the destination
        editTreasureHuntDestinationId: null,
        editTreasureHuntRiddle: "",
        startDateAndTime: null,
        endDateAndTime: null,
        startDate: null,
        startTime: null,
        endDate: null,
        endTime: null,
        today: new Date().toISOString().split("T")[0],
        getPublicDestinationsFunction: search => getPublicDestinations(search, 0) // used by GenericCombobox
      }
    },
    methods: {
      /**
       * Called when a new public destination is selected in the GenericCombobox
       */
      publicDestinationSelected(destination) {
        this.editTreasureHuntDestinationId = destination.destinationId;
      },
      /**
       * Function that emits an event to the parent component to close the modal, and also resets all form data
       */
      closeDialog() {
        this.$emit("closeEditDialog");
        this.editTreasureHuntName = "";
        this.editTreasureHuntRiddle = "";
        this.editTreasureHuntDestinationId = null;
        this.editTreasureHuntDestination = null;
        this.startDate = null;
        this.endDate = null;
      },

      /**
       * Calls the treasure hunt service to get all public destinations required for the drop down in the form
       */
      async getDestinations() {
        try {
          this.destinations = await getPublicDestinations();
        } catch (err) {
          this.$root.$emit("show-error-snackbar", "Could not get public destinations", 3000);
        }
      },

      /**
       * Calls the treasure hunt service to update the given treasure hunt.
       * Create the undo edit command and pass it to the stack.
       */
      async editTreasureHunt() {

        let oldTreasureHuntData = {
          treasureHuntId: this.data.treasureHuntId,
          treasureHuntName: this.data.treasureHuntName,
          treasureHuntDestinationId: this.data.treasureHuntDestinationId,
          riddle: this.data.riddle,
          startDate: moment(this.data.startDate).format("YYYY-MM-DD"),
          startTime: moment(this.data.startDate).format("HH:mm"),
          endDate: moment(this.data.endDate).format("YYYY-MM-DD"),
          endTime: moment(this.data.endDate).format("HH:mm")
        };

        let newTreasureHuntData = {
          treasureHuntId: this.data.treasureHuntId,
          treasureHuntName: this.editTreasureHuntName,
          treasureHuntDestinationId: this.editTreasureHuntDestinationId,
          riddle: this.editTreasureHuntRiddle,
          startDate: moment(this.startDate).format("YYYY-MM-DD"),
          startTime: this.startTime,
          endDate: moment(this.endDate).format("YYYY-MM-DD"),
          endTime: this.endTime
        };

        const undoCommand = async (oldTreasureHuntData) => {
          await editTreasureHunt(oldTreasureHuntData);
          this.$emit("updateList");
        };

        const redoCommand = async (newTreasureHuntData) => {
          await editTreasureHunt(newTreasureHuntData);
          this.$emit("updateList");
        };

        const editCommand = new Command(undoCommand.bind(null, oldTreasureHuntData),
            redoCommand.bind(null, newTreasureHuntData));
        this.$emit("addCommand", editCommand);

        await editTreasureHunt(newTreasureHuntData);
        this.closeDialog();
        this.$emit("updateList");
      }
    },
    computed: {
      /**
       * Returns true when all fields of the input form contain something
       * @returns {boolean}
       */
      validTreasureHunt() {
        return !((this.editTreasureHuntName != null && this.editTreasureHuntName.length > 0) && this.editTreasureHuntDestination != null && (this.editTreasureHuntRiddle != null && this.editTreasureHuntRiddle.length > 0) && this.startDate != null && this.endDate != null)
      }
    },
    watch: {
      toggle(newVal) {
        this.visible = newVal
      },
      visible(newVal) {
        this.$emit("updateToggle", newVal)
      },
      editTreasureHuntDestination(newDestination) {
        this.editTreasureHuntDestination = newDestination;
        if (newDestination !== null) {
          this.editTreasureHuntDestinationId = this.editTreasureHuntDestination.destinationId;
        }
      }
    }
  }
</script>

<style scoped>

  .start-date-time-field {
    float: left;
    width: 100%
  }

  .start-date-time-field div{
    float: left;
  }

  .end-date-time-field {
    float: left;
    width: 100%
  }

  .end-date-time-field div{
    float: left;
  }
</style>