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
                :items="destinations"
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
                  >Cancel</v-btn>
                  <v-btn
                    flat
                    color="primary"
                    @click="$refs.arrivalDateMenu.save(tripDestination.arrivalDate)"
                  >OK</v-btn>
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
                >Cancel</v-btn>
                <v-btn
                  flat
                  color="primary"
                  @click="$refs.departureDateMenu.save(tripDestination.departureDate)"
                >OK</v-btn>
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
          >
            {{ editMode ? "Update" : "Create" }}
          </v-btn>
        </v-spacer>
      </v-card-actions>

    </v-card>
  </v-dialog>
</template>

<script>
import { rules } from "../../../../utils/rules";
import { getDestinations, editTrip } from "./ModifyTripDestinationDialogService";
import { transformFormattedTrip } from '../../TripService';

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
    }
  },
  data() {
    return {
      isShowingDialog: false,
      tripDestination: {
        arrivalDate: "",
        arrivalTime: "",
        departureDate: "",
        departureTime: "",
        destination: null
      },
      destinations: [],
      arrivalDateMenu: false,
      arrivalTimeMenu: false,
      departureDateMenu: false,
      departureTimeMenu: false,
      dateRules: [rules.required],
      destinationRules: [rules.required]
    };
  },
  mounted() {
    this.getDestinations();
  },
  methods: {
    async modifyTripDestination() {
      if (!this.$refs.form.validate()) return;

      if (this.editMode) {
      } else {
        const newTripDestinations = [...this.trip.tripDestinations, this.tripDestination];
        console.log(newTripDestinations);
        const unformattedTrip = transformFormattedTrip({...this.trip, tripDestinations: newTripDestinations});
        const tripId = this.$route.params.tripId;
        await editTrip(tripId, unformattedTrip);
      }
    },
    async getDestinations() {
      const destinations = await getDestinations();
      this.destinations = destinations;
    }
  },
  watch: {
    // Synchronize both isShowing state and props
    isShowingDialog(value) {
      this.$emit("update:isShowing", value);
    },
    isShowing(value) {
      this.isShowingDialog = value;
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