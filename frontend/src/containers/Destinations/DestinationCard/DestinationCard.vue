<template>
    <v-card elevation="10"
            class="destination-card row col-md-12">
        <div class="title-card col-md-3">
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Name"
                        :value="destination.destinationName"
                        v-model="destination.destinationName"
                        @blur="validateName"
                        :error-messages="nameErrors"
                ></v-text-field>
            </div>
            <h2 v-else class="name-header">{{ destination.destinationName }}</h2>
            <div class="body-card col-md-12">
                <v-img class="image" src="https://picsum.photos/510/300?random"></v-img>
            </div>
        </div>
        <div class="col-md-5">
            <div class="row">
                <div class="basic-info-label"><p><b>Type</b></p></div>
                <div v-if="editMode" class="basic-info">
                    <v-select
                            label="Destination Type"
                            :items="destinationTypes.names"
                            :value="destination.destinationType.destinationTypeName"
                            v-model="destination.destinationType.destinationTypeName"
                            @blur="validateType"
                            :error-messages="typeErrors"
                    ></v-select>
                </div>
                <div v-else class="basic-info-label">{{ destination.destinationType.destinationTypeName }}</div>
            </div>
            <hr class="divider"/>
            <div class="row">
                <div class="basic-info-label"><p><b>District</b></p></div>
                <div v-if="editMode" class="basic-info">
                    <v-select
                            label="District"
                            :items="districts.names"
                            :disabled="districtDisabled"
                            :value="destination.destinationDistrict.districtName"
                            v-model="destination.destinationDistrict.districtName"
                            @blur="validateDistrict"
                            :error-messages="districtErrors"
                    ></v-select>
                </div>
                <div v-else class="basic-info-label">{{ destination.destinationDistrict.districtName }}</div>
            </div>
            <hr class="divider"/>
        </div>
        <div class="col-md-4">
            <div v-if="editMode" class="name-header">
                <v-select
                        label="Destination Country"
                        :items="countries.names"
                        :value="destination.destinationCountry.countryName"
                        v-model="destination.destinationCountry.countryName"
                        @blur="validateCountry"
                        :error-messages="countryErrors"
                ></v-select>
            </div>
            <h2 v-else class="name-header">{{ destination.destinationCountry.countryName }}</h2>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Latitude"
                        :value="destination.destinationLat"
                        v-model="destination.destinationLat"
                        v-on:keypress="isValidLatitude"
                        @blur="validateLatitude"
                        :error-messages="latitudeErrors"
                ></v-text-field>
            </div>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Longitude"
                        :value="destination.destinationLon"
                        v-model="destination.destinationLon"
                        v-on:keypress="isValidLongitude"
                        @blur="validateLongitude"
                        :error-messages="longitudeErrors"
                ></v-text-field>
            </div>
            <v-img v-else class="image" src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </div>
        <v-btn v-if="editMode" fab dark id="save-destination-button" @click="saveDestination">
            <v-icon dark>check_circle</v-icon>
        </v-btn>
        <v-btn v-else fab dark class="edit-button" id="edit-destination-button" @click="editDestination">
            <v-icon dark>edit</v-icon>
        </v-btn>
        <v-btn fab dark class="delete-button" id="delete-destination-button" @click="deleteOnClick">
            <v-icon dark>remove</v-icon>
        </v-btn>
    </v-card>
</template>

<script>

  const axios = require("axios");
  import { endpoint } from "../../../utils/endpoint";

  export default {
    name: "DestinationCard",

    props: {
      destination: {
        id: {
          type: Number,
          required: true
        },
        destinationName: {
          type: String,
          required: true
        },
        destinationType: {
          destinationTypeName: {
            type: String,
            required: true
          },
          destinationTypeId: {
            type: Number,
            required: true
          }
        },
        destinationDistrict: {
          districtName: {
            type: String,
            required: true
          },
          districtId: {
            type: Number,
            required: true
          }
        },
        destinationCountry: {
          countryName: {
            type: String,
            required: true
          },
          countryId: {
            type: Number,
            required: true
          }
        },
        destinationLat: {
          type: Number,
          required: true
        },
        destinationLon: {
          type: Number,
          required: true
        }
      },
      editMode: {
        type: Boolean,
        required: true
      },
      countries: {
        names: {
          type: Array,
          required: true
        },
        ids: {
          type: Array,
          required: true
        }
      },
      destinationTypes: {
        names: {
          type: Array,
          required: true
        },
        ids: {
          type: Array,
          required: true
        }
      },
      deleteOnClick: {
        type: Function,
        required: true
      }
    },

    computed: {
      countryName() {
        return this.destination.destinationCountry.countryName;
      },
      typeName() {
        return this.destination.destinationType.destinationTypeName;
      },
      districtName() {
        return this.destination.destinationDistrict.districtName;
      }
    },

    watch: {

      typeName() {
        this.onTypeChanged();
      },

      countryName() {
        this.onCountryChanged();
      },

      districtName() {
        this.onDistrictChanged();
      },

      'editMode': {
        handler: 'onEditModeChanged',
        immediate: true
      },

      'dataEditMode': {
        handler: 'onDataEditModeChanged',
        immediate: true
      },

      'hasInvalidName': {
        handler: 'validityCheck',
        immediate: true
      },

      'hasInvalidType': {
        handler: 'validityCheck',
        immediate: true
      },

      'hasInvalidDistrict': {
        handler: 'validityCheck',
        immediate: true
      },

      'hasInvalidCountry': {
        handler: 'validityCheck',
        immediate: true
      },

      'hasInvalidLatitude': {
        handler: 'validityCheck',
        immediate: true
      },

      'hasInvalidLongitude': {
        handler: 'validityCheck',
        immediate: true
      }
    },

    data() {
      return {
        districts: {
          names: [],
          ids: []
        },
        districtDisabled: false,
        nameErrors: [],
        typeErrors: [],
        districtErrors: [],
        countryErrors: [],
        latitudeErrors: [],
        longitudeErrors: [],
        hasInvalidName: false,
        hasInvalidType: false,
        hasInvalidDistrict: false,
        hasInvalidCountry: false,
        hasInvalidLatitude: false,
        hasInvalidLongitude: false,
        hasInvalidInput: false,
        dataEditMode: true
      }
    },

    methods: {
      saveDestination: async function () {
        await this.validateAll();
        if (!this.hasInvalidInput) {
          let destinationValues = {
            "destinationName": this.destination.destinationName,
            "destinationTypeId": this.destination.destinationType.destinationTypeId,
            "countryId": this.destination.destinationCountry.countryId,
            "districtId": this.destination.destinationDistrict.districtId,
            "latitude": this.destination.destinationLat,
            "longitude": this.destination.destinationLon,
          };
          // If the destination is new
          if (this.destination.destinationId === null) {
            axios.post(endpoint("/destinations"), destinationValues)
                .then(response => {
                  if (response.request.status === 200) {
                    this.dataEditMode = !this.dataEditMode;
                  }
                })
                .catch(error => alert(error));
          } else {
            axios.put(endpoint("/destinations/" + this.destination.destinationId), destinationValues,
                {headers: {
                  "content-type": "application/JSON"
                  }})
                .then(response => {
                  if (response.request.status === 200) {
                    this.dataEditMode = !this.dataEditMode;
                  }
                })
                .catch(error => alert(error));
          }
        }
      },

      editDestination: async function () {
        this.dataEditMode = !this.dataEditMode;
      },

      isValidLatitude: function (event) {
        let currentValue = event.target.value + event.key;
        // Prevent the keystroke if the value would be out of a valid latitude range or not numeric
        if (isNaN(currentValue) || parseFloat(currentValue) > 90 || parseFloat(currentValue) < -90) {
          event.preventDefault();
        }
      },

      isValidLongitude: function (event) {
        let currentValue = event.target.value + event.key;
        // Prevent the keystroke if the value would be out of a valid latitude range or not numeric
        if (isNaN(currentValue) || parseFloat(currentValue) > 180 || parseFloat(currentValue) < -180) {
          event.preventDefault();
        }
      },

      validateName: function () {
        if (this.destination.destinationName === "") {
          this.nameErrors = [ "Name is required" ];
          this.hasInvalidName = true;
        } else {
          this.nameErrors = [];
          this.hasInvalidName = false;
        }
      },

      validateType: function () {
        if (this.destination.destinationType.destinationTypeName === null) {
          this.typeErrors = [ "Type is required" ];
          this.hasInvalidType = true;
        } else if (/\d/.test(this.destination.destinationType.destinationTypeName)) {
          this.typeErrors = [ "No numbers allowed" ];
          this.hasInvalidType = true;
        } else {
          this.typeErrors = [];
          this.hasInvalidType = false;
        }
      },

      validateDistrict: function () {
        if (this.destination.destinationDistrict.districtName === null) {
          this.districtErrors = [ "District is required" ];
          this.hasInvalidDistrict = true;
        } else {
          this.districtErrors = [];
          this.hasInvalidDistrict = false;
        }
      },

      validateCountry: function () {
        if (this.destination.destinationCountry.countryName === null) {
          this.countryErrors = ["Country is required"];
          this.hasInvalidCountry = true;
        } else if (/\d/.test(this.destination.destinationCountry.countryName)) {
          this.countryErrors = ["No numbers allowed"];
          this.hasInvalidCountry = true;
        } else {
          this.countryErrors = [];
          this.hasInvalidCountry = false;
        }
      },
      validateLatitude: function () {
        if (this.destination.destinationLat === "") {
          this.latitudeErrors = ["Latitude is required"];
          this.hasInvalidLatitude = true;
        } else {
          this.latitudeErrors = [];
          this.hasInvalidLatitude = false;
        }
      },

      validateLongitude: function () {
        if (this.destination.destinationLon === "") {
          this.longitudeErrors = ["Longitude is required"];
          this.hasInvalidlongitude = true;
        } else {
          this.longitudeErrors = [];
          this.hasInvalidlongitude = false;
        }
      },

      validateAll: function () {
        this.validateName();
        this.validateCountry();
        this.validateDistrict();
        this.validateType();
        this.validateLatitude();
        this.validateLongitude();
      },

      onEditModeChanged (newValue) {
        this.dataEditMode = newValue;
      },

      onDataEditModeChanged (newValue) {
        if (this.$el) {
          this.$emit('editModeChanged', newValue, this.$el)
        }
      },

      onTypeChanged () {
        let typeIndex = this.destinationTypes.names.indexOf(this.destination.destinationType.destinationTypeName);
        this.destination.destinationType.destinationTypeId = this.destinationTypes.ids[typeIndex];
      },

      onCountryChanged () {
        let countryIndex = this.countries.names.indexOf(this.destination.destinationCountry.countryName);
        this.destination.destinationCountry.countryId = this.countries.ids[countryIndex];
        this.destination.destinationDistrict.districtName = null;
        this.destination.destinationDistrict.districtId = null;
        if (this.destination.destinationCountry.countryName !== null) {
          axios.get(endpoint("/destinations/countries/" + this.countries.ids[countryIndex] + "/districts"))
              .then(response => {
                let currentDistricts = response.data;
                this.districts = {
                  names: [],
                  ids: []
                };
                for (let index in currentDistricts) {
                  this.districts.names.push(currentDistricts[index].districtName);
                  this.districts.ids.push(currentDistricts[index].districtId);
                }
              })
              .catch(error => alert(error));
          this.districtDisabled = false;
        } else {
          this.districtDisabled = true;
        }
      },

      onDistrictChanged () {
        let districtIndex = this.districts.names.indexOf(this.destination.destinationDistrict.districtName);
        this.destination.destinationDistrict.districtId = this.districts.ids[districtIndex];
      },

      validityCheck: function () {
        // Invalid input is true if any of the individual inputs are true
        this.hasInvalidInput = this.hasInvalidName || this.hasInvalidType || this.hasInvalidDistrict ||
            this.hasInvalidCountry || this.hasInvalidLatitude || this.hasInvalidlongitude;
      }
    },

    mounted: function() {
      if (this.destination.destinationCountry.countryName !== null) {
        axios.get(endpoint("/destinations/countries/" + this.destination.destinationCountry.countryId + "/districts"))
            .then(response => {
              let currentDistricts = response.data;
              this.districts = {
                names: [],
                ids: []
              };
              for (let index in currentDistricts) {
                this.districts.names.push(currentDistricts[index].districtName);
                this.districts.ids.push(currentDistricts[index].districtId);
              }
            })
            .catch(error => alert(error));
        this.districtDisabled = false;
      } else {
        this.districtDisabled = true;
      }
    }
  }

</script>

<style lang="scss" scoped>
    @import "../../../styles/_variables.scss";

    .divider {
        margin: 0 0 20px;
    }

    .image {
        margin: 0 0 20px;
    }

    .basic-info {
        text-align: center;
        width: 50%;
    }

    .basic-info-label {
        margin: 10px 0 10px;
        text-align: center;
        width: 50%;
    }

    .name-header {
        margin: 0 0 20px;
        text-align: center;
        width: 100%
    }

    .destination-card {
        width: 100%;
        margin: 10px 0 0;
    }

    .destinations-panel :hover #save-destination-button {
        visibility: visible;
    }

    .destinations-panel :hover #delete-destination-button {
        visibility: visible;
    }

    .destinations-panel :hover #edit-destination-button {
        visibility: visible;
    }

    .destination-card #edit-destination-button {
        position: absolute;
        top: 30px;
        right: 30px;
        visibility: hidden;
    }

    .destination-card #delete-destination-button {
        position: absolute;
        bottom: 30px;
        right: 30px;
        visibility: hidden;
    }

    .destination-card #save-destination-button {
        position: absolute;
        top: 30px;
        right: 30px;
        visibility: hidden;
    }

</style>