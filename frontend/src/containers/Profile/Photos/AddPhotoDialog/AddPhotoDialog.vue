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
      v-if="hasPermissionToView()"
    >
      <v-icon>add</v-icon>
    </v-btn>
  </div>
</template>

<script>
import UploadPhoto from "../../../../components/UploadPhoto/UploadPhoto";
import UserStore from "../../../../stores/UserStore";

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
    /**
     * Called when image is created. Saves image to users photos
     */
   closeDialog: function(image) {
      this.imageDialog = false;
      this.$emit("addImage", image);
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
    },
    /**
     * Checks that the user has permission to see the add photo button
     * @returns {boolean} If the user has permission or not
     */
    hasPermissionToView() {
      const userIdFromUrl = this.$route.params.id;
      return UserStore.methods.hasPermission(userIdFromUrl);
    }
  }
};
</script>

<style scoped>
</style>