import Vue from "vue";
import VueRouter from "vue-router";
import router from "./routes";
import "./plugins/vuetify";
import App from "./containers/App/App.vue";
import "bootstrap/dist/css/bootstrap-grid.min.css";
import * as VueGoogleMaps from "vue2-google-maps";
import config from "./config";
require ('dotenv').config();
Vue.use(VueRouter);
Vue.use(VueGoogleMaps, {
  load: {
    // key: config.GOOGLE_MAPS_KEY,
    libraries: "places"
  }
});

Vue.config.productionTip = false;

/**
 * Save a reference to the Vue instance so that it's accessible from any
 * child component. Used by child components that want to show something
 * in a Snackbar with minimum overhead and code duplication.
 */
window.vue = new Vue({
  el: "#app",
  router,
  render: h => h(App),
}).$mount("#app");
