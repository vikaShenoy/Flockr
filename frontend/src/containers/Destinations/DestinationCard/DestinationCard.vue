<template>
    <v-card elevation="10"
            class="destination-card row col-md-12">
        <div class="title-card col-md-3">
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Name"
                        :value="destination.destinationName"
                        v-model="destination.destinationName">
                </v-text-field>
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
                    <v-text-field
                            label="Destination Type"
                            :value="destination.destinationType"
                            v-model="destination.destinationType"
                    ></v-text-field>
                </div>
                <div v-else class="basic-info-label">{{ destination.destinationType }}</div>
            </div>
            <hr class="divider"/>
            <div class="row">
                <div class="basic-info-label"><p><b>District</b></p></div>
                <div v-if="editMode" class="basic-info">
                    <v-text-field
                            label="District"
                            :value="destination.district"
                            v-model="destination.district"
                    ></v-text-field>
                </div>
                <div v-else class="basic-info-label">{{ destination.district }}</div>
            </div>
            <hr class="divider"/>
        </div>
        <div class="col-md-4">
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Country"
                        :value="destination.country"
                        v-model="destination.country">
                </v-text-field>
            </div>
            <h2 v-else class="name-header">{{ destination.country }}</h2>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Latitude"
                        :value="destination.latitude"
                        v-model="destination.latitude">
                </v-text-field>
            </div>
            <div v-if="editMode" class="name-header">
                <v-text-field
                        label="Destination Longitude"
                        :value="destination.longitude"
                        v-model="destination.longitude">
                </v-text-field>
            </div>
            <v-img v-else class="image" src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </div>
        <v-btn v-if="editMode" fab dark id="save-destination-button" @click="saveOnClick">
            <v-icon dark>check_circle</v-icon>
        </v-btn>
        <v-btn v-else fab dark class="edit-button" id="edit-destination-button" @click="editOnClick">
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
      saveOnClick: {
        type: Function,
        required: true
      },
      deleteOnClick: {
        type: Function,
        required: true
      },
      editOnClick: {
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