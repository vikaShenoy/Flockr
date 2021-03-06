<template>
  <v-card id="dialog-card">
    <img
            v-if="imageUrl"
            :src="imageUrl"
            id="preview-image"
    />

    <v-alert
            :value="true"
            type="error"
            id="upload-image-error"
            v-if="fileTypeError"
    >
      Please upload an image file (png or jpeg)
    </v-alert>


    <div
            id="uploader"
            @drop="onDrop"
            @dragover.prevent
            @click="uploadZoneClicked"
    >
      <v-icon id="upload-icon">cloud_upload</v-icon>

      <input
              type="file"
              style="display: none"
              ref=image
              accept="image/png,image/jpeg"
              @change="onImageChange"
      />

      <h3>Choose one file to upload </h3>
    </div>
    <div class="switch">
      <v-switch switch v-model="isPublic" label="Set to public" id="is-public-switch" color="secondary"></v-switch>
    </div>

    <v-btn id="upload-btn" :disabled="!uploadReady || imageUploading" :loading="imageUploading"
           color="secondary" @click="upload">Upload
    </v-btn>
  </v-card>
</template>

<script>
  import {uploadImage} from "./UploadPhotoService.js";

  export default {
    data() {
      return {
        imageFile: null,
        imageUrl: null,
        imageName: null,
        isPublic: false,
        // Denotes if the user has selected a file that isn't a png or jpeg
        fileTypeError: false,
        imageUploading: false
      };
    },
    methods: {
      /**
       * Gets called when user clicks in upload zone
       */
      uploadZoneClicked() {
        this.$refs.image.click();
      },
      /**
       * Gets triggered when user has selected a photo to upload
       */
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
      /**
       * Processes files to show preview and prepare for upload
       */
      processFiles(files) {
        this.fileTypeError = false;
        const mimeType = files[0].type;
        if (mimeType !== "image/png" && mimeType !== "image/jpeg") {
          this.fileTypeError = true;

          // Reset image
          this.imageUrl = null;
          this.imageFile = null;
          this.imageName = null;
          return;
        }

        this.imageName = files[0].name;
        const fileReader = new FileReader();

        fileReader.readAsDataURL(files[0]);
        fileReader.addEventListener("load", () => {
          this.imageUrl = fileReader.result;
          this.imageFile = files[0];
        });
      },
      /**
       * Uploads photo to backend
       */
      async upload() {
        const {
          imageFile,
          isPublic,
        } = this;

        const userId = this.$route.params.id;

        try {
          this.imageUploading = true;
          const image = await uploadImage(imageFile, isPublic, userId);

          // Reset values to initial state
          this.imageFile = null;
          this.imageUrl = null;
          this.imageName = null;
          this.isPublic = false;
          this.imageUploading = false;
          this.$emit("imageUploaded", image);
        } catch (e) {
          this.imageUploading = false;
          // Handle errors later
        }
      }
    },
    computed: {
      /**
       * Checks if upload is ready
       * @return {boolean} If the upload is ready or not
       */
      uploadReady() {
        return this.imageFile;
      }
    }
  };
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #dialog-card {
    padding: 30px 10px;
  }

  #uploader {
    margin-top: 10px !important;
    width: 80%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.2s linear;
    border: 3px dashed $primary;
    min-height: 200px;
    cursor: pointer;

    h3 {
      color: #636e72;
    }

    &:hover {
      background-color: #dfe6e9;
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

  .switch {
    width: 150px;
    margin: 0 auto;
  }


  #upload-btn {
    margin: 0 auto;
    display: block;
  }
</style>




