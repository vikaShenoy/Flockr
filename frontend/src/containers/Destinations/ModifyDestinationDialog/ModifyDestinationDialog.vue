<template>
  <v-dialog
          v-model="dataDialog"
          width="60%"
          persistent>
    <v-card>
      <v-card-title class="primary title">
        <v-layout v-if="editMode" row>
          <v-spacer align="center">
            <h2 class="light-text">
              <v-icon large>location_on</v-icon>
              Edit Destination
            </h2>
          </v-spacer>
        </v-layout>
        <v-layout v-else row>
          <v-spacer align="center">
            <h2 class="light-text">
              <v-icon large>location_on</v-icon>
              Add Destination
            </h2>
          </v-spacer>
        </v-layout>
      </v-card-title>
      <v-card-text>
        <v-form
                ref="form"
                v-model="isValidForm">
          <v-flex grid-list>
            <!-- Destination Name -->
            <v-flex xs12 sm10 md8 lg6 xl4 offset-xs0 offset-sm1 offset-md2 offset-lg3 offset-xl4>
              <v-text-field
                      v-if="editMode"
                      v-model="editedDestination.destinationName"
                      :value="editedDestination.destinationName"
                      :items="destinationTypes"
                      label="Name"
                      :rules="requiredRule"/>
              <v-text-field
                      v-else
                      v-model="destination.destinationName"
                      :value="destination.destinationName"
                      :items="destinationTypes"
                      label="Name"
                      :rules="requiredRule"/>
            </v-flex>

            <v-flex xl4 lg6 md8 sm10 xs12 offset-xl4 offset-lg3 offset-md2 offset-sm1 offset-xs0>
              <v-select
                      v-if="editMode"
                      v-model="editedDestination.destinationType.destinationTypeId"
                      :value="editedDestination.destinationType.destinationTypeId"
                      :items="destinationTypes"
                      item-value="destinationTypeId"
                      item-text="destinationTypeName"
                      label="Type"
                      :rules="requiredRule"/>
              <v-select
                      v-else
                      v-model="destination.destinationType.destinationTypeId"
                      :value="destination.destinationType.destinationTypeId"
                      :items="destinationTypes"
                      item-value="destinationTypeId"
                      item-text="destinationTypeName"
                      label="Type"
                      :rules="requiredRule"/>
            </v-flex>

            <v-flex xl4 lg6 md8 sm10 xs12 offset-xl4 offset-lg3 offset-md2 offset-sm1 offset-xs0>
              <v-select
                      v-if="editMode"
                      v-model="editedDestination.destinationCountry.countryId"
                      :value="editedDestination.destinationCountry.countryId"
                      :items="countries"
                      item-value="countryId"
                      item-text="countryName"
                      label="Country"
                      :rules="requiredRule"/>
              <v-select
                      v-else
                      v-model="destination.destinationCountry.countryId"
                      :value="destination.destinationCountry.countryId"
                      :items="countries"
                      item-value="countryId"
                      item-text="countryName"
                      label="Country"
                      :rules="requiredRule"/>
            </v-flex>

            <v-flex xl4 lg6 md8 sm10 xs12 offset-xl4 offset-lg3 offset-md2 offset-sm1 offset-xs0>
              <v-select
                      v-if="editMode"
                      v-model="editedDestination.destinationDistrict.districtId"
                      :value="editedDestination.destinationDistrict.districtId"
                      :items="districts"
                      item-value="districtId"
                      item-text="districtName"
                      :disabled="editDistrictDisabled"
                      label="District"
                      :rules="requiredRule"/>
              <v-select
                      v-else
                      v-model="destination.destinationDistrict.districtId"
                      :value="destination.destinationDistrict.districtId"
                      :items="districts"
                      item-value="districtId"
                      item-text="districtName"
                      :disabled="districtDisabled"
                      label="District"
                      :rules="requiredRule"/>
            </v-flex>
            <v-flex xl4 lg6 md8 sm10 xs12 offset-xl4 offset-lg3 offset-md2 offset-sm1 offset-xs0 row>
              <v-flex xs12 sm12 md6 lg6 xl6>
                <v-text-field
                        v-if="editMode"
                        v-model="editedDestination.destinationLat"
                        :value="editedDestination.destinationLat"
                        label="Latitude"
                        :rules="latitudeRules"
                        id="latitude"
                        @click="checkSubmission"/>
                <v-text-field
                        v-else
                        v-model="destination.destinationLat"
                        :value="destination.destinationLat"
                        label="Latitude"
                        :rules="latitudeRules"
                        id="latitude"
                        @click="checkSubmission"/>
              </v-flex>
              <v-flex xs12 sm12 md6 lg6 xl6>
                <v-text-field
                        v-if="editMode"
                        v-model="editedDestination.destinationLon"
                        :value="editedDestination.destinationLon"
                        label="Longitude"
                        :rules="longitudeRules"
                        id="longitude"
                        @click="checkSubmission"/>
                <v-text-field
                        v-else
                        v-model="destination.destinationLon"
                        :value="destination.destinationLon"
                        label="Longitude"
                        :rules="longitudeRules"
                        id="longitude"
                        @click="checkSubmission"/>
              </v-flex>
 
            </v-flex>

            <v-flex xl4 lg6 md8 sm10 xs12 offset-xl4 offset-lg3 offset-md2 offset-sm1 offset-xs0>
              <v-spacer align="center">
                <v-btn
                        flat
                        color="secondary"
                        @click="getUserLocation">Use My Current Location
                </v-btn>
              </v-spacer>
              <v-switch color="secondary" label="Set to public" v-if="editMode" v-model="editedDestination.isPublic"></v-switch>
            </v-flex>

          </v-flex>
        </v-form>
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
  import {
    requestDestination,
    requestDistricts,
    sendAddDestination,
    sendUpdateDestination
  } from "../DestinationsService";

  import UserStore from "../../../stores/UserStore";

  export default {
    name: "add-destination-dialog",

    props: {
      dialog: {
        type: Boolean,
        required: true
      },
      destinationTypes: {
        type: Array,
        required: true
      },
      countries: {
        type: Array,
        required: true
      },
      editedDestination: {
        type: Object,
        required: false,
        destinationId: Number,
        destinationName: String,
        destinationType: {
          destinationTypeName: String,
          destinationTypeId: Number
        },
        destinationDistrict: {
          districtName: String,
          districtId: Number
        },
        destinationCountry: {
          countryName: String,
          countryId: Number
        },
        destinationLat: Number,
        destinationLon: Number,
        isPublic: Boolean
        // TODO: Add owner here when ready.
      },
      index: {
        type: Number,
        required: false
      },
      editMode: {
        type: Boolean,
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
        requiredRule: [rules.required],
        latitudeRules: [rules.required, rules.onlyNumbers, rules.absoluteRange(90.0, "latitude")],
        longitudeRules: [rules.required, rules.onlyNumbers, rules.absoluteRange(180.0, "longitude")],
        districtDisabled: true,
        editDistrictDisabled: false,
        districts: [],
        locationDisabled: false,
        isValidForm: false
      }
    },

    computed: {
      destCountry() {
        return this.destination.destinationCountry.countryId;
      },
      editDestCountry() {
        return this.editedDestination.destinationCountry.countryId;
      }
    },

    methods: {
      /**
       * Gets the users current geo location if permitted.
       */
      getUserLocation() {
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition((position) => {
            if (this.editMode) {
              this.editedDestination.destinationLat = position.coords.latitude;
              this.editedDestination.destinationLon = position.coords.longitude;
            } else {
              this.destination.destinationLat = position.coords.latitude;
              this.destination.destinationLon = position.coords.longitude;
            }
          }, (error) => {
            this.$emit("displayMessage", {
              show: true,
              text: error.message,
              color: "red"
            });
          });
        } else {
          this.$emit("displayMessage", {
            show: true,
            text: "Not supported by your browser",
            color: "red"
          });
        }
      },
      /**
       * Called when the country is selected.
       * Requests the district for the given country.
       */
      async onCountryChanged() {
        if (!this.editMode) {
          this.destination.destinationDistrict.districtName = null;
          this.destination.destinationDistrict.districtId = null;
        } else {
          this.$emit("editCountryChanged", this.index);
        }
        if (this.editMode && [undefined, null].includes(this.editedDestination.destinationCountry.countryId)) {
          this.editDistrictDisabled = true;
        } else if (!this.editMode && [undefined, null].includes(this.destination.destinationCountry.countryId)) {
          this.districtDisabled = true;
        } else {
          try {
            if (this.editMode) {
              this.districts = await requestDistricts(this.editedDestination.destinationCountry.countryId);
              this.editDistrictDisabled = false;
            } else {
              this.districts = await requestDistricts(this.destination.destinationCountry.countryId);
              this.districtDisabled = false;
            }
          } catch (error) {
            this.$emit("displayMessage", {
              show: true,
              text: error.message,
              color: "red"
            });
          }
        }
      },
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
        if (!this.editMode) {
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
          this.$refs.form.reset();
        }
        this.$refs.form.resetValidation()
      },
      /**
       * Called when the submit button is selected.
       * Checks the form is valid and sends the request to add a new destination if so.
       */
      async checkSubmission() {
        this.$refs.form.validate();
        if (this.isValidForm) {
          let destinationInfo;
          if (!this.editMode) {
            destinationInfo = {
              "destinationName": this.destination.destinationName,
              "destinationTypeId": this.destination.destinationType.destinationTypeId,
              "countryId": this.destination.destinationCountry.countryId,
              "districtId": this.destination.destinationDistrict.districtId,
              "latitude": this.destination.destinationLat,
              "longitude": this.destination.destinationLon,
            };

            const userIdUrl = this.$route.params.userId;

            if (userIdUrl) {
              destinationInfo.userId = userIdUrl;
            }


          } else {
            destinationInfo = {
              "destinationName": this.editedDestination.destinationName,
              "destinationTypeId": this.editedDestination.destinationType.destinationTypeId,
              "countryId": this.editedDestination.destinationCountry.countryId,
              "districtId": this.editedDestination.destinationDistrict.districtId,
              "latitude": this.editedDestination.destinationLat,
              "longitude": this.editedDestination.destinationLon,
              "isPublic": this.editedDestination.isPublic
            };
          }
          if (!this.editMode) {
            try {
              this.destination = await sendAddDestination(destinationInfo);
              this.$emit("addNewDestination", this.destination);
              this.closeDialog();
            } catch (error) {
              if (error.message === "Conflict") {
                error.message = "Destination already exists";
              }
              this.$emit("displayMessage", {
                text: error.message,
                color: "red"
              });
            }
          } else {
            try {
              await sendUpdateDestination(destinationInfo, this.editedDestination.destinationId);
              const updatedDestination = await requestDestination(this.editedDestination.destinationId);
              this.$emit("updateDestination", updatedDestination, this.index);
            } catch (error) {
              let message;
              if (error.status === 400) {
                message = error.response.body.message
              } else {
                message = 'Something went wrong';
              }
              this.$emit("displayMessage", {
                text: message,
                color: "red"
              });
            }
          }
        }
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
      },
      destCountry: {
        handler: "onCountryChanged",
        immediate: true
      },
      editDestCountry: {
        handler: "onCountryChanged",
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

  #latitude {
    padding: 0 10px 0 0;
  }

  #longitude {
    padding: 0 0 0 10px;
  }
</style>