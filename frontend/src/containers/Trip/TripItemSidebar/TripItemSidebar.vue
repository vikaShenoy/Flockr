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
          v-if="isUserOwner"
          @click="isShowingManageTripDialog = true"
      >Manage
      </v-btn>

      <v-spacer width="100"
                align="center"
                v-if="editNameMode">
        <v-text-field
            ref="nameField"
            autofocus
            v-model="editedTripName"
            @blur="nameEdited"
            @keyup.enter="nameEdited"
            color="white"
            class="trip-name-field"
            :rules="rule"
        ></v-text-field>
      </v-spacer>
      <h2 id="trip-name" v-else @click="hasPermissionToEdit && enableEditName()">{{ trip.name }}</h2>
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
            :rootTrip="trip"
            @toggleExpanded="tripNodeId => $emit('toggleExpanded', tripNodeId)"
            @tripNodeOrderChanged="tripNodeOrderChanged"
            @showEditTripDestination="showEditTripDestination"
            @deleteTripNode="tripNode => $emit('deleteTripNode', tripNode)"
            @tripNameUpdated="(tripNode, newName) => $emit('nestedTripNameUpdated', tripNode, newName)"
        />
        <v-spacer align="center" v-if="hasPermissionToEdit">
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


      <div id="line">
        <hr>
        <div id="pan-toggle">
          <v-switch
            v-model="panOn"
            label="Pan On"
          ></v-switch>
        </div>
      </div>

    </div>

  </v-card>
</template>

<script>
  import Timeline from "./Timeline/Timeline.vue";
  import UserStore from "../../../stores/UserStore";
  import ModifyTripDestinationDialog
    from "./ModifyTripDestinationDialog/ModifyTripDestinationDialog";
  import ManageTripDialog from "./ManageTripDialog/ManageTripDialog";
  import ModifySubtripDialog from "./ModifySubtripDialog/ModifySubtripDialog";
  import {rules} from "../../../utils/rules";
  import {editTrip} from "../TripService";
import roleType from '../../../stores/roleType';

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
      isShowingManageTripDialog: false,
      editNameMode: false,
      editedTripName: null,
      rule: [rules.required],
      panOn: false
    };
  },
  props: {
    trip: {
      required: true
    }
  },
  methods: {
        /**
         * Called when the name of a trip is updated.
         */
        async nameEdited() {
          if (this.editedTripName &&
              this.editedTripName.length > 0) {
            if (this.editedTripName !== this.trip.name) {
              try {
                const originalTripInfo = {
                  ...this.trip,
                };

                const editedTripInfo = {
                  ...this.trip,
                  name: this.editedTripName
                };

                await editTrip(editedTripInfo);
                this.$emit(
                    "addEditTripCommand",
                    originalTripInfo,
                    editedTripInfo,
                    this.editedTripName);
                this.editedTripName = null;
                this.editNameMode = false;
                this.showSnackbar(
                    "Trip name successfully changed",
                    "success",
                    500);
              } catch (error) {
                this.showSnackbar(
                    "Please try a suitable name.",
                    "error",
                    500);
              }
            } else {
              this.editedTripName = null;
              this.editNameMode = false;
            }
          }
        },
        /**
         * @param {String} message the message to show in the snackbar
         * @param {String} color the colour for the snackbar
         * @param {Number} timeout the amount of time (in ms) for which we show the snackbar
         */
        showSnackbar(message, color, timeout) {
          this.$root.$emit("show-snackbar", {
            message: message,
            color: color,
            timeout: timeout
          });
        },
        /**
         * Called when the name of the trip is clicked on.
         * Enables a text field to edit the name of the trip.
         */
        enableEditName() {
          this.editNameMode = true;
          this.editedTripName = this.trip.name;
        },
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
    tripNodesUpdated(parentTripNode, tripNodes) {
      this.$emit("tripNodesUpdated", parentTripNode, tripNodes);
    },
    newUsers(newUsers, oldUsers, newRoles, oldRoles) {
      this.$emit("newUsers", newUsers, oldUsers, newRoles, oldRoles);
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
    },
    isUserOwner() {
      const user = this.trip.users.find(user => user.userId === UserStore.data.userId);
      const userRole = this.trip.userRoles.find(userRole => userRole.user.userId === user.userId && userRole.role.roleType === roleType.TRIP_OWNER);
      return userRole ? true : false; 
    },
    hasPermissionToEdit() {
      const userRole = this.trip.userRoles.find(userRole => userRole.user.userId === UserStore.data.userId);
      return userRole.role.roleType === roleType.TRIP_MANAGER || userRole.role.roleType === roleType.TRIP_OWNER;
    }
  },
  watch: {
    /**
     * Sets the pan on to the current value.
     * * True if the pan is on, false if the pan is off.
     */
    panOn(newPanOn) {
      this.panOn = newPanOn;
      this.$emit("setPan", this.panOn);
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
      height: 70px;
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
      height: calc(100vh - 205px);
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

    #trip-name {
      margin-top: 30px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
      margin-left: 10px;
      margin-right: 10px;
    }

  #pan-toggle {
    background: white;
    margin-left: 20px;
    color: $darker-white;
  }

  #line {
    position: absolute;
    bottom: 0%;
    width: 100%;
  }

    #manage-trip-btn {
      position: absolute;
      left: 5px;
      margin-top: -10px;
    }

    .trip-name-field {
      -webkit-text-fill-color: white;
      width: 100px !important;
      margin-top: 0px;
      margin-bottom: 0px;
      padding-top: 0px;
      padding-bottom: 0px;
    }
  }
</style>

