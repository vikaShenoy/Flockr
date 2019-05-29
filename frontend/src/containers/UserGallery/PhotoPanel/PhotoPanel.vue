<template>
  <v-dialog
          v-model="dialog"
          width="80%"
          v-if="photo"
  >
    <v-card>
      <v-responsive>
        <!-- Weird bug that only accepts this format -->
        <v-layout
                v-if="photo.endpoint"
                grid-list
                class="photo-panel">
          <v-img
                  :src="`${ photo.endpoint }`"
                  lazy-src="src/containers/Profile/Photos/beach-holiday.jpg"
                  class="grey darken-3"
                  :contain="true"/>
        </v-layout>
      </v-responsive>
      <v-card-actions>
        <v-spacer align="left">
          <v-btn
                  v-if="showModify"
                  flat
                  color="error"
                  @click="deletePhoto">
            Delete
          </v-btn>
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
        required: false,
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
          required: false
        }
      },
      currentPhotoIndex: Number,
      showDialog: {
        type: Boolean,
        required: true
      },
      deleteFunction: {
        type: Function,
        required: false
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
        if (this.photo.deleteFunction) {
          this.$emit("deletePhoto", this.photo.deleteFunction);
        } else {
          this.$emit("deletePhoto", this.deleteFunction);
        }
      },
      /**
       * Called when the dialog variable changes state.
       * If the photo has been set to an object, updates other fields.
       */
      showDialogChanged() {
        if (this.photo) {
          this.showModify = (localStorage.getItem("userId") === this.$route.params.id || UserStore.methods.isAdmin());
          this.isPublicData = this.photo.public;
          this.dialog = this.showDialog;
        }
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