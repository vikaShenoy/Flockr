<template>
    <v-card
            class="destination-card row col-md-12">
        <div class="title-card col-md-3">
            <h2 class="name-header">{{ destination.name }}</h2>
            <div class="body-card col-md-12">
                <v-img src="https://picsum.photos/510/300?random"></v-img>
            </div>
        </div>
        <div class="col-md-5">
            <div class="row">
                <div class="basic-info"><p><b>Type</b></p></div><div class="basic-info">{{ destination.destType }}</div>
            </div>
            <hr class="divider"/>
            <div class="row">
                <div class="basic-info"><p><b>District</b></p></div><div class="basic-info">{{ destination.district }}</div>
            </div>
            <hr class="divider"/>
        </div>
        <div class="col-md-4">
            <h2 class="name-header">{{ destination.country }}</h2>
            <v-img src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </div>
        <v-btn fab dark class="edit-button" id="edit-destination-button">
            <v-icon dark>edit</v-icon>
        </v-btn>
        <v-btn fab dark class="delete-button" id="delete-destination-button">
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
        name: {
          type: String,
          required: true
        },
        destType: {
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
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
    @import "../../../styles/_variables.scss";

    .divider {
        margin: 0 0 20px;
    }

    .basic-info {
        text-align: center;
        width: 50%;
    }

    .name-header {
        margin: 0 0 20px;
        text-align: center;
    }

    .destination-card {
        width: 100%;
        display: flex;
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

</style>