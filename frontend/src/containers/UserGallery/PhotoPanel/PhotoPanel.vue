<template>
  <v-dialog
          v-model="dialog"
          width="80%"
          v-if="photo.endpoint"
  >
    <v-card>
      <v-responsive>
        <!-- Weird bug that only accepts this format -->
        <v-layout grid-list class="photo-panel">
          <v-img
                  :src="`${photo.endpoint}`"
                  lazy-src="src/containers/Profile/Photos/beach-holiday.jpg"
                  class="grey darken-3"
                  :contain="true"/>
          <img :src="photo.endpoint" />
        </v-layout>
      </v-responsive>
      <v-card-actions>
        <v-spacer align="left">
          <v-btn flat color="error" @click="deletePhoto">Delete</v-btn>
        </v-spacer>
        <v-spacer align="center">
          <v-switch
                  v-if="showModify"
                  prepend-icon="lock"
                  v-model="isPublicData"
                  :label="`Photo is Public: ${isPublicData}`"
                  :false-value="false"
                  v-on:change="updatePermission"></v-switch>
        </v-spacer>
        <v-spacer align="right">
          <v-btn flat color="success" @click="dialog=false">Close</v-btn>
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>

  import UserStore from "../../../stores/UserStore";
  import {updatePhotoPermissions} from "../UserGalleryService";

  export default {

    name: "photo-panel",

    props: {
      photo: {
        type: Object,
        required: true,
        endpoint: {
          type: String,
          required: false
        },
        public: {
          type: Boolean,
          required: true
        },
        deleteFunction: {
          type: Function,
          immediate: true
        }
      },
      currentPhotoIndex: Number,
      showDialog: {
        type: Boolean,
        required: true
      }
    },

    watch: {
      showDialog: {
        handler: "showDialogChanged",
        immediate: true
      },
      dialog: {
        handler: "closeDialog",
        immediate: true
      }
    },

    data() {
      return {
        dialog: false,
        showModify: false,
        isPublicData: false
      }
    },

    methods: {
      /**
       * Called when the dialog is closed.
       * Emits a closeDialog event.
       */
      closeDialog() {
        this.$emit("closeDialog", this.dialog);
      },
      /**
       * Called when the delete button is selected, prompts the user to delete the photo.
       * Opens a delete prompt for the user to confirm and passes in the delete function.
       */
      deletePhoto() {
        this.$emit("deletePhoto", this.photo.deleteFunction);
      },
      /**
       * Called when the dialog variable changes state.
       */
      showDialogChanged() {
        this.showModify = (localStorage.getItem("userId") === this.$route.params.id || UserStore.methods.isAdmin());
        this.isPublicData = this.photo.public;
        this.dialog = this.showDialog;
      },
      /**
       * Called when the user chooses to update photo permissions.
       * Emits a changedPermission event with the new value.
       */
      async updatePermission() {
        try {
          await updatePhotoPermissions(this.isPublicData, this.photo.photoId);
          this.$emit("changedPermission", this.isPublicData, this.currentPhotoIndex);
        } catch (error) {
          this.$emit("displayErrorMessage", error.message);
        }
      }
    }
  }
</script>

<style lang="scss" scoped>

  .photo-panel {
    height: 80vh;
  }

</style>