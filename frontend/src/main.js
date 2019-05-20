import Vue from "vue";
import VueRouter from "vue-router";
import router from "./routes";
import "./plugins/vuetify";
import App from "./containers/App/App.vue";
import "bootstrap/dist/css/bootstrap-grid.min.css";
require ('dotenv').config();

Vue.use(VueRouter);

Vue.config.productionTip = false;

new Vue({
  el: "#app",
  router,
  render: h => h(App),
}).$mount("#app");
