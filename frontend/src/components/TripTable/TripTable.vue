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
        <v-text-field v-model="props.item.arrivalDate" type="date"></v-text-field>
      </td>
      <td>
        <v-text-field v-model="props.item.arrivalTime" type="time"></v-text-field>
      </td>
      <td>
        <v-text-field v-model="props.item.departureDate" type="date"></v-text-field>
      </td>
      <td>
        <v-text-field v-model="props.item.departureTime" type="time"></v-text-field>
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
    },
    save(index) {
      console.log(this.$refs[`arrivalDateMenu${index}`].save(this.tripDestinations[index].arrivalDate));
      // $refs[`arrivalDateMenu${props.index}`].save(props.item.arrivalDate) 
    }
  },
};
</script>

<style lang="scss" scoped>
</style>


