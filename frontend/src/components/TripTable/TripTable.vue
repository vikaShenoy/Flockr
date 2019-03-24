<template>
  <v-data-table
    :headers="headers"
    :items="tripDestinations"
    hide-actions
  >
    <template v-slot:items="props">
      <td>
        <v-select
          v-model="props.item.destinationId"
          :items="destinations"
          label="Destination"
          item-text="destinationName"
          item-value="destinationId"
          color="secondary"
          :rules="fieldRules"
          :error-messages="props.item.destinationErrors"
        ></v-select>
      </td>
      <td>
        <v-menu
          ref="arrivalDateMenu"
          v-model="arrivalDateMenu"
          :close-on-content-click="false"
          :nudge-right="40"
          :return-value.sync="props.item.arrivalDate"
          lazy
          transition="scale-transition"
          offset-y
          full-width
        >
          <template v-slot:activator="{ on }">
            <v-text-field
              class="edit-field"
              v-model="props.item.arrivalDate"
              readonly
              v-on="on"
            ></v-text-field>
          </template>
          <v-date-picker
            color="primary"
            ref="picker"
            :min="currentDate"
            v-model="props.item.arrivalDate"
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
              @click="$refs.arrivalDateMenu.save(props.item.arrivalDate)"
            >OK</v-btn>
          </v-date-picker>
        </v-menu>
      </td>
      <td>
        <v-menu
          ref="arrivalTimeMenu"
          v-model="arrivalTimeMenu"
          :close-on-content-click="false"
          :nudge-right="40"
          :return-value.sync="props.item.arrivalTime"
          lazy
          transition="scale-transition"
          offset-y
          full-width
          max-width="290px"
          min-width="290px"
        >
          <template v-slot:activator="{ on }">
            <v-text-field
              v-model="props.item.arrivalTime"
              readonly
              v-on="on"
            ></v-text-field>
          </template>
          <v-time-picker
            color="primary"
            v-if="arrivalTimeMenu"
            v-model="props.item.arrivalTime"
            full-width
            @click:minute="$refs.arrivalTimeMenu.save(props.item.arrivalTime)"
          ></v-time-picker>
        </v-menu>
      </td>
      <td>
        <v-menu
          ref="departureDateMenu"
          v-model="departureDateMenu"
          :close-on-content-click="false"
          :nudge-right="40"
          :return-value.sync="props.item.departureDate"
          lazy
          transition="scale-transition"
          offset-y
          full-width
        >
          <template v-slot:activator="{ on }">
            <v-text-field
              class="edit-field"
              v-model="props.item.departureDate"
              readonly
              v-on="on"
            ></v-text-field>
          </template>
          <v-date-picker
            color="primary"
            ref="picker"
            :min="currentDate"
            v-model="props.item.departureDate"
            scrollable
          >
            <v-spacer></v-spacer>
            <v-btn
              flat
              color="primary"
              @click="props.item.departureDateMenu = false"
            >Cancel</v-btn>
            <v-btn
              flat
              color="primary"
              @click="$refs.departureDateMenu.save(props.item.departureDate)"
            >OK</v-btn>
          </v-date-picker>
        </v-menu>
      </td>
      <td>
        <v-menu
          ref="departureTimeMenu"
          v-model="departureTimeMenu"
          :close-on-content-click="false"
          :nudge-right="40"
          :return-value.sync="props.item.departureTime"
          lazy
          transition="scale-transition"
          offset-y
          full-width
          max-width="290px"
          min-width="290px"
        >
          <template v-slot:activator="{ on }">
            <v-text-field
              v-model="props.item.departureTime"
              readonly
              v-on="on"
            ></v-text-field>
          </template>
          <v-time-picker
            color="primary"
            v-if="departureTimeMenu"
            v-model="props.item.departureTime"
            full-width
            @click:minute="$refs.departureTimeMenu.save(props.item.departureTime)"
          ></v-time-picker>
        </v-menu>
      </td>
    </template>
  </v-data-table>
</template>

<script>
import { getDestinations } from "./TripTableService";
import moment from "moment";

export default {
  props: {
    tripDestinations: {
      type: Array
    }
  },
  mounted() {
    this.getDestinations();
  },
  data() {
    return {
      headers: [
        {
          text: "Destination",
          value: "destination",
          sortable: false
        },
        {
          text: "Arrival Date",
          value: "arrivalDate",
          sortable: false
        },
        {
          text: "Arrival Time",
          value: "arrivalTime",
          sortable: false
        },
        {
          text: "Departure Date",
          value: "departureDate",
          sortable: false
        },
        {
          text: "Departure Time",
          value: "departureTime",
          sortable: false
        }
      ],
      destinations: [],
      fieldRules: [field => !!field || "Field is required"],
      arrivalDateMenu: false,
      arrivalTimeMenu: false,
      departureDateMenu: false,
      departureTimeMenu: false,
      currentDate: moment().format("YYY-MM-DD")
    };
  },
  methods: {
    async getDestinations() {
      try {
        const destinations = await getDestinations();
        this.destinations = destinations;
      } catch (e) {
        // add error handling later
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>


