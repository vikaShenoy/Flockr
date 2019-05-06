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
    </v-card>
</template>

<script>
  import {endpoint} from "../../utils/endpoint";

  export default {

    name: "UserGallery",

    data() {
      return {
        userId: null,
        photos: [1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8]
      }
    },

    methods: {
      getPhotoUrl: function(photo) {
        // return endpoint(`/travellers/${this.userId}/photos/${photo.filename}/thumbnail`);
        return `https://picsum.photos/500/300?image=55`;
      }
    },

    mounted: function() {
      this.userId = this.$route.params.id;
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

</style>