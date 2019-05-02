import { shallowMount } from "@vue/test-utils";
import Vue from "vue";
import Vuetify from 'vuetify';
Vue.use(Vuetify);
import App from "@/containers/App/App.vue";

describe("Component", () => {
  test("is a Vue instance", () => {
    const wrapper = shallowMount(App);
    expect(wrapper.isVueInstance()).toBeTruthy();
  });
});
