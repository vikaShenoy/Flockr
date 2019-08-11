// NOTE: There should be one instance of this Snackbar in the app.
// This is so that it can listen for the events emitted by the Vue
// instance.
//
// main.js
// window.vue = new Vue({...});
//
// (Any component under the App tree).vue
// window.vue.$emit('show-snackbar', {options...})
//
// GlobalSnackbar.vue
// window.$on('show-snackbar', // handle this)

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
      }
    },
    mounted() {
      if (window.vue) {
        window.vue.$on('show-snackbar', this.handleShowSnackbar);
      } else {
        this.vueInWindowTimeout = setTimeout(function() {
            if (window.vue) {
              window.vue.$on('show-snackbar', this.handleShowSnackbar);
              clearInterval(this.vueInWindowTimeout);
            } else {
              console.log('Did not find vue in the Window object, retrying soon to attach snackbar event listener');
            }
          }, 2000);
      }
    }
  }
</script>
