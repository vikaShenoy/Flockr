<template>
  <v-dialog
          v-model="dialog"
          width="80%"
  >
    <v-card>
      View photo stuff here.\n
      {{ photo }}
    </v-card>
  </v-dialog>
</template>

<script>

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
        isPrimary: {
          type: Boolean,
          required: true
        },
        isPublic: {
          type: Boolean,
          required: true
        }
      },
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
        dialog: false
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

      showDialogChanged() {
        this.dialog = this.showDialog;
      }
    }
  }
</script>

<style scoped>

</style>