<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-dialog
            id="AddPhotoDialog"
            v-model="dataDialog"
            width="80%">

        <template v-slot:activator="{ on }">
            <v-btn
                    color="secondary"
                    v-on="on"
                    outline
                    ><v-icon>add</v-icon></v-btn>
        </template>

        <v-card>
            Add the add new photo stuff here.
        </v-card>

    </v-dialog>
</template>

<script>
  export default {

    name: "add-photo-dialog",

    data() {
      return {
        dataDialog: false
      }
    },

    props: {
      dialog: {
        type: Boolean,
        required: true
      },
    },

    watch: {

      "dialog": {
        handler: "onDialogChanged",
        immediate: true
      }

    },

    methods: {

      /**
       * Called when the dataDialog variable is modified.
       * Emits an event called dialogChangedEvent with a parameter of the dataDialog variable
       */
      closeDialog: function () {
        this.$emit("closeDialog");
      },

      /**
       * Called when the dialog prop is changed.
       * Updates the dataDialog variable to match the dialog prop.
       */
      onDialogChanged: function () {
        this.dataDialog = this.dialog;
      }

    }
  }
</script>

<style scoped>

</style>