<template>
  <v-data-table
    :headers="headers"
    :items="tripDestinations"
    item-key="id"
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
        <v-text-field
          class="date"
          v-model="props.item.arrivalDate"
          type="date"
          color="secondary"
        ></v-text-field>
      </td>
      <td>
        <v-text-field
          class="time"
          v-model="props.item.arrivalTime"
          type="time"
          color="secondary"
        ></v-text-field>
      </td>
      <td>
        <v-text-field
          class="date"
          v-model="props.item.departureDate"
          type="date"
          color="secondary"
        ></v-text-field>
      </td>
      <td>
        <v-text-field
          class="time"
          v-model="props.item.departureTime"
          type="time"
          color="secondary"
        ></v-text-field>
      </td>
      <td v-if="isEditing && tripDestinations.length > 2">
        <v-icon
          class="delete-dest"
          @click="deleteDest(props.index)"
        >delete</v-icon>
      </td>
    </template>
  </v-data-table>

</template>

<script>
import Sortable from "sortablejs";
import moment from "moment";
import { getDestinations } from "./TripTableService";
import { getYourDestinations } from '../../containers/Destinations/DestinationsService';

export default {
  props: {
    tripDestinations: {
      type: Array
    },
    isEditing: {
      type: Boolean
    }
  },
  mounted() {
    this.getDestinations();
    this.initSorting();
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
      currentDate: moment().format("YYYY-MM-DD")
    };
  },
  methods: {
		/**
		 * Get all destinations, then filter to remove duplicates.
		 * */
    async getDestinations() {
      try {
        const [publicDestinations, yourDestinations] = await Promise.all([getDestinations(), getYourDestinations()]);
        
        // Need to filter out duplicate destinations
        const destinationsFound = new Set();
        const allDestinations = [...publicDestinations, ...yourDestinations].filter(destination => {
          return !destinationsFound.has(destination.destinationId) && destinationsFound.add(destination.destinationId);
        });

        this.destinations = allDestinations;
      } catch (e) {
        console.log(e);
      }
    },

		/**
		 * Allow the user to sort the trip table with drag/drop.
		 * */
    initSorting() {
      let table = document.querySelector(".v-datatable tbody");
      Sortable.create(table, {
        onEnd: ({ newIndex, oldIndex })  => {
          const movedRow = this.tripDestinations.splice(oldIndex, 1)[0];
          this.tripDestinations.splice(newIndex, 0, movedRow);
        }
      });
    },
    /**
     * Delete destination by index
     * @param {number} index  The index to delete
     */
    deleteDest(index) {
      this.tripDestinations.splice(index, 1);
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";
.date {
  max-width: 90px;
}

.time {
  max-width: 70px;
}

.delete-dest {
  cursor: pointer;

  &:hover {
    color: $error;
  }
}
</style>


