<template>
  <v-dialog
          id="ViewPhotoDialog"
          v-model="dataDialog"
          width="80%">

    <v-card>
      Add the view photo stuff here.
    </v-card>

  </v-dialog>
</template>

<script>
  import {endpoint} from "../../../../utils/endpoint";

  export default {

    name: "view-photo-dialog",

    data() {
      return {
        dataDialog: false
      }
    },

    props: {
      photo: {
        type: Object,
        required: false,
        filename: {
          type: String,
          required: true
        },
        primary: {
          type: Boolean,
          required: true
        }
      },
      dialog: {
        type: Boolean,
        required: true
      }
    },

    watch: {
      "dialog": {
        handler: "onDialogChanged",
        immediate: true
      },
      dataDialog: {
        handler: "closeDialog",
        immediate: true
      }
    },

    methods: {
      /**
       * Called when the dataDialog variable is modified.
       * Emits an event called dialogChangedEvent with a parameter of the dataDialog variable
       */
      closeDialog: function () {
        this.$emit("dialogChanged", this.dataDialog);
      },
      /**
       * Called when the dialog prop is changed.
       * Updates the dataDialog variable to match the dialog prop.
       */
      onDialogChanged: function () {
        this.dataDialog = this.dialog;
      },
      /**
       * Gets the photo endpoint to query for the given photo filename
       *
       * @param photoFilename the filename of the photo
       * @return String the url of photos the endpoint
       */
      getPhotoEndpoint: function (photoFilename) {
        return endpoint(`/travellers/photos/${photoFilename}`);
      }

    }
  }
</script>

<style scoped>

</style>