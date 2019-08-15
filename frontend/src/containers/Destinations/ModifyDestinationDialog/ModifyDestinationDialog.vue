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
        <v-form
          ref="form"
          v-model="isValidForm"
        >
          <v-flex grid-list>
            <!-- Destination Name -->
            <v-flex
              xs12
              sm10
              md8
              lg6
              xl4
              offset-xs0
              offset-sm1
              offset-md2
              offset-lg3
              offset-xl4
            >
              <v-text-field
                v-model="destination.destinationName"
                :value="destination.destinationName"
                :items="destinationTypes"
                label="Name"
                :rules="requiredRule"
              />
            </v-flex>

            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
            >
              <v-select
                v-model="destination.destinationType.destinationTypeId"
                :value="destination.destinationType.destinationTypeId"
                :items="destinationTypes"
                item-value="destinationTypeId"
                item-text="destinationTypeName"
                label="Type"
                :rules="requiredRule"
              />

            </v-flex>

            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
            >
              <CountryPicker v-if="destinationToEdit" v-bind:country="destinationToEdit.destinationCountry" v-on:change="updateCountry"></CountryPicker>
              <CountryPicker v-else v-on:change="updateCountry"></CountryPicker>
            </v-flex>

            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
            >
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
            </v-flex>

            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
            >
          <v-combobox
        v-model="destination.travellerTypes"
        :items="travellerTypes"
        item-text="travellerTypeName"
        item-value="travellerTypeId"
        :rules="requiredRule"
        label="Traveller Types"
        chips
        clearable
        solo
        multiple
      >

        <template v-slot:selection="data">
          <v-chip
            color="primary"
            text-color="white"
            :selected="data.selected"
            close
            @input="removeTravellerType(data.item)"
          >
            <strong>{{ data.item.travellerTypeName }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-combobox>
            </v-flex>


            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
              row
            >
              <v-flex
                xs12
                sm12
                md6
                lg6
                xl6
              >
                <v-text-field
                  v-model="destination.destinationLat"
                  :value="destination.destinationLat"
                  label="Latitude"
                  :rules="latitudeRules"
                  id="latitude"
                />
              </v-flex>
              <v-flex
                xs12
                sm12
                md6
                lg6
                xl6
              >
                <v-text-field
                  v-model="destination.destinationLon"
                  :value="destination.destinationLon"
                  label="Longitude"
                  :rules="longitudeRules"
                  id="longitude"
                />
              </v-flex>

            </v-flex>

            <v-flex
              xl4
              lg6
              md8
              sm10
              xs12
              offset-xl4
              offset-lg3
              offset-md2
              offset-sm1
              offset-xs0
            >
              <v-spacer align="center">
                <v-btn
                  flat
                  color="secondary"
                  @click="getUserLocation"
                >Use My Current Location
                </v-btn>
              </v-spacer>
              <v-switch
                color="secondary"
                label="Set to public"
                v-if="editMode"
                v-model="destination.isPublic"
              ></v-switch>
            </v-flex>

          </v-flex>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer align="right">
          <v-btn
            flat
            color="error"
            @click="closeDialog"
          >Cancel</v-btn>
          <v-btn
            flat
            color="success"
            @click="checkSubmission"
            :loading="formIsLoading"
          >Submit</v-btn>
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
import CountryPicker from "../../../components/Country/CountryPicker"
export default {
  name: "add-destination-dialog",
  components: {
    CountryPicker
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
    }
  },
  methods: {
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
        this.$refs.form.reset();
      }
      this.$refs.form.resetValidation();
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
      this.$refs.form.validate();
      if (this.isValidForm) {
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
      }
    },
    updateCountry(newValue) {
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

#latitude {
  padding: 0 10px 0 0;
}

#longitude {
  padding: 0 0 0 10px;
}
</style>