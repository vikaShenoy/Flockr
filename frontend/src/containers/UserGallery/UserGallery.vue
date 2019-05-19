<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-card id="user-gallery">
      <v-card-title>
        <v-spacer align="center">
          <h1>Photo Gallery</h1>
        </v-spacer>
      </v-card-title>
      <v-responsive>
        <v-container grid-list fluid >
        <v-layout row wrap>
          <v-flex
            v-for="photo in photos"
            :key="photo.filename"
            d-flex
            lg3
            md4
            sm6
            xs12
            class="photo-tile"
            v-on:click="openPhotoDialog(photo)"
          >
            <v-img
              :src="getPhotoUrl(photo)"
              aspect-ratio="1"
              class="grey lighten-2"
              >
              <template v-slot:placeholder>
                <v-layout
                  fill-height
                  align-center
                  justify-center
                  >
                    <v-progress-circular indeterminate color="grey lighten-5"></v-progress-circular>
                </v-layout>
              </template>
            </v-img>
          </v-flex>

        </v-layout>

      </v-container>
      </v-responsive>
      <photo-panel
        :photo="currentPhoto"
        :showDialog="showPhotoDialog"
        v-on:closeDialog="closePhotoDialog"/>
    </v-card>
</template>

<script>

  import PhotoPanel from "./PhotoPanel/PhotoPanel";
  export default {

    name: "UserGallery",
    components: {PhotoPanel},
    data() {
      return {
        userId: null,
        photos: [],
        allPhotos: [],
        currentPhoto: {
          endpoint: null,
          isPrimary: false,
          isPublic: false
        },
        showPhotoDialog: false
      }
    },

    methods: {

      /**
       * Gets the url for this photo.
       *
       * @param photo the object containing the photo information
       * @return {string} the url of the endpoint containing the photo data
       */
      getPhotoUrl(photo) {
        // return endpoint(`/travellers/${this.userId}/photos/${photo.filename}/thumbnail`);
        return `https://picsum.photos/500/300?image=55`;
      },

      /**
       * Closes the photo dialog
       */
      closePhotoDialog() {
        this.showPhotoDialog = false;
        this.currentPhoto = {
          endpoint: null,
          isPrimary: false,
          isPublic: false
        };
      },

      /**
       * Sets the current photo to the one clicked on and opens the photo dialog
       *
       * @param photo the object containing the photo data
       */
      openPhotoDialog(photo) {
        this.currentPhoto = photo;
        this.showPhotoDialog = true;
      },

      /**
       * Called when scrolling reaches near the bottom of the page.
       * Adds another 12 photos to the photos list or adds the rest of the photos if there is less than 12 left.
       */
      getMorePhotos() {
        this.photos = this.photos.concat(this.allPhotos.slice(0, 12));
        this.allPhotos = this.allPhotos.slice(12);
      },

      /**
       * Sets the event listener for scrolling near the bottom of the page and adds more photos if available.
       */
      setScroll() {
        window.onscroll = () => {
          let atBottom = document.documentElement.scrollTop + window.innerHeight >= document.documentElement.offsetHeight * 0.8;

          if (atBottom) {
            this.getMorePhotos();
          }
        }
      },

      /**
       * Gets a list of photos for the user from the server.
       */
      getUsersPhotos() {

      }
    },

    mounted: function() {
      this.userId = this.$route.params.id;
      this.allPhotos = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40];
      this.getMorePhotos();
      this.setScroll();
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #user-gallery {
    h1{
      color: $primary;
    }
    width:100%;
    margin:1.5em;
  }

  .photo-tile {
    padding: 0.2em;
  }

  .photo-tile :hover {
    cursor: pointer;
  }

</style>