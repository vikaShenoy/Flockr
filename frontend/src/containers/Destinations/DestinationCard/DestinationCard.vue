<template>
    <v-card elevation="10"
            class="destination-card row col-md-12">
        <div class="title-card col-md-3">
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Name"
                        :value="destination.destName"
                        v-model="destination.destName"
                        @blur="validateName"
                        :error-messages="nameErrors"
                ></v-text-field>
            </div>
            <h2 v-else class="name-header">{{ destination.destName }}</h2>
            <div class="body-card col-md-12">
                <v-img class="image" src="https://picsum.photos/510/300?random"></v-img>
            </div>
        </div>
        <div class="col-md-5">
            <div class="row">
                <div class="basic-info-label"><p><b>Type</b></p></div>
                <div v-if="editMode" class="basic-info">
                    <v-text-field
                            label="Destination Type"
                            :value="destination.destType.destTypeName"
                            v-model="destination.destType.destTypeName"
                            @blur="validateType"
                            :error-messages="typeErrors"
                    ></v-text-field>
                </div>
                <div v-else class="basic-info-label">{{ destination.destType.destTypeName }}</div>
            </div>
            <hr class="divider"/>
            <div class="row">
                <div class="basic-info-label"><p><b>District</b></p></div>
                <div v-if="editMode" class="basic-info">
                    <v-text-field
                            label="District"
                            :value="destination.destDistrict.districtName"
                            v-model="destination.destDistrict.districtName"
                            @blur="validateDistrict"
                            :error-messages="districtErrors"
                    ></v-text-field>
                </div>
                <div v-else class="basic-info-label">{{ destination.destDistrict.districtName }}</div>
            </div>
            <hr class="divider"/>
        </div>
        <div class="col-md-4">
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Country"
                        :value="destination.destCountry.countryName"
                        v-model="destination.destCountry.countryName"
                        @blur="validateCountry"
                        :error-messages="countryErrors"
                ></v-text-field>
            </div>
            <h2 v-else class="name-header">{{ destination.destCountry.countryName }}</h2>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Latitude"
                        :value="destination.destLat"
                        v-model="destination.destLat"
                        v-on:keypress="isValidLatitude"
                        @blur="validateLatitude"
                        :error-messages="latitudeErrors"
                ></v-text-field>
            </div>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Longitude"
                        :value="destination.destLon"
                        v-model="destination.destLon"
                        v-on:keypress="isValidLongitude"
                        @blur="validateLongitude"
                        :error-messages="longitudeErrors"
                ></v-text-field>undefined
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
          type: String,
          required: true
        },
        district: {
          type: String,
          required: true
        },
        country: {
          type: String,
          required: true
        },
        latitude: {
          type: Number,
          required: true
        },
        longitude: {
          type: Number,
          required: true
        }
      },
      editMode: {
        type: Boolean,
        required: true
      },
      deleteOnClick: {
        type: Function,
        required: true
      }
    },

    watch: {

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
          this.dataEditMode = !this.dataEditMode;
          // If the destination is new
          if (this.destination.id === "") {
            // TODO: Send the new destination to the back-end and update the id in destinations when confirmed
          } else {
            // TODO: Send the modified destination to the back-end
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
        if (this.destination.destName === "") {
          this.nameErrors = [ "Name is required" ];
          this.hasInvalidName = true;
        } else {
          this.nameErrors = [];
          this.hasInvalidName = false;
        }
      },

      validateType: function () {
        if (this.destination.destType.destTypeName === "") {
          this.typeErrors = [ "Type is required" ];
          this.hasInvalidType = true;
        } else if (/\d/.test(this.destination.destType.destTypeName)) {
          this.typeErrors = [ "No numbers allowed" ];
          this.hasInvalidType = true;
        } else {
          this.typeErrors = [];
          this.hasInvalidType = false;
        }
      },

      validateDistrict: function () {
        if (this.destination.destDistrict.districtName === "") {
          this.districtErrors = [ "District is required" ];
          this.hasInvalidDistrict = true;
        } else {
          this.districtErrors = [];
          this.hasInvalidDistrict = false;
        }
      },

      validateCountry: function () {
        if (this.destination.destCountry.countryName === "") {
          this.countryErrors = ["Country is required"];
          this.hasInvalidCountry = true;
        } else if (/\d/.test(this.destination.destCountry.countryName)) {
          this.countryErrors = ["No numbers allowed"];
          this.hasInvalidCountry = true;
        } else {
          this.countryErrors = [];
          this.hasInvalidCountry = false;
        }
      },
      validateLatitude: function () {
        if (this.destination.destLat === "") {
          this.latitudeErrors = ["Latitude is required"];
          this.hasInvalidLatitude = true;
        } else {
          this.latitudeErrors = [];
          this.hasInvalidLatitude = false;
        }
      },

      validateLongitude: function () {
        if (this.destination.destLon === "") {
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

      validityCheck: function () {
        // Invalid input is true if any of the individual inputs are true
        this.hasInvalidInput = this.hasInvalidName || this.hasInvalidType || this.hasInvalidDistrict ||
            this.hasInvalidCountry || this.hasInvalidLatitude || this.hasInvalidlongitude;
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