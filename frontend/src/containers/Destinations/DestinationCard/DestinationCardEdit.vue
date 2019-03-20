<template>
    <v-card elevation="10"
            class="destination-card row col-md-12">
        <div class="title-card col-md-3">
            <div xs12 sm6 md3 class="name-header">
                <v-text-field
                    label="Destination Name"
                    :value="destination.destinationName"
                    v-model="destination.destinationName">
                </v-text-field>
            </div>
            <div class="body-card col-md-12">
                <v-img class="image" src="https://picsum.photos/510/300?random"></v-img>
            </div>
        </div>
        <div class="col-md-5">
            <div class="row">
                <div class="basic-info-label"><p><b>Type</b></p></div>
                <div xs12 sm6 md3 class="basic-info">
                    <v-text-field
                            label="Destination Type"
                            :value="destination.destinationType"
                            v-model="destination.destinationType"
                    ></v-text-field>
                </div>
            </div>
            <hr class="divider"/>
            <div class="row">
                <div class="basic-info-label"><p><b>District</b></p></div>
                <div xs12 sm6 md3 class="basic-info">
                    <v-text-field
                            label="District"
                            :value="destination.district"
                            v-model="destination.district"
                    ></v-text-field>
                </div>
            </div>
            <hr class="divider"/>
        </div>
        <div class="col-md-4">
            <div xs12 sm6 md3 class="name-header">
                <v-text-field
                        label="Destination Country"
                        :value="destination.country"
                        v-model="destination.country">
                </v-text-field>
            </div>
            <div xs12 sm6 md3 class="name-header">
                <v-text-field
                        label="Destination Latitude"
                        :value="destination.latitude"
                        v-model="destination.latitude">
                </v-text-field>
            </div>
            <div xs12 sm6 md3 class="name-header">
                <v-text-field
                        label="Destination Longitude"
                        :value="destination.longitude"
                        v-model="destination.longitude">
                </v-text-field>
            </div>
            <!--<v-img class="image" src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>-->
        </div>
        <v-btn fab dark id="save-destination-button" @click="onClick">
            <v-icon dark>check_circle</v-icon>
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
      onClick: {
        type: Function,
        required: true
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
        margin: 10px 0 0;
        text-align: center;
        width: 50%;
    }

    .name-header {
        margin: 0 0 20px;
        text-align: center;
    }

    .destination-card {
        width: 100%;
        margin: 10px 0 0;
    }

    .destinations-panel :hover #save-destination-button {
        visibility: visible;
    }

    .destination-card #save-destination-button {
        position: absolute;
        top: 30px;
        right: 30px;
        visibility: hidden;
    }

</style>