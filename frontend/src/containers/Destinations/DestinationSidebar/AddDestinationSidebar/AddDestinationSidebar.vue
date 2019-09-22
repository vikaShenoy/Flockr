<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <!-- <v-card> -->
    <!-- <v-card-text> -->
  <div class="form-padding">
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
                :rules="requiredRule"></v-text-field>
          </v-flex>

          <v-flex>
            <v-select
                v-model="destination.destinationType.destinationTypeId"
                :value="destination.destinationType.destinationTypeId"
                :items="destinationTypes"
                item-value="destinationTypeId"
                item-text="destinationTypeName"
                label="Type"
                :rules="requiredRule"></v-select>

          </v-flex>

          <v-flex>
            <CountryPicker v-on:change="updateCountry"></CountryPicker>
          </v-flex>

          <v-flex>
            <v-text-field
              v-model="destination.destinationDistrict.districtName"
              :value="destination.destinationDistrict.districtName"
              label="District"
              :rules="requiredRule">
            </v-text-field>
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
                  v-model="latitude"
                  :value="latitude"
                  label="Latitude"
                  :rules="latitudeRules"
                  id="latitude"></v-text-field>
            </v-flex>
            <v-flex>
              <v-text-field
                  v-model="longitude"
                  :value="longitude"
                  label="Longitude"
                  :rules="longitudeRules"
                  id="longitude"></v-text-field>
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
    <!-- </v-card-text> -->
    <!-- <v-card-actions> -->
      <v-spacer align="right">
        <v-btn
            flat
            color="success"
            @click="checkSubmission"
            :loading="formIsLoading"
        >Submit</v-btn>
      </v-spacer>
  </div>
    <!-- </v-card-actions>
  </v-card>-->
</template>

<script>
  import {rules} from "../../../../utils/rules";
  import {sendAddDestination} from "../../DestinationsService";
  import {
    requestCountries,
    requestDestinationTypes,
    requestDistricts,
    requestTravellerTypes
  } from "./AddDestinationSidebarService";
  import CountryPicker from "../../../../components/Country/CountryPicker"

  export default {
    name: "AddDestinationSidebar",
    components: {
      CountryPicker
    },
    props: {
      latitude: null,
      longitude: null
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
          this.countries = await requestCountries();
        } catch (e) {
          this.showError("Could not get countries");
        }
      },
      /**
       * Gets destination types to populate destination types with and sets it as state
       */
      async getDestinationTypes() {
        this.destinationTypes = await requestDestinationTypes();
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
            districtName: null
          },
          travellerTypes: [],
          destinationLat: "",
          destinationLon: "",
          destinationCountry: {
            countryId: null,
            countryName: null
          },
        };
        this.$refs.form.reset();
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
          try {
            this.destination.destinationLat = this.latitude;
            this.destination.destinationLon = this.longitude;
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
    }
  }
</script>

<style lang="scss" scoped>

  @import "../../../../styles/_variables.scss";

  .light-text {
    -webkit-text-fill-color: $darker-white;
  }

  .form-padding {
    padding-left: 5px;
    padding-right: 5px;
  }
</style>