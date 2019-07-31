<template>
  <v-container grid-list-xl id="grid-container">
    <v-layout wrap>
      <v-flex
              xs8
              offset-xs2
      >
        <div style="position: relative">
          <div id="undo-redo">
            <UndoRedo
                    ref=undoRedo
            />
          </div>

          <h2>Treasure Hunts</h2>
        </div>

        <div v-if="!treasureHunts">
          <v-progress-circular
                  indeterminate
                  color="secondary"
                  id="loading-spinner"
          ></v-progress-circular>
        </div>

        <div v-else-if="treasureHunts.length === 0" id="no-treasure-hunts">
          <v-icon>search</v-icon>
          <span>No treasure hunts found</span>
        </div>


        <v-expansion-panel class="panel" v-else>
          <v-expansion-panel-content
                  v-for="(item, i) in treasureHunts"
                  :key="i"
                  class="content"
          >
            <template v-slot:header>
              <div id="header">
                <h4>{{item.treasureHuntName}}</h4>
                <span class="end-date">Ends {{getTimeToGo(item.endDate)}} </span>
              </div>

              <v-spacer align="right">
                <v-btn
                        color="secondary"
                        fab
                        small
                        flat
                        v-if="isOwner(item.ownerId) || isAdmin()"
                        @click="event => showEditDialog(event, item)"
                >
                  <v-icon>edit</v-icon>
                </v-btn>
                <v-btn
                        color="error"
                        fab
                        small
                        flat
                        v-if="isOwner(item.ownerId) || isAdmin()"
                        @click="event => deleteTreasureHunt(event, item.treasureHuntId)"
                >
                  <v-icon>delete</v-icon>
                </v-btn>

              </v-spacer>
            </template>
            <v-card class="card">
              <v-card-text><b>Riddle</b>: {{item.riddle}}</v-card-text>
              <v-card-text><b>Start Date</b>: {{formatDate(item.startDate)}}
                <br>
                <b>End Date</b>: {{formatDate(item.endDate)}}
                <br>Date and times are in your local timezone.
              </v-card-text>
              <v-card-text v-if="isOwner(item.ownerId) || isAdmin()"><b>Destination</b>: {{item.destination}}
              </v-card-text>
            </v-card>
          </v-expansion-panel-content>
        </v-expansion-panel>


        <AddTreasureHunt
                :toggle="showDialog"
                @closeDialog="closeDialog"
                @updateToggle="updateToggle"
                @updateList="updateList"
                @createTreasureHuntCommand="createTreasureHuntCommand"
        ></AddTreasureHunt>

        <EditTreasureHunt
                :toggle="showEditForm"
                :data="treasureHunt"
                :key="refreshEditDialog"
                @closeEditDialog="closeEditDialog"
                @updateList="updateList"
                @addCommand="addCommand"
        ></EditTreasureHunt>
      </v-flex>
    </v-layout>

    <v-btn
            color="secondary"
            id="add-treasure-hunt-btn"
            fab
            v-on:click="showDialog = true"
    >
      <v-icon>add</v-icon>
    </v-btn>

  </v-container>
</template>

<script>
  import AddTreasureHunt from "./AddTreasureHunt";
  import EditTreasureHunt from "./EditTreasureHunt";
  import {
    deleteTreasureHuntData,
    getAllTreasureHunts,
    getDestination,
    undoDeleteTreasureHuntData
  } from "./TreasureHuntsService";
  import moment from "moment";
  import UserStore from "../../stores/UserStore";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command";

  export default {
    components: {UndoRedo, AddTreasureHunt, EditTreasureHunt},
    mounted() {
      this.getTreasureHunts();
    },
    data() {
      return {
        showDialog: false,
        showEditForm: false,
        treasureHunts: null,
        treasureHunt: {},
        refreshEditDialog: 50
      };
    },
    methods: {
      addCommand(editCommand) {
        this.$refs.undoRedo.addUndo(editCommand);
      },

      /**
       * Adds the treasure hunt undo-redo in the stack
       */
      createTreasureHuntCommand(createTreasureHuntCommand) {
        this.$refs.undoRedo.addUndo(createTreasureHuntCommand);
      },
      /**
       * Hides the create treasure hunt modal
       */
      closeDialog() {
        this.showDialog = false;
      },

      /**
       * Hides the edit treasure hunt modal
       */
      closeEditDialog() {
        this.showEditForm = false;
      },

      /**
       * Calls the treasure hunt service to set the populate the list of treasure hunts with all valid treasure hunts
       */
      async getTreasureHunts() {
        const rawTreasureHunts = await getAllTreasureHunts();

        const treasureHuntsPromises = rawTreasureHunts.map(async treasureHunt => {
          return {
            ...treasureHunt,
            destination: await getDestination(
                treasureHunt.treasureHuntDestinationId
            )
          };
        });

        this.treasureHunts = await Promise.all(treasureHuntsPromises);
      },

      /**
       * Takes in a date number retrieved from the database and converts it into a displayable string
       * @param date from treasure hunt object
       * @returns {string} formatted by moment
       */
      formatDate(date) {
        return moment(date).format("D/M/YYYY H:mm");
      },

      /**
       * Calculates the remaining time left to solve a treasure hunt, currently doesn't take into account the time zones
       * @param date - the ending time of the treasure hunt
       * @returns {string} A formatted string displaying the time remaining
       */
      getTimeToGo(date) {
        const currentTime = moment();
        const closeDate = moment(date);
        if (currentTime.isSame(closeDate, "year")) {
          return closeDate.format("DD MMM [at] HH:mm");
        }

        return closeDate.format("DD MMM YYYY");
      },

      /**
       * Function to check if the logged in user is the owner of the treasure hunt
       * @param ownerId
       * @returns {boolean} true if the owner of the treasureHunt is the logged in user
       */
      isOwner(ownerId) {
        return localStorage.getItem("userId") == ownerId;
      },

      /**
       * Function to that checks if the user is an admin or not
       */
      isAdmin() {
        return UserStore.methods.isAdmin();
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
        console.log(2);
        this.getTreasureHunts();
      },

      /**
       * Function to show the edit treasure hunt form
       * @param treasureHunt
       */
      showEditDialog(event, treasureHunt) {
        event.stopPropagation();
        this.treasureHunt = treasureHunt;
        this.showEditForm = true;
        this.refreshEditDialog += 1;
      },

      async deleteTreasureHunt(event, treasureHuntId) {
        event.stopPropagation();
        try {
          await deleteTreasureHuntData(treasureHuntId);
          this.updateList();

          const undoCommand = async () => {
            await undoDeleteTreasureHuntData(treasureHuntId);
            this.updateList();
          };

          const redoCommand = async () => {
            await deleteTreasureHuntData(treasureHuntId);
            this.updateList();
          };

          const deleteTreasureHuntCommand = new Command(
              undoCommand.bind(null, treasureHuntId),
              redoCommand.bind(null, treasureHuntId)
          );
          this.$refs.undoRedo.addUndo(deleteTreasureHuntCommand);
        } catch (e) {
          this.showError(e.message);
        }
      },
      /**
       * Shows an snackbar error message
       * @param {string} text the text to display on the snackbar
       */
      showError(text) {
        this.errorSnackbar.text = text;
        this.errorSnackbar.show = true;
        this.errorSnackbar.color = "error";
      }
    }
  };
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #header {

    h4 {
      color: $secondary;
      font-weight: bold;
      text-align: left;
    }
  }

  .testing {
    background-color: $darker-white !important;
  }

  h2 {
    margin-top: 10px;
  }

  #undo-redo {
    right: 0px;
    position: absolute;
    bottom: 0px;
    z-index: 3;
  }

  .panel {
    margin-top: 20px;
  }

  #add-treasure-hunt-btn {
    position: absolute;
    bottom: 20px;
    right: 20px;
  }

  #grid-container {
    margin-top: 0px;
    padding-top: 0px;
  }

  .end-date {
    color: rgba(0, 0, 0, 0.80)
  }

  #loading-spinner {
    margin: 0 auto;
    display: block;
    margin-top: 50px;
  }

  #no-treasure-hunts {
    span {
      color: rgba(0, 0, 0, 0.5);
      font-weight: 500;
    }

    display: flex;
    flex-direction: column;
    margin-top: 80px;
  }
</style>


