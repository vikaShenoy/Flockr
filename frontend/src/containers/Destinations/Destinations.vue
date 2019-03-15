<template>
  <div style="width: 100%">
    <div id="page-title"><h1>Destinations</h1></div>
    <div id="destinations-card" class="destinations-panel">
      <v-card
              v-for="destination in destinations"
              v-bind:key="destination.id"
              class="destination-card row col-md-12">
        <v-card class="title-card col-md-3">
          <h2 class="name-header">{{ destination.name }}</h2>
          <v-card class="body-card col-md-12">
            <v-img src="https://picsum.photos/510/300?random"></v-img>
          </v-card>
        </v-card>
        <v-card class="col-md-5">
          <div class="row">
            <div class="basic-info"><p><b>Type</b></p></div><div class="basic-info">{{ destination.type }}</div>
          </div>
          <hr class="divider"/>
          <div class="row">
            <div class="basic-info"><p><b>District</b></p></div><div class="basic-info">{{ destination.district }}</div>
          </div>
          <hr class="divider"/>
        </v-card>
        <v-card class="col-md-4">
          <h2 class="name-header">{{ destination.country }}</h2>
          <v-img src="https://cdn.mapsinternational.co.uk/pub/media/catalog/product/cache/afad95d7734d2fa6d0a8ba78597182b7/w/o/world-wall-map-political-without-flags_wm00001_h.jpg"></v-img>
        </v-card>
        <v-btn fab dark class="edit-button" id="edit-destination-button">
          <v-icon dark>edit</v-icon>
        </v-btn>
        <v-btn fab dark class="delete-button" id="delete-destination-button">
          <v-icon dark>remove</v-icon>
        </v-btn>
      </v-card>
      <v-btn fab dark id="addDestinationButton">
        <v-icon dark>add</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
      destinations: [
        {
          "id": 15043,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15044,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        },
        {
          "id": 15045,
          "name": "1st Staircase",
          "type": "Place",
          "district": "South Auckland",
          "lat": -38.450361,
          "lon": 174.642722,
          "country": "New Zealand"
        }
      ]
    }
  },
  methods: {
    // Methods here
  }
}
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

#page-title {
  position: fixed;
  z-index: 1;
  width: 100%;
  padding: 15px;
  text-align: left;
  background: black;
  color: $primary;
  font-size: 30px;
  h1 {
    color: white;
  }
}

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

.title-card {
  display: inline-block;
}

.destinations-panel {
  padding: 150px 50px 50px 50px;
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

#destinations-card {
  padding: 150px 50px 50px;

}

#addDestinationButton {
  position: fixed;
  bottom: 30px;
  right: 30px;
  color: $secondary;
  .v-icon {
    color: $darker-white;
  }
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




