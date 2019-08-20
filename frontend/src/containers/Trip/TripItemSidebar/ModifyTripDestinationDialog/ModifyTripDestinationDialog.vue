<template>
  <v-dialog
          v-model="isShowingDialog"
          width="600px"
  >
    <v-card id="modify-trip-destination-dialog">
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">{{ editMode ? "Edit Trip Destination" : "Add Trip Destination" }}</h2>
          </v-spacer>
        </v-layout>

      </v-card-title>
      <v-form ref="form">
        <v-container grid-list-md>
          <v-layout
                  row
                  wrap
          >
            <v-flex xs12>
              <v-select
                      v-model="tripDestination.destination"
                      :items="filteredDestinations"
                      item-text="destinationName"
                      :item-value="destination => destination"
                      label="Destination"
                      :rules="destinationRules"
                      color="secondary"
              />
            </v-flex>

            <v-flex xs8>
              <v-menu
                      ref="arrivalDateMenu"
                      v-model="arrivalDateMenu"
                      :close-on-content-click="false"
                      :nudge-right="40"
                      :return-value.sync="tripDestination.arrivalDate"
                      lazy
                      transition="scale-transition"
              >
                <template v-slot:activator="{ on }">
                  <v-text-field
                          class="edit-field"
                          label="Arrival Date"
                          prepend-icon="date_range"
                          v-model="tripDestination.arrivalDate"
                          readonly
                          v-on="on"
                          color="secondary"
                          clearable
                  ></v-text-field>
                </template>
                <v-date-picker
                        color="secondary"
                        ref="picker"
                        v-model="tripDestination.arrivalDate"
                        no-title
                        scrollable
                >
                  <v-spacer></v-spacer>
                  <v-btn
                          flat
                          color="primary"
                          @click="arrivalDateMenu = false"
                  >Cancel
                  </v-btn>
                  <v-btn
                          flat
                          color="primary"
                          @click="$refs.arrivalDateMenu.save(tripDestination.arrivalDate)"
                  >OK
                  </v-btn>
                </v-date-picker>
              </v-menu>
            </v-flex>

            <v-menu
                    ref="arrivalTimeMenu"
                    v-model="arrivalTimeMenu"
                    :close-on-content-click="false"
                    :nudge-right="40"
                    :return-value.sync="tripDestination.arrivalTime"
                    lazy
                    transition="scale-transition"
                    offset-y
                    full-width
                    max-width="290px"
                    min-width="290px"
            >
              <template v-slot:activator="{ on }">
                <v-flex xs4>
                  <v-text-field
                          v-model="tripDestination.arrivalTime"
                          label="Arrival Time"
                          prepend-icon="access_time"
                          readonly
                          v-on="on"
                          color="secondary"
                          xs4
                          clearable
                  ></v-text-field>
                </v-flex>
              </template>
              <v-time-picker
                      v-if="arrivalTimeMenu"
                      v-model="tripDestination.arrivalTime"
                      full-width
                      @click:minute="$refs.arrivalTimeMenu.save(tripDestination.arrivalTime)"
              ></v-time-picker>
            </v-menu>

            <v-menu
                    ref="departureDateMenu"
                    v-model="departureDateMenu"
                    :close-on-content-click="false"
                    :nudge-right="40"
                    :return-value.sync="tripDestination.departureDate"
                    lazy
                    transition="scale-transition"
                    offset-y
                    full-width
                    class="date-picker"
            >
              <template v-slot:activator="{ on }">
                <v-flex xs8>
                  <v-text-field
                          class="edit-field"
                          label="Departure Date"
                          prepend-icon="date_range"
                          v-model="tripDestination.departureDate"
                          readonly
                          v-on="on"
                          color="secondary"
                          xs8
                          clearable
                  ></v-text-field>
                </v-flex>
              </template>
              <v-date-picker
                      color="secondary"
                      ref="picker"
                      v-model="tripDestination.departureDate"
                      no-title
                      scrollable
              >
                <v-spacer></v-spacer>
                <v-btn
                        flat
                        color="primary"
                        @click="departureDateMenu = false"
                >Cancel
                </v-btn>
                <v-btn
                        flat
                        color="primary"
                        @click="$refs.departureDateMenu.save(tripDestination.departureDate)"
                >OK
                </v-btn>
              </v-date-picker>
            </v-menu>

            <v-menu
                    ref="departureTimeMenu"
                    v-model="departureTimeMenu"
                    :close-on-content-click="false"
                    :nudge-right="40"
                    :return-value.sync="tripDestination.departureTime"
                    lazy
                    transition="scale-transition"
                    offset-y
                    full-width
                    max-width="290px"
                    min-width="290px"
            >
              <template v-slot:activator="{ on }">
                <v-flex xs4>
                  <v-text-field
                          v-model="tripDestination.departureTime"
                          label="Departure Time"
                          prepend-icon="access_time"
                          readonly
                          v-on="on"
                          color="secondary"
                          clearable
                  ></v-text-field>
                </v-flex>
              </template>
              <v-time-picker
                      v-if="departureTimeMenu"
                      v-model="tripDestination.departureTime"
                      full-width
                      @click:minute="$refs.departureTimeMenu.save(tripDestination.departureTime)"
              ></v-time-picker>
            </v-menu>
          </v-layout>
        </v-container>
      </v-form>

      <v-card-actions>
        <v-spacer align="right">
          <v-btn
                  depressed
                  color="secondary"
                  @click="modifyTripDestination()"
                  :loading="isLoading"
          >
            {{ editMode ? "Update" : "Create" }}
          </v-btn>
        </v-spacer>
      </v-card-actions>

    </v-card>
  </v-dialog>
</template>

<script>
  import { editTrip, getTripNodeParentById } from "../../TripService";
  import {rules} from "../../../../utils/rules";
  import {getDestinations} from "./ModifyTripDestinationDialogService";
  import {getYourDestinations} from '../../../Destinations/DestinationsService';
import { transformTripNode } from '../../TripService';

  export default {
    props: {
      isShowing: {
        type: Boolean,
        required: false
      },
      editMode: {
        type: Boolean,
        required: true
      },
      trip: {
        type: Object,
        required: true
      },
      editedTripDestination: {
        type: Object,
        required: false
      }
    },
    data() {
      return {
        isShowingDialog: false,
        tripDestination: {
          tripNodeId: null,
          arrivalDate: "",
          arrivalTime: "",
          departureDate: "",
          departureTime: "",
          destination: null,
          nodeType: "TripDestinationLeaf"
        },
        destinations: [],
        arrivalDateMenu: false,
        arrivalTimeMenu: false,
        departureDateMenu: false,
        departureTimeMenu: false,
        dateRules: [rules.required],
        destinationRules: [rules.required],
        isLoading: false
      };
    },
    mounted() {
      this.getDestinations();
    },
    methods: {
      async modifyTripDestination() {
        if (!this.$refs.form.validate()) return;
        let newTripNodes;

        this.tripDestination.name = this.tripDestination.destination.destinationName;

        const parentTripNode = getTripNodeParentById(this.tripDestination.tripNodeId, this.trip);
        
        console.log("THe parent trip is: ");
        console.log(parentTripNode);

        if (this.editMode) {
          // Replace trip destination with the new content
          newTripNodes = [...parentTripNode.tripNodes].map(tripNode => {
            if (tripNode.tripNodeId === this.tripDestination.tripNodeId) {
              return this.tripDestination;
            }

            return tripNode;
          });
        } else {
          newTripNodes = [...parentTripNode.tripNodes, this.tripDestination];
        }
        const unformattedTrip = transformTripNode({...parentTripNode, tripNodes: newTripNodes});
        
        const tripId = this.$route.params.tripId;
        this.isLoading = true;
        await editTrip(unformattedTrip);
        this.isLoading = false;
        this.isShowingDialog = false;
        this.$emit("updatedTripNodes", newTripNodes);

        this.tripDestination = {
          tripDestinationId: null,
          arrivalDate: "",
          arrivalTime: "",
          departureDate: "",
          departureTime: "",
          destination: null,
          nodeType: "TripDestinationLeaf"
        };
      },
      async getDestinations() {
        const [publicDestinations, yourDestinations] = await Promise.all([getDestinations(), getYourDestinations()]);

        // Need to filter out duplicate destinations
        const destinationsFound = new Set();
        let allDestinations = [...publicDestinations, ...yourDestinations].filter(destination => {
          return !destinationsFound.has(destination.destinationId) && destinationsFound.add(destination.destinationId);
        });

        this.destinations = allDestinations;
      }
    },
    computed: {
      /**
       * Filter destinations for the user to select.
			 * If in edit mode, filter out the destination on either side of the node being edited.
			 * If adding a new destination, filter out the destination at the bottom of the sidebar.
       */
      filteredDestinations() {
        if (this.editMode) {
          if (this.tripDestination.destination === null) return this.destinations;
          const tripDestinationParentNode = getTripNodeParentById(this.tripDestination.tripNodeId, this.trip, null);
          const tripDestinationIndex = tripDestinationParentNode.tripNodes.findIndex(
              node => node.tripNodeId === this.tripDestination.tripNodeId);

          let filterDestinationIds = [];
          let nodes = tripDestinationParentNode.tripNodes;

          if (tripDestinationIndex > 0 &&
              nodes[tripDestinationIndex - 1].nodeType === "TripDestinationLeaf") {
            filterDestinationIds.push(nodes[tripDestinationIndex - 1].destination.destinationId);
          }

          if (tripDestinationIndex + 1 < nodes.length &&
              nodes[tripDestinationIndex + 1].nodeType === "TripDestinationLeaf") {
            filterDestinationIds.push(nodes[tripDestinationIndex + 1].destination.destinationId);
          }
          return this.destinations.filter(destination => !filterDestinationIds.includes(destination.destinationId));
        } else {
          let allDestinations = this.destinations;
          const endNode = this.trip.tripNodes[this.trip.tripNodes.length - 1];
          if (endNode.nodeType === "TripDestinationLeaf") {
            const endNodeDestinationId = endNode.destination.destinationId;
            allDestinations = this.destinations.filter(
                destination => destination.destinationId !== endNodeDestinationId);
          }
          return allDestinations;
				}
			}
    },
    watch: {
      // Synchronize both isShowing state and props
      isShowingDialog(value) {
        this.$emit("update:isShowing", value);
      },
      isShowing(value) {
        this.isShowingDialog = value;
        if (this.isShowing) {
          this.tripDestination = this.editedTripDestination;
        }
      },
      editedTripDestination(tripDestination) {
        this.tripDestination = {...tripDestination};
      }
    }
  };
</script>

<style lang="scss" scoped>
  @import "../../../../styles/variables";

  .light-text {
    color: #fff;
  }

  // Override to fix weird bug where v-menu spans larger then date picker
  .v-menu__content {
    min-width: 280px !important;
  }

  .modify-trip-destination-dialog {
    padding: 10px;
  }
</style>