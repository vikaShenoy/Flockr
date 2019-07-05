<template>
  <div id="map">
    <GmapMap
      ref="map"
      :center="{lat:10, lng:10}"
      :zoom="7"
      map-type-id="roadmap"
      style="width: 100%; height: 100%"
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
        </div>
      </GmapInfoWindow>

    </GmapMap>

    <v-card id="key">
      <h3>Key</h3>

      Public: <img :src="publicIcon" />
      <br />
      Private: <img :src="privateIcon" />
    </v-card>
  </div>
</template>

<script>
import { gmapApi } from "vue2-google-maps";

// Keep for later incase we want to customize colours
// const publicIcon = {
//   path:
//     "M27.648-41.399q0-3.816-2.7-6.516t-6.516-2.7-6.516 2.7-2.7 6.516 2.7 6.516 6.516 2.7 6.516-2.7 2.7-6.516zm9.216 0q0 3.924-1.188 6.444l-13.104 27.864q-.576 1.188-1.71 1.872t-2.43.684-2.43-.684-1.674-1.872l-13.14-27.864q-1.188-2.52-1.188-6.444 0-7.632 5.4-13.032t13.032-5.4 13.032 5.4 5.4 13.032z",
//   fillColor: "#4287f5",
//   fillOpacity: 1,
//   strokeWeight: 0,
//   scale: 0.65
// };

// const privateIcon = {
//   path:
//     "M27.648-41.399q0-3.816-2.7-6.516t-6.516-2.7-6.516 2.7-2.7 6.516 2.7 6.516 6.516 2.7 6.516-2.7 2.7-6.516zm9.216 0q0 3.924-1.188 6.444l-13.104 27.864q-.576 1.188-1.71 1.872t-2.43.684-2.43-.684-1.674-1.872l-13.14-27.864q-1.188-2.52-1.188-6.444 0-7.632 5.4-13.032t13.032-5.4 13.032 5.4 5.4 13.032z",
//   fillColor: "#7689a2",
//   fillOpacity: 1,
//   strokeWeight: 0,
//   scale: 0.65
// };

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
      },
    }
  },
  props: {
    destinations: {
      type: Array,
      required: true
    }
  },
  methods: {
    mapDestinationsToMarkers(destinations) {
      return destinations.map(destination => ({
        position: {
          lat: destination.destinationLat,
          lng: destination.destinationLon,
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
</style>


