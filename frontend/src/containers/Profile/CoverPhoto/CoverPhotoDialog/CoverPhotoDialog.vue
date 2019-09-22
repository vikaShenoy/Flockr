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
              aspect-ratio="2.75"/>
        </v-layout>
        <profile-photos-selection
            :photos="photos"
            :photoUrl="coverPhotoEndpoint"
            v-on:photoSelected="photoSelected"
            :selectedPhoto="selectedPhoto"/>
      </v-responsive>

      <v-card-actions>
        <v-spacer align="left">
          <v-btn v-if="photo" flat color="error" @click="$emit('deleteCoverPhoto')">Delete</v-btn>
        </v-spacer>
        <v-spacer align="right">
          <v-btn flat color="secondary" v-on:click="submitNewCoverPhoto">Submit</v-btn>

          <v-btn flat color="secondary" v-on:click="$emit('closeDialog')">Cancel</v-btn>
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  import {endpoint} from "../../../../utils/endpoint";
  import ProfilePhotosSelection
    from "../../ProfilePic/ProfilePhotoDialog/ProfilePhotosSelection/ProfilePhotosSelection";

  export default {
    name: "CoverPhotoDialog",
    components: {ProfilePhotosSelection},
    props: {
      userId: Number,
      dialog: {
        type: Boolean,
        required: true
      },
      photo: Object,
      photos: Array
    },
    data() {
      return {
        selectedPhoto: null
      }
    },
    computed: {
      /**
       * Gets the cover photo endpoint for the cover photo.
       *
       * @return {string} the filename of the cover photo (null if none)
       */
      coverPhotoEndpoint() {
        if (this.selectedPhoto) {
          return endpoint(
              `/users/photos/${this.selectedPhoto.photoId}?Authorization=${localStorage.getItem(
                  "authToken")}`);
        } else {
          return this.photo ? endpoint(
              `/users/photos/${this.photo.photoId}?Authorization=${localStorage.getItem(
                  "authToken")}`) : endpoint(
              `/photos/cover/default?Authorization=${localStorage.getItem(
                  "authToken")}`);
        }
      }
    },
    methods: {
      /**
       * Re-emits the select cover photo event.
       *
       * @param newCoverPhoto the new cover photo.
       */
      photoSelected(newCoverPhoto) {
        this.selectedPhoto = newCoverPhoto;
      },
      /**
       * Emits an event to change the cover photo.
       */
      submitNewCoverPhoto() {
        this.$emit('photoSelected', this.selectedPhoto);
      }
    }
  }
</script>

<style scoped>

</style>