<template>
  <v-dialog width="80%" v-model="showPhotoDialog">
    <v-card>
      <img
          :src="getPhotoUrl(photo.photoId)"
          style="height:60vh"
          alt="Some Image"/>
      <v-card-actions>
        <v-btn flat color="error" @click="showPhotoDialog=false">
          Close
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
  export default {
    name: "destination-photo-panel",

    props: {
      photo: {
        type: Object,
        required: false
      },
      showDialog: {
        type: Boolean,
        required: true
      },
      index: {
        type: Number,
        required: false
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
      getPhotoUrl() {
        return "https://i2.wp.com/digital-photography-school.com/wp-content/uploads/2012/10/image1.jpg?fit=500%2C500&ssl=1";
      }
    }
  }
</script>

<style scoped>

</style>