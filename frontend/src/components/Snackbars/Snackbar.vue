// NOTE: The snackbar will hide itself after its timeout is over but it will not emit a dismissSnackbar event.

<template>
  <v-snackbar
          v-model="snackbarModel.show"
          :bottom="true"
          :left="false"
          :multi-line="false"
          :right="true"
          :timeout="snackbarModel.timeout"
          :top="false"
          :vertical="false"
  >
    {{ snackbarModel.text }}
    <v-btn
            :color="snackbarModel.color"
            flat
            @click="$emit('dismissSnackbar', snackbarModel.snackbarId)"
    >
      Dismiss
    </v-btn>
  </v-snackbar>
</template>


<script>
  export default {
    props: {
      snackbarModel: {
        show: Boolean,
        timeout: Number,
        text: String,
        color: String,
        snackbarId: Number
      }
    }
  }
</script>
