<template>
  <v-dialog width="80%" v-model="showPhotoDialog">
    <v-card>
      <destination-photo
        :photo="photo"
        @displayError="displayError"
        @permissionUpdated="permissionUpdated"
        :hasModifyRights="hasPhotoModifyRights"
      />
      <v-card-actions>
        <v-spacer align="left">
          <v-btn flat color="error" @click="removePhoto">
            Remove This Photo From Destination
          </v-btn>
        </v-spacer>
        <v-spacer align="right">
          <v-btn v-if="hasPhotoModifyRights || hasOwnerRights" flat color="secondary" @click="showPhotoDialog=false">
            Close
          </v-btn>
        </v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  import DestinationPhoto from "../../../../../components/DestinationPhoto/DestinationPhoto";
  import UserStore from "../../../../../stores/UserStore";
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
      },
      hasOwnerRights: {
        type: Boolean,
        required: false
      }
    },
    data() {
      return {
        showPhotoDialog: false,
        hasPhotoModifyRights: false
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
      },
      photo: {
        handler: "onPhotoChanged",
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
      },
      /**
       * Called when the photos prop is set.
       * Calculates if the user has admin rights.
       */
      onPhotoChanged() {
        if (![null,undefined].includes(this.photo)) {
          this.hasPhotoModifyRights = localStorage.getItem("userId") === this.photo.personalPhoto.ownerId.toString() || UserStore.methods.isAdmin();
        }
      },
      /**
       * Called when the remove button is selected.
       * Removes this photos association with the destination.
       */
      removePhoto() {
        this.$emit("displayRemovePrompt", this.photo.photoId);
      }
    }
  }
</script>

<style scoped>

</style>