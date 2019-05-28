<template>
  <div id="destination-photo" v-if="photo">
    <v-speed-dial
      v-model="fab"
      bottom
      right
      direction="top"
      :open-on-hover="false"
      transition="slide-y-reverse-transition"
      id="permissions-button"
    >
      <template v-slot:activator>
        <v-btn v-if="hasModifyRights" v-model="fab" color="blue darken-2" dark fab>
          <v-icon>{{photo.personalPhoto.isPublic ? 'public' : 'lock'}}</v-icon>
          <v-icon>close</v-icon>
        </v-btn>
      </template>
      <v-btn v-if="!photo.personalPhoto.isPublic" fab dark small @click="updatePhotoPermissions" color="green">
        <v-icon>public</v-icon>
      </v-btn>
      <v-btn v-if="photo.personalPhoto.isPublic" fab dark small @click="updatePhotoPermissions" color="indigo">
        <v-icon>lock</v-icon>
      </v-btn>
    </v-speed-dial>
    <v-img
            id="image"
            contain
            :src="photo.endpoint"
            aspect-ratio="1">
      <template v-slot:placeholder>
        <v-layout
                fill-height
                align-center
                justify-center>
          <v-progress-circular
                  indeterminate
                  color="grey lighten-5"/>
        </v-layout>
      </template>
    </v-img>
  </div>
</template>

<script>
  import {updatePhotoPermissions} from "../../containers/UserGallery/UserGalleryService";

  export default {
    props: {
      photo: {
        photoId: {
          type: Number,
          required: false
        },
        isPublic: {
          type: Boolean,
          required: false
        },
        endpoint: {
          type: String,
          required: false
        },
        thumbEndpoint: {
          type: String,
          required: false
        },
        ownerId: {
          type: Number,
          required: false
        }
      },
      hasModifyRights: {
        type: Boolean,
        required: false
      }
    },
    data() {
      return {
        fab: false
      }
    },
    methods: {
      /**
       * Called when the permission button is selected.
       * Updates the permission of a photo and emits the new value to be updated in the parent.
       */
      async updatePhotoPermissions() {
        try {
          await updatePhotoPermissions(!this.photo.personalPhoto.isPublic, this.photo.personalPhoto.photoId);
          this.$emit("permissionUpdated", !this.photo.personalPhoto.isPublic);
        } catch (error) {
          this.$emit("displayError", error.message);
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  #destination-photo {
    position: relative;

    #permissions-button {
      z-index: 1;
      position: absolute;
    }

    #image {
      height: 80vh;
      background-color: dimgrey
    }

  }
</style>