import { mount } from "@vue/test-utils";
import UploadPhoto from "../../../src/components/UploadPhoto/UploadPhoto.vue";

describe("Uploading photos work", () => {
  test("When I set the public switch on, my photo should be set to public", () => {
    const wrapper = mount(UploadPhoto);
    const isPublicSwitch = wrapper.find("#is-public-switch");
    isPublicSwitch.trigger("click");
    expect(wrapper.vm.isPublic).toBe(true);
  });

  test("When the user drags a file that isn't an image, an error message should be displayed", () => {
    const wrapper = mount(UploadPhoto);
    wrapper.vm.fileTypeError = true;
    expect(wrapper.find("#upload-image-error").isVisible()).toBe(true);
  });
});


