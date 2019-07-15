<template>
  <v-dialog v-model="isShowingDialog" width="60%">  
    <v-card >
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">{{ editMode ? "Edit Trip Destination" : "Add Trip Destination" }}</h2>
          </v-spacer>
        </v-layout>

      </v-card-title>
            <v-menu
              ref="arrivalDateMenu"
              v-model="arrivalDateMenu"
              :close-on-content-click="false"
              :nudge-right="40"
              :return-value.sync="tripDestination.arrivalDate"
              lazy
              transition="scale-transition"
              offset-y
              full-width
            >
              <template v-slot:activator="{ on }">
                <v-text-field
                  class="edit-field"
                  v-model="tripDestination.arrivalDate"
                  readonly
                  v-on="on"
                  color="secondary"
                  :rules="dateRules"

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
          <v-text-field
            v-model="tripDestination.arrivalTime"
            label="Picker in menu"
            prepend-icon="access_time"
            readonly
            v-on="on"
            color="secondary"
          ></v-text-field>
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
          >
            <template v-slot:activator="{ on }">
              <v-text-field
                class="edit-field"
                v-model="tripDestination.departureDate"
                readonly
                v-on="on"
                :rules="dateRules"
                color="secondary"

              ></v-text-field>
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
        <v-text-field
          v-model="tripDestination.departureTime"
          label="Picker in menu"
          prepend-icon="access_time"
          readonly
          v-on="on"
          color="secondary"
        ></v-text-field>
      </template>
      <v-time-picker
        v-if="departureTimeMenu"
        v-model="tripDestination.departureTime"
        full-width
        @click:minute="$refs.departureTimeMenu.save(tripDestination.departureTime)"
      ></v-time-picker>
    </v-menu>


    </v-card>
  </v-dialog>
</template>

<script>
import { rules } from "../../../../utils/rules";

export default {
  props: {
    isShowing: {
      type: Boolean,
      required: false
    },
    editMode: {
      type: Boolean,
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
      },
      arrivalDateMenu: false,
      arrivalTimeMenu: false,
      departureDateMenu: false,
      departureTimeMenu: false,
      dateRules: [rules.required]
    };
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
  
}
</script>

<style lang="scss" scoped>
@import "../../../../styles/variables";

.light-text {
  color: #FFF;
}
</style>


