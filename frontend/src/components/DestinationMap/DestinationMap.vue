<template>
  <div id="map">
    <div id="destination-title" v-if="destinationTitle">

      <v-avatar> <img
          src="https://vuetifyjs.com/apple-touch-icon-180x180.png"
          alt="avatar"
          class="avatar"
        />
      </v-avatar>

      <h1>{{ destinationTitle }}</h1>



    </div>

    <GmapMap
      ref="map"
      :center="{lat:10, lng:10}"
      :zoom="7"
      map-type-id="roadmap"
      style="width: 100%; height: 100%"
      :options="{
        mapTypeControl: false,
        fullscreenControl: false
      }"
    >



      <GmapMarker
        :key="index"
        v-for="(marker, index) in mapDestinationsToMarkers(destinations)"
        :position="marker.position"
        :clickable="true"
        @click="toggleInfoWindow(marker, index)"
        :icon="marker.icon"
      />

      <GmapInfoWindow
        :options="infoOptions"
        :position="infoWindowPos"
        :opened="infoWindowOpen"
        @closeclick="infoWindowOpen=false"
      >
        <div v-if="infoContent">
          <h4
            class="destination-name"
            @click="$router.push(`/destinations/${infoContent.destinationId}`)"
          >
            {{ infoContent.destinationName }}
          </h4>
          Type: {{ infoContent.destinationType.destinationTypeName }}
          <br />
          {{ infoContent.destinationDistrict.districtName }}, {{ infoContent.destinationCountry.countryName }}
        </div>
      </GmapInfoWindow>

    </GmapMap>


    <v-card id="key">
      <h3>Key</h3>
      <div class="map-key">
      Public: <img style="margin-left: 5px;" :src="publicIcon" />
      </div>
      <div class="map-key">
      Private: <img :src="privateIcon" />
      </div>
    </v-card>

    <div v-if="shouldShowOverlay" id="overlay"></div>



  </div>
</template>

<script>
import { gmapApi } from "vue2-google-maps";

const publicIcon = "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";
const privateIcon = "http://maps.google.com/mapfiles/ms/icons/red-dot.png";

export default {
  data() {
    return {
      publicIcon,
      privateIcon,
      infoWindowPos: null,
      infoContent: null,
      currentOpenedIndex: null,
      infoWindowOpen: false,
      infoOptions: {
        pixelOffset: {
          width: 0,
          height: -35
        }
      }
    };
  },
  props: {
    destinations: {
      type: Array,
      required: true
    },
    destinationTitle: {
      type: String,
      required: false
    },
    shouldShowOverlay: {
      type: Boolean,
      required: false
    }
  },
  methods: {
    mapDestinationsToMarkers(destinations) {
      return destinations.map(destination => ({
        position: {
          lat: destination.destinationLat,
          lng: destination.destinationLon
        },
        icon: destination.isPublic ? publicIcon : privateIcon,
        destination
      }));
    },
    toggleInfoWindow(marker, index) {
      this.infoWindowPos = marker.position;
      this.infoContent = marker.destination;

      // Toggle info popup if on same index
      if (this.currentOpenedIndex === index) {
        this.infoWindowOpen = !this.infoWindowOpen;
      } else {
        this.infoWindowOpen = true;
        this.currentOpenedIndex = index;
      }
    }
  },
  watch: {
    destinations(newDestinations) {
      const markers = this.mapDestinationsToMarkers(newDestinations);
      this.$refs.map.$mapPromise.then(map => {
        const bounds = new window.google.maps.LatLngBounds();
        for (const marker of markers) {
          bounds.extend(marker.position);
        }

        map.fitBounds(bounds);
      });
    }
  }
};
</script>

<style lang="scss" scoped>
#map {
  width: 100%;
  height: 100%;
}
#key {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 10px;
}

.destination-name {
  cursor: pointer;
}

#destination-title {
  z-index: 1;
  position: absolute;
  margin-top: calc(30vh - 80px);
  margin-left: 5px;
  h1 {
    color: #fff;
    font-size: 3rem;
    display: inline-block;
    margin-left: 6px;
  }
 
}

.avatar {
  display: inline-block;
  margin-top: -20px;
}

.map-key {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px;
}

#overlay {
  top: 64px;
  background-color: rgba(0, 0, 0, 0.2);
  pointer-events: none;
  position: absolute;
  width: 100%;
  height: 30vh;
}
</style>


