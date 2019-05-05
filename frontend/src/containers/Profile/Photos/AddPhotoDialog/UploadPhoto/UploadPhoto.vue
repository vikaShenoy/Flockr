<template>
  <v-card id="dialog-card">

    <img
      v-if="imageUrl"
      :src="imageUrl"
      id="preview-image"
    />

    

    <div
      id="uploader"
      @drop="onDrop"
      @dragover.prevent
      @click="uploadClicked"
    >
      <v-icon id="upload-icon">cloud_upload</v-icon>

      <input
        type="file"
        style="display: none"
        ref=image
        accept="image/png,image/jpeg"
        @change="onImageChange"
      />

      <h3>Choose a file or drag it here</h3>
    </div>

    <div id="permission-switch">
      <v-switch switch v-model="isPublic" :label="`Set to public`" color="secondary"></v-switch>
    </div>

    <v-btn id="upload-btn" color="secondary" @click="upload">Upload</v-btn>


  </v-card>
</template>

<script>
export default {
  data() {
    return {
      imageFile: null,
      imageUrl: null,
      imageName: null,
      isPublic: false
    };
  },
  methods: {
    onInputChange(event) {
      const files = event.target.files;
    },
    uploadClicked() {
      this.$refs.image.click();
    },
    onImageChange() {
      const files = event.target.files;
      this.processFiles(files);
    },
    /**
     * Event listener that gets called when user drops image on uploader
     */
    onDrop(event) {
      const files = event.dataTransfer.files;
      event.preventDefault();
      this.processFiles(files);
    },
    processFiles(files) {
      this.imageName = files[0].name;
      const fileReader = new FileReader();

      fileReader.readAsDataURL(files[0]);
      fileReader.addEventListener("load", () => {
        this.imageUrl = fileReader.result;
        this.imageFile = files[0];
        console.log(this.imageUrl);
        console.log(this.imageFile);
      });
    },
    upload() {
      
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../../../../styles/_variables.scss";

#dialog-card {
  padding-top: 30px;
  padding-bottom: 30px;
}

#uploader {
  margin-top: 10px !important;
  width: 80%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 3px dashed $primary;
  min-height: 200px;

  h3 {
    color: #636e72;
  }
}

#upload-icon {
  font-size: 100px;
}

#preview-image {
  max-width: 100%;
  max-height: 300px;
  margin: 0 auto;
  display: block;
}

#permission-switch {
  width: 140px;
  margin: 0 auto;
}

#upload-btn {
  margin: 0 auto;
  display: block;
}
</style>




