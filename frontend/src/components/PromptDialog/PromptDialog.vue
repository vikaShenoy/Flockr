<template>
  <v-dialog
          v-model="dialog"
          width="500"
          persistent
  >
    <v-card>
      <v-card-title class="warning headline">
        <v-spacer align="center">
          <h2 style="color: #FFF">Warning</h2>
        </v-spacer>
      </v-card-title>
      <v-card-text>
        <v-flex>
          <pre>{{ message }}</pre>
        </v-flex>
      </v-card-text>
      <v-responsive>
        <v-layout row>
          <v-spacer align="center">
            <v-btn color="green" flat v-on:click="sendResponse(true)" :loading="isLoading">Confirm</v-btn>
          </v-spacer>
          <v-spacer align="center">
            <v-btn color="red" flat v-on:click="sendResponse(false)">Cancel</v-btn>
          </v-spacer>
        </v-layout>
      </v-responsive>
    </v-card>
  </v-dialog>
</template>

<script>

  export default {

    name: "prompt-dialog",
    data() {
      return {
        isLoading: false
      };
    },
    props: {
      dialog: {
        type: Boolean,
        required: true
      },
      message: {
        type: String,
        required: true
      },
      onConfirm: {
        type: Function,
        required: false
      }
    },

    methods: {

      /**
       * Called when one of the buttons is selected.
       * Emits the appropriate response
       *
       * @param response {Boolean} true if confirm, false if cancel.
       */
      sendResponse: function (response) {
        if (response && this.onConfirm) {
          this.isLoading = true;
          this.onConfirm();
          this.isLoading = false;
        }
        this.$emit("promptEnded", response);
      }

    }
  }
</script>

<style lang="scss" scoped>

  pre {
    white-space: pre-wrap; /* Since CSS 2.1 */
    white-space: -moz-pre-wrap; /* Mozilla, since 1999 */
    white-space: -o-pre-wrap; /* Opera 7 */
    word-wrap: break-word; /* Internet Explorer 5.5+ */
    font-family: 'Roboto', sans-serif;
    font-size: 1em;
    text-align: center;
  }

</style>
