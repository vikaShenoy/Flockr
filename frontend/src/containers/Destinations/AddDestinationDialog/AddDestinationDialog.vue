<template>
  <v-dialog
    v-model="dataDialog"
    width="70%%">
    <v-card>
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text"><v-icon large>location_on</v-icon>Add Destination</h2>
          </v-spacer>
        </v-layout>
      </v-card-title>
      <v-card-text>
        <v-layout grid-list>
          <v-form ref="form">
            <v-flex>
              <v-text-field
                v-model="destination.destinationName"
                :value="destination.destinationName"
                :rules="nameRules"/>
            </v-flex>
          </v-form>
        </v-layout>
      </v-card-text>
      <v-card-actions>
        <v-spacer align="right">
          <v-btn flat color="success" @click="checkSubmission">Submit</v-btn>
          <v-btn flat color="error" @click="closeDialog">Cancel</v-btn>
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>

  import {rules} from "../../../utils/rules"
  export default {
    name: "add-destination-dialog",

    props: {
      dialog : {
        Type: Boolean,
        required: true
      }
    },

    data() {
      return {
        dataDialog: false,
        destination: {
          destinationName: "",
          destinationType: {
            destinationTypeId: null,
            destinationTypeName: null
          },
          destinationDistrict: {
            districtId: null,
            districtName: null
          },
          destinationLat: "",
          destinationLon: "",
          destinationCountry: {
            countryId: null,
            countryName: null
          }
        },
        nameRules: [rules.required]
      }
    },

    methods: {
      /**
       * Called when the dialog prop is modified.
       * Updates the dataDialog to match the dialog prop.
       */
      onDialogChanged() {
        this.dataDialog = this.dialog;
      },
      /**
       * Called when the dataDialog object is modified.
       * Emits an event to the parent to notify it of the new value of dataDialog.
       */
      onDataDialogChanged() {
        this.$emit("dialogChanged", this.dataDialog);
      },
      /**
       * Closes the dialog window and resets all fields to default values.
       */
      closeDialog() {
        this.dataDialog = false;
        this.destination = {
          destinationName: "",
          destinationType: {
            destinationTypeId: null,
            destinationTypeName: null
          },
          destinationDistrict: {
            districtId: null,
            districtName: null
          },
          destinationLat: "",
          destinationLon: "",
          destinationCountry: {
            countryId: null,
            countryName: null
          }
        };
      },
      /**
       * Called when the submit button is selected.
       * Checks the form is valid and sends the request to add a new destination if so.
       */
      checkSubmission() {

      }
    },

    watch: {
      dialog: {
        handler: "onDialogChanged",
        immediate: true
      },
      dataDialog: {
        handler: "onDataDialogChanged",
        immediate: true
      }
    }

  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";
  @import "../../../styles/_defaults.scss";

  .light-text {
    -webkit-text-fill-color: $darker-white;
  }
</style>