<template>
  <v-card id="destination-proposals">
    <v-subheader>
      Destination Proposals
    </v-subheader>
    <v-divider></v-divider>

    <v-data-table
        :headers="headers"
        :items="destinationProposals || []"
        :loading="!destinationProposals"
        color="secondary"
        hide-actions
        :no-data-text="destinationProposals ? 'No Proposals' : ''"
    >
      <template v-slot:items="props">
        <td>{{ props.item.destination.destinationName }}</td>
        <td class="text-xs-left">
          <v-chip
              v-for="travellerType in props.item.travellerTypes"
              v-bind:key="travellerType.travellerTypeId"
              color="primary"
              text-color="white"
              close
              v-on:input="removeTravellerTypeFromProposal(travellerType, props.item)"
          >
            {{ travellerType.travellerTypeName }}
          </v-chip>
        </td>
        <td>
          <v-combobox
              v-if="getAvailableTravellerTypes(props.item.travellerTypes).length !== 0"
              :items="getAvailableTravellerTypes(props.item.travellerTypes)"
              :ref="props.item.destinationProposalId"
              item-text="travellerTypeName"
              label="Add Another Type"
              append-outer-icon="add"
              multiple
              @click:append-outer="addTravellerTypesFromProposal(props.item)"
          />
          <h4 v-else>
            All types selected
          </h4>
        </td>
        <td class="text-xs-center">
          <v-btn
              flat
              color="success"
              @click="acceptProposal(props.item.destinationProposalId)"
          >
            <v-icon>check_circle</v-icon>
          </v-btn>
          <v-btn
              flat
              color="error"
              @click="declineProposal(props.item.destinationProposalId)"
          >
            <v-icon>delete</v-icon>
          </v-btn>

        </td>
      </template>
    </v-data-table>

    <div style="text-align: center !important;">
      <v-btn :disabled="page <= 1" small fab @click="backPage">
        <v-icon>navigate_before</v-icon>
      </v-btn>
      <v-btn :disabled="destinationProposals !== null && destinationProposals.length < 5" small fab @click="nextPage">
        <v-icon>navigate_next</v-icon>
      </v-btn>
    </div>

  </v-card>
</template>

<script>
  import {
    acceptProposal,
    declineProposal,
    getDestinationProposal,
    getDestinationProposals,
    updateProposal
  } from "./DestinationProposalsService";
  import Command from "../../../components/UndoRedo/Command";
  import {sendUpdateDestination} from "../../Destinations/DestinationsService";
  import {undeleteProposal} from "../../Destination/DestinationService";
  import {getAllTravellerTypes} from "../../Profile/TravellerTypes/TravellerTypesService";

  export default {
    data() {
      return {
        headers: [
          {
            text: "Destination Name",
            align: "left",
            sortable: false,
            value: "destinationName"
          },
          {
            text: "Current Suggested Traveller Types",
            value: "travellerTypes",
            sortable: false,
            align: "left"
          },
          {
            text: "Add Traveller Type",
            value: "addTravellerType",
            sortable: false,
            align: "center",
            width: "400px"
          },
          {
            text: "Actions",
            value: "actions",
            sortable: false,
            align: "center",
          }
        ],
        destinationProposals: null,
        oldDestination: null,
        destinationId: null,
        allTravellerTypes: [],
        backDisabled: true,
        forwardDisabled: false,
        page: 1
      };
    },
    /**
     * Get all traveller types and proposals used for rendering in table.
     */
    async mounted() {
      try {
        this.destinationProposals = await getDestinationProposals();
      } catch (e) {
        this.$emit("showError", "Could not get proposals");
      }

      try {
        this.allTravellerTypes = await getAllTravellerTypes();
      } catch (e) {
        this.$emit("showError", "Could not get traveller types");
      }
    },
    methods: {
      /**
       * Increment the page by one and make a call to the backend to retrieve this new page
       */
      nextPage() {
        this.page += 1;
        this.getAllProposals(this.page);
      },
      /**
       * Decrement the page by one and make a call to the backend to retrieve this new page
       */
      backPage() {
        this.page -= 1;
        this.getAllProposals(this.page);
      },
      /**
       * Return a list of traveller types not in the given list.
       *
       * @param travellerTypes the unavailable traveller types.
       * @return Array<Object> the traveller types still available.
       */
      getAvailableTravellerTypes(travellerTypes) {
        return this.allTravellerTypes.filter(travellerType => {
          return !travellerTypes.some(
              type => type.travellerTypeId === travellerType.travellerTypeId)
        });
      },
      /**
       * Called when a close button is clicked on a traveller type chip.
       * Modifies a destination proposal to remove the said traveller type.
       * Sets undo and redo commands for this process.
       *
       * @param travellerType the traveller type object.
       * @param proposal the destination proposal to modify.
       */
      async removeTravellerTypeFromProposal(travellerType, proposal) {
        const proposalIndex = this.destinationProposals.indexOf(proposal);
        const typeIndex = proposal.travellerTypes.indexOf(travellerType);

        const originalProposal = {...proposal};
        originalProposal.travellerTypeIds =
            originalProposal.travellerTypes.map(travellerTypeId => travellerTypeId.travellerTypeId);

        const modifiedProposal = {
          destinationProposalId: proposal.destinationProposalId,
          travellerTypeIds: [...originalProposal.travellerTypeIds]
        };
        modifiedProposal.travellerTypeIds.splice(typeIndex, 1);

        try {
          this.destinationProposals[proposalIndex] = await updateProposal(modifiedProposal);
          this.getAllProposals(this.page);

          const undoCommand = async (proposal) => {
            this.destinationProposals[proposalIndex] = await updateProposal(proposal);
            this.getAllProposals(this.page);
          };

          const redoCommand = async (proposal) => {
            this.destinationProposals[proposalIndex] = await updateProposal(proposal);
            this.getAllProposals(this.page);
          };

          const modifyProposalCommand = new Command(undoCommand.bind(null, originalProposal),
              redoCommand.bind(null, modifiedProposal));
          this.$emit("addUndoCommand", modifyProposalCommand);
        } catch (error) {
          if (error.status === 400) {
            this.$emit("showError", "There was an error updating the proposal.")
          }
          if (error.status === 404) {
            this.$emit("showError", "This destination Proposal does not exist.");
            this.getAllProposals(this.page);
          }
        }
      },
      /**
       * Called when the add button is selected on the add traveller type combobox.
       * Modifies a destination proposal to add the said traveller type.
       * Sets undo and redo commands for this process.
       *
       * @param proposal the destination proposal to modify.
       */
      async addTravellerTypesFromProposal(proposal) {
        const travellerTypes = this.$refs[proposal.destinationProposalId].selectedItems;
        const travellerTypeIdsToAdd =
            travellerTypes.map(travellerType => travellerType.travellerTypeId);

        const proposalIndex = this.destinationProposals.indexOf(proposal);

        const originalProposal = {...proposal};
        originalProposal.travellerTypeIds =
            originalProposal.travellerTypes.map(travellerTypeId => travellerTypeId.travellerTypeId);

        const modifiedProposal = {
          destinationProposalId: proposal.destinationProposalId,
          travellerTypeIds: originalProposal.travellerTypeIds.concat(travellerTypeIdsToAdd)
        };

        try {
          this.destinationProposals[proposalIndex] = await updateProposal(modifiedProposal);
          this.getAllProposals(this.page);
          this.$refs[proposal.destinationProposalId].reset();

          const undoCommand = async (proposal) => {
            this.destinationProposals[proposalIndex] = await updateProposal(proposal);
            this.getAllProposals(this.page);
          };

          const redoCommand = async (proposal) => {
            this.destinationProposals[proposalIndex] = await updateProposal(proposal);
            this.getAllProposals(this.page);
          };

          const modifyProposalCommand = new Command(undoCommand.bind(null, originalProposal),
              redoCommand.bind(null, modifiedProposal));
          this.$emit("addUndoCommand", modifyProposalCommand);
        } catch (error) {
          if (error.status === 400) {
            this.$emit("showError", "There was an error updating the proposal.")
          }
          if (error.status === 404) {
            this.$emit("showError", "This destination Proposal does not exist.");
            this.getAllProposals(this.page);
          }
        }
      },
      /**
       * Called when admin clicked accept button for proposal.
       * Sends a request to change the destination proposal to the new proposal.
       * Sets undo/redo commands.
       * @param destinationProposalId id of the proposal to be accepted.
       */
      async acceptProposal(destinationProposalId) {
        try {
          const destinationProposal = await getDestinationProposal(destinationProposalId);
          await acceptProposal(destinationProposalId);
          this.oldDestination = destinationProposal.destination;
          this.destinationId = destinationProposal.destination.destinationId;

          const undoCommand = async () => {
            await undeleteProposal(destinationProposalId);
            await sendUpdateDestination(this.oldDestination, this.destinationId);
            this.getAllProposals(this.page);
          };

          const redoCommand = async () => {
            await acceptProposal(destinationProposalId);
            this.filterOutDestinationProposalId(destinationProposalId);
            this.getAllProposals(this.page);
          };

          const acceptProposalCommand = new Command(undoCommand.bind(null, destinationProposalId),
              redoCommand.bind(null, destinationProposalId));
          this.$emit("addUndoCommand", acceptProposalCommand);

          this.filterOutDestinationProposalId(destinationProposalId);
          this.$emit("showMessage", "Accepted Proposal");
          this.getAllProposals(this.page);
        } catch (e) {
          this.$emit("showError", "Could not accept proposal");
        }
      },
      /**
       * Decline proposal for traveller type change.
       * Send a request to delete the proposal.
       * Set undo/redo commands.
       * @param destinationProposalId id of the destination proposal to be discarded.
       */
      async declineProposal(destinationProposalId) {
        try {
          await declineProposal(destinationProposalId);

          const undoCommand = async () => {
            await undeleteProposal(destinationProposalId);
            this.getAllProposals(this.page);
          };

          const redoCommand = async () => {
            await declineProposal(destinationProposalId);
            this.filterOutDestinationProposalId(destinationProposalId);
            this.getAllProposals(this.page);
          };

          const declineProposalCommand = new Command(undoCommand.bind(null, destinationProposalId),
              redoCommand.bind(null, destinationProposalId));
          this.$emit("addUndoCommand", declineProposalCommand);
          this.filterOutDestinationProposalId(destinationProposalId);
          this.$emit("showMessage", "Rejected Proposal");
          this.getAllProposals(this.page);
        } catch (e) {
          this.$emit("showError", "Could not decline proposal");
        }
      },
      /**
       * Called after a proposal is accepted or rejected.
       * Remove a destination proposal from the list of viewable destination proposals.
       * @param destinationProposalId id of the proposal which is removed.
       */
      filterOutDestinationProposalId(destinationProposalId) {
        this.destinationProposals = this.destinationProposals.filter(destinationProposal => {
          return destinationProposal.destinationProposalId !== destinationProposalId;
        });
      },
      /**
       * Call endpoint to populate the array of destination proposals to be displayed.
       * @returns {Promise<void>} body of request containing destination proposals.
       */
      async getAllProposals(page) {
        this.destinationProposals = await getDestinationProposals(page);
        if (this.destinationProposals !== null && this.destinationProposals.length < 5) {
          this.forwardDisabled = true;
        }
      },
    }
  };
</script>

<style lang="scss" scoped>
  #destination-proposals {
    margin-top: 10px;
  }
</style>


