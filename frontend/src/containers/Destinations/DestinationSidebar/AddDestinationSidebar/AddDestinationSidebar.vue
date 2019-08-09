<template>
  <v-card>
    <v-card-title class="primary title">
      <v-layout row>
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
          v-model="isValidForm"
      >
        <v-flex grid-list>
          <!-- Destination Name -->
          <v-flex>
            <v-text-field
                v-model="destination.destinationName"
                :value="destination.destinationName"
                :items="destinationTypes"
                label="Name"
                :rules="requiredRule"
            />
          </v-flex>

          <v-flex>
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

          <v-flex>
            <CountryPicker v-if="destinationToEdit" v-bind:country="destinationToEdit.destinationCountry" v-on:change="updateCountry"></CountryPicker>
            <CountryPicker v-else v-on:change="updateCountry"></CountryPicker>
          </v-flex>

          <v-flex>
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

          <v-flex>
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


          <v-flex>
            <v-flex>
              <v-text-field
                  v-model="destination.destinationLat"
                  :value="destination.destinationLat"
                  label="Latitude"
                  :rules="latitudeRules"
                  id="latitude"
              />
            </v-flex>
            <v-flex>
              <v-text-field
                  v-model="destination.destinationLon"
                  :value="destination.destinationLon"
                  label="Longitude"
                  :rules="longitudeRules"
                  id="longitude"
              />
            </v-flex>

          </v-flex>

          <v-flex>
            <v-spacer align="center">
              <v-btn
                  flat
                  color="secondary"
                  @click="getUserLocation"
              >Use My Current Location
              </v-btn>
            </v-spacer>
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
</template>

<script>
  import { rules } from "../../../../utils/rules";
  import { sendAddDestination } from "../../DestinationsService";
  import {
    requestCountries,
    requestDestinationTypes,
    requestTravellerTypes,
    requestDistricts
  } from "./ModifyDestinationSidebarService";
  import CountryPicker from "../../../../components/Country/CountryPicker"

  export default {
    name: "ModifyDestinationSidebar",
    components: {
      CountryPicker
    },
    props: {
    },
    data() {
      return {
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
          this.districts = await requestDistricts(countryId);
        } catch (e) {
          this.showError("Could not get districts");
        }
      },
      /**
       * Gets all traveller types
       */
      async getTravellerTypes() {
        try {
          this.travellerTypes = await requestTravellerTypes();
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
        this.$refs.form.resetValidation();
        this.$emit('closeEditor');
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
        }
      },
      updateCountry(newValue) {
        this.destination.destinationCountry = newValue;
      }
    },

    watch: {
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
  }
</script>

<style lang="scss" scoped>

  @import "../../../../styles/_variables.scss";

  .light-text {
    -webkit-text-fill-color: $darker-white;
  }
</style>