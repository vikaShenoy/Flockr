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
          >
            {{ travellerType.travellerTypeName }}
          </v-chip>

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

  </v-card>
</template>

<script>
import { getDestinationProposals, acceptProposal, declineProposal } from "./DestinationProposalsService";

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
          text: "Traveller Types",
          value: "travellerTypes",
          sortable: false,
          align: "left"
        },
        {
          text: "Actions",
          value: "actions",
          sortable: false,
          align: "center"
        }
      ],
      destinationProposals: null,
    };
  },
  /**
   * Gets all proposals used for rendering in table
   */
  async mounted() {
    try {
      const destinationProposals = await getDestinationProposals();
      this.destinationProposals = destinationProposals;
    } catch (e) {
      this.$emit("showError", "Could not get proposals");
    }
  },
  methods: {
    /**
     * Accept proposal for traveller type change
     */
    async acceptProposal(destinationProposalId) {
      try {
        await acceptProposal(destinationProposalId); 
        this.filterOutDestinationProposalId(destinationProposalId); 
        this.$emit("showMessage", "Accepted Proposal");
      } catch (e) {
        this.$emit("showError", "Could not accept proposal");
      }
    },
    /**
     * Declines proposal for treaveller type change
     */
    async declineProposal(destinationProposalId) {
      try {
        await declineProposal(destinationProposalId);
        this.filterOutDestinationProposalId(destinationProposalId);
        this.$emit("showMessage", "Rejected Proposal");
      } catch (e) {
        this.$emit("showError", "Could not decline proposal");
      }
    },
    /**
     * Filters out the destination proposal that was just accepted
     * or rejected
     */
    filterOutDestinationProposalId(destinationProposalId) {
      const newDestinationProposals = this.destinationProposals.filter(destinationProposal => {
        return destinationProposal.destinationProposalId !== destinationProposalId;
      }); 

      this.destinationProposals = newDestinationProposals;
    }
    
  }
};
</script>

<style lang="scss" scoped>
#destination-proposals {
  margin-top: 10px;
}
</style>


