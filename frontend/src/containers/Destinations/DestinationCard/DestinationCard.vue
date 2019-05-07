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
                            :items="destinationTypes"
                            item-value="destinationTypeId"
                            item-text="destinationTypeName"
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
                            :items="districts"
                            item-value="districtId"
                            item-text="districtName"
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
                        :items="countries"
                        item-value="countryId"
                        item-text="countryName"
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
            <v-img v-else class="image"
                   src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </div>
        <v-btn v-if="editMode" fab id="save-destination-button" @click="saveDestination">
            <v-icon>check_circle</v-icon>
        </v-btn>
        <v-btn v-else fab class="edit-button" id="edit-destination-button" @click="editDestination">
            <v-icon>edit</v-icon>
        </v-btn>
        <v-btn v-if="editMode" fab class="delete-button" id="cancel-destination-edit-button" @click="cancelOnClick">
            <v-icon>remove_circle</v-icon>
        </v-btn>
        <v-btn v-else fab class="delete-button" id="delete-destination-button" @click="deleteOnClick">
            <v-icon>remove</v-icon>
        </v-btn>
    </v-card>
</template>

<script>

  import {sendUpdateDestination, sendAddDestination, requestDistricts} from "../DestinationsService.js";

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
        type: Array,
        required: true
      },
      destinationTypes: {
        type: Array,
        required: true
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
      districtName() {
        return this.destination.destinationDistrict.districtName;
      },
      destinationId() {
        return this.destination.destinationId;
      }
    },

    watch: {

      countryName() {
        this.onCountryChanged();
      },

      districtName() {
        this.onDistrictChanged();
      },

      destinationId() {
        this.onDestinationIdChanged();
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
        districts: [],
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
        dataEditMode: true,
        oldDestinationData: {
          id: null,
          name: null,
          countryId: null,
          districtId: null,
          latitude: null,
          longitude: null,
          type: null
        }
      }
    },

    methods: {
      saveDestination: async function () {
        await this.validateAll();
        if (!this.hasInvalidInput) {
          let destinationInfo = {
            "destinationName": this.destination.destinationName,
            "destinationTypeId": this.destination.destinationType.destinationTypeId,
            "countryId": this.destination.destinationCountry.countryId,
            "districtId": this.destination.destinationDistrict.districtId,
            "latitude": this.destination.destinationLat,
            "longitude": this.destination.destinationLon,
          };
          // If the destination is new
          if (this.destination.destinationId === null) {
            try {
              let newDestination = await sendAddDestination(destinationInfo);
              this.destination.destinationId = newDestination.destinationId;
              this.dataEditMode = !this.dataEditMode;
            } catch (error) {
              console.log(error);
            }
          } else {
            try {
              await sendUpdateDestination(destinationInfo, this.destination.destinationId);
              this.dataEditMode = !this.dataEditMode;
            } catch (error) {
              console.log(error);
            }
          }
        }
      },

      editDestination: async function () {
        this.oldDestinationData.id = this.destination.destinationId;
        this.oldDestinationData.name = this.destination.destinationName;
        this.oldDestinationData.type = this.destination.destinationType.destinationTypeId;
        this.oldDestinationData.district = this.destination.destinationDistrict.districtId;
        this.oldDestinationData.country = this.destination.destinationCountry.countryId;
        this.oldDestinationData.latitude = this.destination.destinationLat;
        this.oldDestinationData.longitude = this.destination.destinationLon;
        this.dataEditMode = !this.dataEditMode;
      },

      isValidLatitude: function (event) {
        let currentValue = event.target.value + event.key;

        // Allow the keystroke if the value is the negative sign
        if (currentValue === '-') {
          return;
        }
        // Prevent the keystroke if the value would be out of a valid latitude range or not numeric or -0 or 0 followed by any other number
        if (isNaN(currentValue) || parseFloat(currentValue) > 90 || parseFloat(currentValue) < -90 || currentValue === '-0' || (currentValue.charAt(0) === '0') && currentValue.length > 1) {
          event.preventDefault();
        }
      },

      isValidLongitude: function (event) {
        let currentValue = event.target.value + event.key;

        // Allow the keystroke if the value is the negative sign
        if (currentValue === '-') {
          return;
        }
        // Prevent the keystroke if the value would be out of a valid latitude range or not numeric or -0 or 0 followed by any other number
        if (isNaN(currentValue) || parseFloat(currentValue) > 180 || parseFloat(currentValue) < -180 || currentValue === '-0' || (currentValue.charAt(0) === '0') && currentValue.length > 1) {
          event.preventDefault();
        }
      },

      validateName: function () {
        if (this.destination.destinationName === "") {
          this.nameErrors = ["Name is required"];
          this.hasInvalidName = true;
        } else {
          this.nameErrors = [];
          this.hasInvalidName = false;
        }
      },

      validateType: function () {
        if (this.destination.destinationType.destinationTypeName === null) {
          this.typeErrors = ["Type is required"];
          this.hasInvalidType = true;
        } else {
          this.typeErrors = [];
          this.hasInvalidType = false;
        }
      },

      validateDistrict: function () {
        if (this.destination.destinationDistrict.districtName === null) {
          this.districtErrors = ["District is required"];
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

      onEditModeChanged(newValue) {
        this.dataEditMode = newValue;
      },

      onDataEditModeChanged(newValue) {
        if (this.$el.index) {
          this.$emit('editModeChanged', newValue, this.$el)
        }
      },

      async onCountryChanged() {
        this.destination.destinationDistrict.districtName = null;
        this.destination.destinationDistrict.districtId = null;
        if (this.destination.destinationCountry.countryName !== null) {
          try {
            this.districts = await requestDistricts(this.destination.destinationCountry.countryId);
            this.districtDisabled = false;
          } catch (error) {
            console.log(error);
          }
        } else {
          this.districtDisabled = true;
        }
      },

      onDestinationIdChanged() {
        if (this.$el) {
          this.$emit('idChanged', this.destination.destinationId, this.$el);
        }
      },

      onDistrictChanged() {
        let districtIndex = this.districts.names.indexOf(this.destination.destinationDistrict.districtName);
        this.destination.destinationDistrict.districtId = this.districts.ids[districtIndex];
      },

      validityCheck: function () {
        // Invalid input is true if any of the individual inputs are true
        this.hasInvalidInput = this.hasInvalidName || this.hasInvalidType || this.hasInvalidDistrict ||
            this.hasInvalidCountry || this.hasInvalidLatitude || this.hasInvalidlongitude;
      },
      cancelOnClick: function () {
        if (this.destination.destinationId === null) {
          // delete it
          this.$emit('deleteNewDestination', this.$el);
        } else {
          // restore old values
          this.destination.destinationId = this.oldDestinationData.id;
          this.destination.destinationName = this.oldDestinationData.name;
          this.destination.destinationType.destinationTypeId = this.oldDestinationData.typeId;
          this.destination.destinationDistrict.districtId = this.oldDestinationData.districtId;
          this.destination.destinationCountry.countryId = this.oldDestinationData.countryId;
          this.destination.destinationLat = this.oldDestinationData.latitude;
          this.destination.destinationLon = this.oldDestinationData.longitude;
          // disable edit mode
          this.dataEditMode = !this.dataEditMode;
        }
      }
    },

    mounted: async function () {
      if (this.destination.destinationCountry.countryName !== null) {
        try {
          this.districts = await requestDistricts(this.destination.destinationCountry.countryId);
          this.districtDisabled = false;
        } catch (error) {
          console.log(error);
        }
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