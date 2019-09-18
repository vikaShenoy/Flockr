<template>
  <v-dialog
      v-model="dialog"
      max-width="840px"
      persistent
  >
    <v-card>
      <v-responsive>
        <v-layout
            grid-list
            class="photo-panel">
          <v-img
              :src="coverPhotoEndpoint"
              class="grey darken-3"
              :contain="true"/>
        </v-layout>
      </v-responsive>

      <v-card-actions>
        <v-spacer align="left">
          <v-btn v-if="photo" flat color="error" @click="$emit('deleteCoverPhoto')">Delete</v-btn>
        </v-spacer>
        <v-spacer align="right">
          <v-btn flat color="secondary" v-on:click="$emit('closeDialog')">Cancel</v-btn>
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  import {endpoint} from "../../../../utils/endpoint";

  export default {
    name: "CoverPhotoDialog",
    props: {
      dialog: {
        type: Boolean,
        required: true
      },
      photo: Object
    },
    computed: {
      /**
       * Gets the cover photo endpoint for the cover photo.
       *
       * @return {string} the filename of the cover photo (null if none)
       */
      coverPhotoEndpoint() {
        return this.photo ? endpoint(
            `/users/photos/${this.userProfile.coverPhoto.photoId}?Authorization=${localStorage.getItem(
                "authToken")}`) : endpoint(
            `/photos/cover/default?Authorization=${localStorage.getItem(
                "authToken")}`);
      }
    }
  }
</script>

<style scoped>

</style>