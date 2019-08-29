// NOTE: There should be one instance of this Snackbar in the app.
// This is so that it can listen for the events emitted by the Vue
// instance.
//
// (Any component under the App tree).vue
// this.$root.$emit('show-snackbar', {options...})
//
// GlobalSnackbar.vue
// this.$root.$on('show-snackbar', // handle this)

<template>
  <v-snackbar
    v-model="showSnackbar"
    :bottom="true"
    :left="false"
    :multi-line="false"
    :right="true"
    :timeout="timeout"
    :top="false"
    :vertical="false"
  >
    {{ message }}
    <v-btn
      :color="color"
      flat
      @click="showSnackbar = false"
    >
      Dismiss
    </v-btn>
  </v-snackbar>
</template>


<script>
  export default {
    data() {
      return {
        showSnackbar: false,
        timeout: 0,
        color: 'brown',
        message: 'Snackbar message goes here!',
        viewWindowTimeout: null
      }
    },
    methods: {
      handleShowSnackbar(event) {
        this.timeout = event.timeout;
        this.color = event.color;
        this.message = event.message;
        this.showSnackbar = true;
      },
      /**
       * Show the error snackbar for the specified amount of time
       * @param message the message to show
       * @param timeout for how long (in ms) we want to show the snackbar
       */
      handleShowErrorSnackbar(message, timeout) {
        this.handleShowSnackbar({
          message: message,
          timeout: timeout,
          color: 'error'
        });
      },
      /**
       * Show the success snackbar for the specified amount of time
       * @param message the message to show
       * @param timeout for how long (in ms) we want to show the snackbar
       */
      handleShowSuccessSnackbar(message, timeout) {
        this.handleShowSnackbar({
          message: message,
          timeout: timeout,
          color: 'success'
        });
      }
    },
    mounted() {
      this.$root.$on('show-snackbar', this.handleShowSnackbar);
      this.$root.$on('show-error-snackbar', this.handleShowErrorSnackbar);
      this.$root.$on('show-success-snackbar', this.handleShowSuccessSnackbar);
    }
  }
</script>
