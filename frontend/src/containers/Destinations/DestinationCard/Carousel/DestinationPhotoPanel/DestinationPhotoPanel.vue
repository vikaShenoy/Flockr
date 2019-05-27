<template>
  <v-dialog width="80%" v-model="showPhotoDialog">
    <v-card>
      <destination-photo :photo="photo" @displayError="displayError" @permissionUpdated="permissionUpdated"/>
      <v-card-actions>
        <v-btn flat color="error" @click="showPhotoDialog=false">
          Close
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  import DestinationPhoto from "../../../../../components/DestinationPhoto/DestinationPhoto";
  export default {
    name: "destination-photo-panel",
    components: {DestinationPhoto},
    props: {
      photo: {
        type: Object,
        required: false
      },
      showDialog: {
        type: Boolean,
        required: true
      }
    },
    data() {
      return {
        showPhotoDialog: false
      }
    },
    watch: {
      showDialog: {
        handler: 'onShowDialogUpdated',
        immediate: true
      },
      showPhotoDialog: {
        handler: 'onShowPhotoDialogUpdated',
        immediate: true
      }
    },
    methods: {
      /**
       * Called when showDialog is modified in the parent.
       * Updates the showPhotoDialog to match the prop showDialog
       */
      onShowDialogUpdated() {
        this.showPhotoDialog = this.showDialog;
      },
      /**
       * Called when the close button is clicked
       * Emits an event to the parent to close the dialog.
       */
      onShowPhotoDialogUpdated() {
        this.$emit('closeDialog', this.showPhotoDialog);
      },
      /**
       * Called when the destination photo emits an error to display.
       * Emits the given error to it's parent to display.
       *
       * @param message {String} the error message
       */
      displayError(message) {
        this.$emit("displayError", message);
      },
      /**
       * Called when the permission of a photo is updated.
       *
       * @param newValue {Boolean} the new value of isPublic in the photo
       */
      permissionUpdated(newValue) {
        this.$emit("permissionUpdated", newValue);
      }
    }
  }
</script>

<style scoped>

</style>