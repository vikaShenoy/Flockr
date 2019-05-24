import { mount } from "@vue/test-utils";
import UploadPhoto from "../../../src/components/UploadPhoto/UploadPhoto.vue";

describe("Uploading photos work", () => {
  test("When I set the public switch on, my photo should be set to public", () => {
    const wrapper = mount(UploadPhoto);
    const isPublicSwitch = wrapper.find("#is-public-switch");
    isPublicSwitch.trigger("click");
    expect(wrapper.vm.public).toBe(true);
  });

  test("When I set the primary switch on, my photo is set to primary", () => {
    const wrapper = mount(UploadPhoto);
    const isPrimarySwitch = wrapper.find("#is-primary-switch");
    isPrimarySwitch.trigger("click");
    expect(wrapper.vm.primary).toBe(true);
    const isPublicSwitch = wrapper.find("#is-public-switch");

    expect(isPublicSwitch.attributes("disabled")).toBe("disabled");
    expect(wrapper.vm.public).toBe(false);
  });

  test("When I set the public and primary switch on, my photo should be primary and not public", () => {
    const wrapper = mount(UploadPhoto);
    const isPrimarySwitch = wrapper.find("#is-primary-switch");
    const isPublicSwitch = wrapper.find("#is-public-switch");
    isPublicSwitch.trigger("click");
    isPrimarySwitch.trigger("click");

    expect(wrapper.vm.public).toBe(false);
    expect(wrapper.vm.primary).toBe(true);
    expect(isPublicSwitch.attributes("disabled")).toBe("disabled");
  });

  test("When the user drags a file that isn't an image, an error message should be displayed", () => {
    const wrapper = mount(UploadPhoto);
    wrapper.vm.fileTypeError = true;
    expect(wrapper.find("#upload-image-error").isVisible()).toBe(true);
  });
});


