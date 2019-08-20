<template>
  <div id="map">
    <!--Conditional title that gets displayed at the bottom left of the map-->
    <div id="destination-title" v-if="destinationTitle">

      <v-avatar v-if="destinationPhotos.length"><img
              :src="getPhotoUrl(destinationPhotos[0].personalPhoto.photoId)"
              alt="avatar"
              class="avatar"
      />
      </v-avatar>

      <h1>{{ destinationTitle }}</h1>


    </div>

    <!--empty stylers object is used as it makes the google icon white-->
    <GmapMap
            ref="map"
            :center="{lat:10, lng:10}"
            :zoom="7"
            map-type-id="roadmap"
            style="width: 100%; height: 100%"
            @rightclick="pingMap"
            :options="{
        mapTypeControl: false,
        fullscreenControl: false,
        styles: [{
          stylers: [{}]
        }],
        fullscreenControl: false,
        minZoom: 2
      }"
    >

      <GmapMarker
        ref="marker"
        v-if="marker.length"
        :position="marker[0].position"
        :icon="markerOptions"
        :opacity="marker[0].opacity"
        :animation="marker[0].animation"
      />

      <GmapMarker
              :key="index"
              v-for="(marker, index) in mapDestinationsToMarkers(destinations)"
              :position="marker.position"
              :clickable="true"
              @click="toggleInfoWindow(marker, index)"
              :icon="marker.icon"
      />

      <div v-if="destinations.length && isTripMap">
        <GmapPolyline
                v-for="(marker, index) in mapDestinationsToMarkers(destinations).slice(0, destinations.length - 1)"
                v-bind:key="index"
                :path="[
            {
              lat: destinations[index].destinationLat,
              lng: destinations[index].destinationLon
            },
            {
              lat: destinations[index + 1].destinationLat,
              lng: destinations[index + 1].destinationLon
            }
          ]"

                :options="{
            strokeColor: '#4d80af',
            icons: [{
              icon: {
                path: forwardClosedArrow
              },
              offset: '100%'
            }]
          }"
        />
      </div>

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
          <br/>
          {{ infoContent.destinationDistrict.districtName }}, {{ infoContent.destinationCountry.countryName }}
        </div>
      </GmapInfoWindow>

    </GmapMap>


    <v-card id="key">
      <h3>Key</h3>
      <div class="map-key">
        Public: <img style="margin-left: 5px;" :src="publicIcon"/>
      </div>
      <div class="map-key">
        Private: <img :src="privateIcon"/>
      </div>
    </v-card>

    <div v-if="shouldShowOverlay" id="overlay"></div>


  </div>
</template>

<script>
  import {endpoint} from "../../utils/endpoint.js";
  import UserStore from "../../stores/UserStore";

  const publicIcon = "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";
  const privateIcon = "http://maps.google.com/mapfiles/ms/icons/red-dot.png";
  const pingIcon = "http://earth.google.com/images/kml-icons/track-directional/track-8.png";

  export default {
    data() {
      return {
        markerOptions: {
          url: pingIcon,
          size: {width: 40, height: 30},
          scaledSize: {width: 40, height: 30},
        },
        publicIcon,
        privateIcon,
        marker: [],
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
        forwardClosedArrow: 1
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
      },
      isTripMap: {
        type: Boolean,
        required: false
      },
      destinationPhotos: {
        type: Array,
        required: false
      },
      panOn: {
        type: Boolean,
        required: false
      }
    },
    mounted() {
      this.listenOnMessage();
    },
    methods: {
      /**
       * Gets the latitude and longitude of the clicked location
       */
      pingMap(event) {
        this.latitude = event.latLng.lat();
        this.longitude = event.latLng.lng();
        this.marker = [];
        this.marker.push({
          position: {
            lat: this.latitude,
            lng: this.longitude
          },
          icon: pingIcon,
          opacity: 1,
          animation: google.maps.Animation.BOUNCE
        });
        if (this.panOn) {
          this.$refs.map.panTo({lat: this.latitude, lng: this.longitude});
          this.$refs.map.panTo({lat: this.latitude, lng: this.longitude});
        }
        const data = {
          "type": "ping-map",
          "latitude": this.latitude,
          "longitude": this.longitude,
          "tripNodeId": this.$route.params.tripId
        };

        UserStore.data.socket.send(JSON.stringify(data));
      },
      /**
       * Listens websockets events to for map pings
       */
      listenOnMessage() {
        UserStore.data.socket.addEventListener("message", (event) => {
          const message = JSON.parse(event.data);
          if (message.type === "ping-map") {
            this.showPing(message);
          }
        })
        },
      /**
       * When the websocket receives a message, this function is called to display the ping
       */
      showPing(message) {
        this.latitude = message.latitude;
        this.longitude = message.longitude;
        this.marker = [];
        this.marker.push({
          position: {
            lat: this.latitude,
            lng: this.longitude
          },
          icon: pingIcon,
          opacity: 1,
          animation: google.maps.Animation.BOUNCE,
        });

        if (this.panOn) {
          this.$refs.map.panTo({lat: this.latitude, lng: this.longitude});
        }
      },
      /**
       * Transforms destinations to a format that the gmap api understands
       * @returns {Object} the transformed marker object
       */
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
      /**
       * Gets called when map marker has been clicked on
       */
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
      },
      getPhotoUrl(photoId) {
        return endpoint(`/users/photos/${photoId}?Authorization=${localStorage.getItem("authToken")}`);
      }
    },
    watch: {
      /**
       * Watches for changes of markers to recenter them
       */
      destinations(newDestinations) {
        const markers = this.mapDestinationsToMarkers(newDestinations);
        this.$refs.map.$mapPromise.then(map => {
          const bounds = new window.google.maps.LatLngBounds();
          for (const marker of markers) {
            bounds.extend(marker.position);
          }

          map.fitBounds(bounds);
          const zoomLevel = map.getZoom();
          map.setZoom(zoomLevel > 6 ? 6 : zoomLevel);
        });
      }
    }
  };
</script>

<style lang="scss" scoped>
  #map {
    width: 100%;
    height: 100%;
    position: relative;
  }

  #key {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 10px;

    .map-key {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 5px;
    }
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

  #overlay {
    top: 0px;
    background-color: rgba(0, 0, 0, 0.4);
    pointer-events: none;
    position: absolute;
    width: 100%;
    height: 30vh;
  }

</style>


