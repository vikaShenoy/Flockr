<template>
  <div> 
    <Snackbar
      :snackbarModel="snackbarModel"
      v-on:dismissSnackbar="dissmissSnackbar"
      />     
  </div>
</template>

<script>
import Snackbar from "./Snackbar";
export default {
  data() {
    return {
      snackbarModel: {
        show: false,
        timeout: 3000,
        text: "",
        color: "error",
        snackbarId: 1
      }
    }; 
  },
  methods: {
    showError(errorMessage) {
      this.snackbarModel.text = errorMessage;
      this.snackbarModel.show = true; 
    },
    dissmissSnackbar() {
      this.snackbarModel.show = false;
    }
  },
  components: {
    Snackbar
  }
}
</script>

