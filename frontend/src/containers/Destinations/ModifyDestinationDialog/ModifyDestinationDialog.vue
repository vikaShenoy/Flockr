<template>
  <v-dialog
    v-model="dataDialog"
    width="60%"
    persistent
  >
    <v-card>
      <v-card-title class="primary title">
        <v-layout row>
          <v-spacer align="center">
            <h2 class="light-text">
              <v-icon large>location_on</v-icon>
              {{ editMode ? "Edit Destination" : "Add Destination" }}
            </h2>
          </v-spacer>
        </v-layout>
      </v-card-title>

      

      <v-card-text>

        <v-stepper v-model="currStepperStep">
        <v-stepper-header>
          <v-stepper-step :complete="currStepperStep > 1" step="1">Basic Info</v-stepper-step>
          <v-divider />
          <v-stepper-step :complete="currStepperStep > 2" step="2">Location</v-stepper-step>
        </v-stepper-header>

        <v-stepper-items>
          <v-stepper-content step="1">
            <v-card class="mb-5" flat>
              <v-text-field v-model="destination.destinationName" :rules="requiredRule"
                :value="destination.destinationName" color="secondary" label="Name" />

              <v-select v-model="destination.destinationType.destinationTypeId"
                :value="destination.destinationType.destinationTypeId" :items="destinationTypes"
                item-value="destinationTypeId" item-text="destinationTypeName" label="Type" :rules="requiredRule" />

              <CountryPicker v-if="destinationToEdit" v-bind:country="destinationToEdit.destinationCountry" v-on:change="updateCountry"></CountryPicker>
              <CountryPicker v-else v-on:change="updateCountry"></CountryPicker>

              <v-select
                v-model="destination.destinationDistrict.districtId"
                :value="destination.destinationDistrict.districtId"
                :items="districts"
                item-value="districtId"
                item-text="districtName"
                :disabled="!destination.destinationCountry.countryId"
                label="District"
                :rules="requiredRule"
              />

              <v-combobox v-model="destination.travellerTypes" :items="travellerTypes" item-text="travellerTypeName"
                item-value="travellerTypeId" :rules="requiredRule" label="Traveller Types" chips clearable solo
                multiple>
                <template v-slot:selection="data">
                  <v-chip color="primary" text-color="white" :selected="data.selected" close
                    @input="removeTravellerType(data.item)">
                    <strong>{{ data.item.travellerTypeName }}</strong>&nbsp;
                  </v-chip>
                </template>
              </v-combobox>

            </v-card>

            <v-switch
              color="secondary"
              label="Set to public"
              v-if="editMode"
              v-model="destination.isPublic"
            />

            <v-btn color="error" @click="closeDialog">Cancel</v-btn>
            <v-btn color="primary" @click="currStepperStep = 2" :disabled="false">Continue</v-btn>
          </v-stepper-content>

          <v-stepper-content step="2">
            <v-card id="location-stepper" class="mb-5" flat>
              <div id="map">
                <DestinationMap
                  :destinations="mapDestinations"
                  @coordinates-selected="handleCoordinateSelection"
                />
              </div>

              <div id="not-map">
                <v-container grid-list-md text-xs-center>
                  <v-layout row wrap>
                    <v-flex xs6>
                      <v-text-field v-model="destination.destinationLat" :value="destination.destinationLat"
                        label="Latitude" :rules="latitudeRules" id="latitude" />
                    </v-flex>
                    <v-flex xs6>
                      <v-text-field v-model="destination.destinationLon" :value="destination.destinationLon"
                        label="Longitude" :rules="longitudeRules" id="longitude" />
                    </v-flex>
                      
                    <v-flex xs12>
                      <v-btn id="use-my-location-button" flat color="secondary" @click="getUserLocation">
                        Use My Current Location
                      </v-btn>
                    </v-flex>
                  </v-layout>
                </v-container>

                <v-btn @click="currStepperStep = 1">Go back</v-btn>
                <v-btn color="success" @click="checkSubmission" :loading="formIsLoading">Done</v-btn>
              </div>
            </v-card>
          </v-stepper-content>
        </v-stepper-items>
      </v-stepper>

      </v-card-text>
      <v-card-actions>
        <v-spacer align="right">
          
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import { rules } from "../../../utils/rules";
import {
  requestDestination,
  sendAddDestination,
  sendUpdateDestination
} from "../DestinationsService";
import {
  requestCountries,
  requestDestinationTypes,
  requestTravellerTypes,
  requestDistricts
} from "./ModifyDestinationDialogService";
import ErrorSnackbar from "../../../components/Snackbars/ErrorSnackbar";
import CountryPicker from "../../../components/Country/CountryPicker"
import DestinationMap from "../../../components/DestinationMap/DestinationMap";
export default {
  name: "add-destination-dialog",
  components: {
    CountryPicker, DestinationMap
  },
  props: {
    dialog: {
      type: Boolean,
      required: true
    },
    destinationToEdit: {
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
      currStepperStep: 0,
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
        travellerTypes: [],
        destinationLat: "",
        destinationLon: "",
        destinationCountry: {
          countryId: null,
          countryName: null
        }
      },
      requiredRule: [rules.required],
      latitudeRules: [
        rules.required,
        rules.onlyNumbers,
        rules.absoluteRange(90.0, "latitude")
      ],
      longitudeRules: [
        rules.required,
        rules.onlyNumbers,
        rules.absoluteRange(180.0, "longitude")
      ],
      districtDisabled: true,
      editDistrictDisabled: false,
      countries: [],
      districts: [],
      destinationTypes: [],
      travellerTypes: [],
      locationDisabled: false,
      isValidForm: false,
      formIsLoading: false
    };
  },
  mounted() {
    this.getCountries();
    this.getDestinationTypes();
    this.getTravellerTypes();

    if (this.editMode) {
      // Shallow copy to not overwrite what is currently being showed
      this.destination = { ...this.destinationToEdit };
    }
  },
  computed: {
    destCountry() {
      return this.destination.destinationCountry.countryId;
    },
    /**
     * Returns the destinations used by the map.
     * @returns {Array<Object>} a list with either no destination or one destination
     */
    mapDestinations() {
      if (!this.editMode) {
        return []
      } else {
        const destination = { ...this.destinationToEdit };

        // overwrite coordinates from prop with coordinates from form
        // to update the map marker when new coordinates are selected
        const { destinationLat, destinationLon } = this.destination;
        if (destinationLat && destinationLon) {
          destination.destinationLat = destinationLat;
          destination.destinationLon = destinationLon;
        }
        return [destination];
      }
      editMode ? [destinationToEdit] : []
    }
  },
  methods: {
    /**
     * Called when the user selects coordinates in the map.
     * @param {Object} coordinates contains the selected coordinates
     */
    handleCoordinateSelection(coordinates) {
      const { latitude, longitude } = coordinates;
      this.destination.destinationLat = latitude;
      this.destination.destinationLon = longitude;
    },
    /**
     * Gets the users current geo location if permitted.
     */
    getUserLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          position => {
            this.destination.destinationLat = position.coords.latitude;
            this.destination.destinationLon = position.coords.longitude;
          },
          error => {
            this.$emit("displayMessage", {
              show: true,
              text: error.message,
              color: "red"
            });
          }
        );
      } else {
        this.$emit("displayMessage", {
          show: true,
          text: "Not supported by your browser",
          color: "red"
        });
      }
    },
    /**
     * Gets countries to populate select input with
     */
    async getCountries() {
      try {
        const countries = await requestCountries();
        this.countries = countries;
      } catch (e) {
        this.showError("Could not get countries");
      }
    },
    /**
     * Gets destination types to populate destination types with and sets it as state
     */
    async getDestinationTypes() {
      const destinationTypes = await requestDestinationTypes();
      this.destinationTypes = destinationTypes;
    },
    /**
     * Gets districts in a specific country and sets it as state
     * @param {number} countryId The country of where to get districts from
     */
    async getDistricts(countryId) {
      try {
        const districts = await requestDistricts(countryId);
        this.districts = districts;
      } catch (e) {
        this.showError("Could not get districts");
      }
    },
    /**
     * Gets all traveller types
     */
    async getTravellerTypes() {
      try {
        const travellerTypes = await requestTravellerTypes();
        this.travellerTypes = travellerTypes;
      } catch (e) {
        this.showError("Could not get traveller types");
      }
    },
    showError(errorMessage) {
      this.$emit("showError", errorMessage);
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
          travellerTypes: [],
          destinationLat: "",
          destinationLon: "",
          destinationCountry: {
            countryId: null,
            countryName: null
          }
        };
      }
    },
    /**
     * Removes a traveller type from chips
     */
    removeTravellerType(item) {
      this.destination.travellerTypes.splice(this.destination.travellerTypes.indexOf(item), 1);
    },
    /**
     * Called when the submit button is selected.
     * Checks the form is valid and sends the request to add a new destination if so.
     */
    async checkSubmission() {
        this.formIsLoading = true;
        if (!this.editMode) {
          try {
            const insertedDestination = await sendAddDestination(this.destination);
            this.$emit("addNewDestination", insertedDestination);

            this.closeDialog();
            this.formIsLoading = false;
          } catch (error) {
            this.formIsLoading = false;
            const errorMessage =
              error.message === "Conflict"
                ? "Destination already exists"
                : error.message;
            this.showError(errorMessage);
          }
        } else {
            try {
              await sendUpdateDestination(
                this.destination,
                this.destination.destinationId
              );
              const updatedDestination = await requestDestination(
                this.destination.destinationId
              );
              this.$emit("updateDestination", updatedDestination);
              this.formIsLoading = false;
            } catch (error) {
              const message =
                error.status === 400
                  ? error.response.body.message
                  : "Something went wrong";
              this.formIsLoading = false;
              this.showError(message);
            }
        }
    },
    updateCountry(newValue) {
      console.log("I am setting the country: ", newValue);
      this.destination.destinationCountry = newValue;
    }
  },
  watch: {
    /**
     * Called when the dialog prop is modified.
     * Updates the dataDialog to match the dialog prop.
     * Also updates destination to reflect what is currently being shown
     */
    dialog() {
      if (this.editMode) {
        // Deep clones what is being sent in
        this.destination = JSON.parse(JSON.stringify(this.destinationToEdit));
      }
      this.dataDialog = this.dialog;
    },
    /**
     * Called when the dataDialog object is modified.
     * Emits an event to the parent to notify it of the new value of dataDialog.
     */
    dataDialog() {
      this.$emit("dialogChanged", this.dataDialog);
    },
    /**
     * Called when the country is selected.
     * Requests the district for the given country.
     */
    async destCountry() {
      try {
        this.districts = await requestDistricts(
          this.destination.destinationCountry.countryId
        );
      } catch (error) {
        this.$emit("displayMessage", {
          show: true,
          text: error.message,
          color: "red"
        });
      }
    },
  }
};
</script>

<style lang="scss" scoped>
@import "../../../styles/_variables.scss";
@import "../../../styles/_defaults.scss";

.light-text {
  -webkit-text-fill-color: $darker-white;
}

#location-stepper {

  #map {
    width: 100%;
    height: 30vh;
  }

  #not-map {
    margin-top: 20px;
    margin-bottom: -30px;

    .container {
      padding: 0;
    }
  }
}
</style>