<template>
  <div>
    <v-dialog
      v-model="imageDialog"
      max-width="800px"
    >
    <UploadPhoto v-on:imageUploaded="closeDialog" />
   </v-dialog>

    <v-btn
      color="secondary"
      @click="showImageDialog"
      outline
    >
      <v-icon>add</v-icon>
    </v-btn>
  </div>
</template>

<script>
import UploadPhoto from "../../../../components/UploadPhoto/UploadPhoto";

export default {
  components: {
    UploadPhoto
  },
  name: "add-photo-dialog",
  data() {
    return {
      imageDialog: false,
    };
  },

  props: {
    dialog: {
      type: Boolean,
      required: true
    }
  },
  watch: {
    dialog: {
      handler: "onDialogChanged",
      immediate: true
    }
  },

  methods: {
   closeDialog: function() {
      this.imageDialog = false;
    },
    showImageDialog() {
      this.imageDialog = true;
    },
    /**
     * Called when the dialog prop is changed.
     * Updates the dataDialog variable to match the dialog prop.
     */
    onDialogChanged: function() {
      this.dataDialog = this.dialog;
    }
  }
};
</script>

<style scoped>
</style>