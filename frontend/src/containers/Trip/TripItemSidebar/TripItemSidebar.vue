<template>
  <v-card
    id="trip-item-sidebar"
    :elevation="20"
    v-bind:style="{width: computeWidth}"
  >

    <div
      id="title"
      v-if="trip"
    >
      <v-btn
        flat
        color="secondary"
        id="manage-trip-btn"
        @click="isShowingManageTripDialog = true"
      >Manage
      </v-btn>

      <h2>{{ trip.name }}</h2>
    </div>

    <div id="trip-destinations-list">
      <div
        v-if="!trip"
        id="spinner"
      >
        <v-progress-circular
          indeterminate
          color="secondary"
          style="align-self: center;"
        >
        </v-progress-circular>

      </div>

      <div v-else>
        <Timeline
          :trip="trip"
					@toggleExpanded="tripNodeId => $emit('toggleExpanded', tripNodeId)"
          @tripNodeOrderChanged="tripNodeOrderChanged"
          @showEditTripDestination="showEditTripDestination"
          @deleteTripNode="tripNode => $emit('deleteTripNode', tripNode)"
        />
				<v-spacer align="center">
					<v-btn
									depressed
									color="secondary"
									id="add-trip-destination-btn"
									@click="isShowingAddDestinationDialog = true"
					>
						Add Destination
					</v-btn>

					<v-btn
									depressed
									color="secondary"
									id="add-subtrip-btn"
									@click="isShowingAddSubtripDialog = true"
					>
						Add Subtrip
					</v-btn>


				</v-spacer>

				<ModifySubtripDialog
					:editMode="false"
					:isShowing.sync="isShowingAddSubtripDialog"
					@newTripAdded="(subTrip, oldParentTrip, newParentTrip) =>
					$emit('newTripAdded', subTrip, oldParentTrip, newParentTrip)"
					@tripNodeOrderChanged="indexes => $emit('tripNodeOrderChanged', indexes)"
					:parentTrip="trip"
				/>

        <ModifyTripDestinationDialog
          :isShowing.sync="isShowingAddDestinationDialog"
          :editMode="false"
          :trip="trip"
          @updatedTripNodes="tripNodesUpdated"
        />

        <ModifyTripDestinationDialog
          :isShowing.sync="isShowingUpdateDestinationDialog"
          :editMode="true"
          :trip="trip"
          :editedTripDestination="editedTripDestination"
          @updatedTripNodes="tripNodesUpdated"
        />

        <ManageTripDialog
          :isShowing.sync="isShowingManageTripDialog"
          :trip="trip"
          v-if="trip"
          @newUsers="newUsers"
        />
      </div>
    </div>

  </v-card>
</template>

<script>
import Timeline from "./Timeline/Timeline.vue";
import ModifyTripDestinationDialog from "./ModifyTripDestinationDialog/ModifyTripDestinationDialog";
import ManageTripDialog from "./ManageTripDialog/ManageTripDialog";
import ModifySubtripDialog from "./ModifySubtripDialog/ModifySubtripDialog";

export default {
  components: {
    ModifySubtripDialog,
    Timeline,
    ModifyTripDestinationDialog,
    ManageTripDialog
  },
  data() {
    return {
      isShowingAddDestinationDialog: false,
      isShowingUpdateDestinationDialog: false,
			isShowingAddSubtripDialog: false,
      editedTripDestination: null,
      isShowingManageTripDialog: false
    };
  },
  props: {
    trip: {
      required: true
    }
  },
  methods: {
    /**
     * Emitted when the order of destinations have changed
     */
    tripNodeOrderChanged(indexes) {
      this.$emit("tripNodeOrderChanged", indexes);
    },
    /**
     * Called when the edit button has been pressed on
     * a trip destination
     */
    showEditTripDestination(tripDestination) {
      this.isShowingUpdateDestinationDialog = true;
      this.editedTripDestination = tripDestination;
    },
    tripNodesUpdated(tripNodes) {
      this.$emit("tripNodesUpdated", tripNodes);
    },
    newUsers(newUsers) {
      this.$emit("newUsers", newUsers);
    },
    /**
     * Finds the deepest level of a trip tree
     */
    findDeepestNodeLevel(tripNode) {
      if (!tripNode) {
        return 0;
      }
      if (tripNode.tripNodes.length === 0) {
        return 0;
      }
      const treeDepths = tripNode.tripNodes.map(currTripNode => {
        if (currTripNode.isShowing) {
          return this.findDeepestNodeLevel(currTripNode)
        } else {
          return 0;
        }
      });
      return Math.max(...treeDepths) + 1;
    }
  },
  computed: {
    computeWidth() {
      const width = 300 + (this.findDeepestNodeLevel(this.trip) * 50);
      return `${width}px`;
    }
  }
};
</script>


<style lang="scss" scoped>
@import "../../../styles/_variables.scss";

#trip-item-sidebar {
  height: calc(100vh - 64px);
  transition: width 0.2s linear;
  

  #title {
    height: 50px;
    background-color: $primary;
    color: #fff;
    z-index: 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  h2 {
    z-index: 3;
  }

  #trip-destinations-list {
    padding-bottom: 10px;
    overflow-y: auto;
    height: calc(100vh - 114px);
  }

  .option {
    background-color: $secondary;
    color: $darker-white;
  }

  .not-selected {
    background: none;
    background-color: none !important;
  }

  .theme--light.v-btn-toggle {
    background: none !important;
  }

  #spinner {
    justify-content: center;
    align-content: center;
    display: flex;
    height: 100%;
    flex-direction: column;
  }

  #add-destination-btn {
    position: absolute;
    margin-top: 13px;
    left: 0;
  }

  #manage-trip-btn {
    position: absolute;
    left: 5px;
    margin-top: 0px;
  }
}
</style>

