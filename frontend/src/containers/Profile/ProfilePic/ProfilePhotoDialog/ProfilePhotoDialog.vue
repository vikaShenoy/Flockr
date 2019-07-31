<template>
  <div>
    <v-dialog
            v-model="dialog"
            max-width="840px"
            persistent
    >
      <v-card>

        <div v-if="selectedPhoto" id="selected-photo">
          <img
                  v-if="cropOption === 'auto'"
                  :src="photoUrl(selectedPhoto.photoId)"
                  class="profile-picture-preview"
                  alt="Profile Picture"
          />
          <!--key is used to make sure that if the photoID updates, the component updates-->
          <vue-cropper
                  ref="cropper"
                  :aspectRatio="1"
                  v-if="cropOption === 'self'"
                  :src="photoUrl(selectedPhoto.photoId)"
                  class="profile-picture-preview"
                  alt="Profile Picture"
                  :key="selectedPhoto.photoId"
          />

          <!--Cropper for auto crop that doesn't actually show but crops the image in the background-->
          <vue-cropper
                  ref="autoCropper"
                  :aspectRatio="1"
                  v-if="cropOption === 'auto'"
                  :src="photoUrl(selectedPhoto.photoId)"
                  id="auto-cropper"
                  alt="Profile Picture"
          />

          <v-btn-toggle
                  v-model="cropOption"
                  color="secondary"
                  id="crop-options"
                  mandatory
          >
            <v-btn value="self">Self Crop</v-btn>
            <v-btn value="auto">Auto Crop</v-btn>
          </v-btn-toggle>
          <v-btn
                  color="secondary"
                  depressed
                  id="set-profile-pic-btn"
                  @click="saveProfilePicture"
                  :disabled="uploadingProfilePic"
                  :loading="uploadingProfilePic"
          >
            Set Profile Picture
            <v-icon id="profile-pic-icon">face</v-icon>
          </v-btn>
        </div>


        <ProfilePhotosSelection
                :photos="photos"
                :photoUrl="photoUrl"
                v-on:photoSelected="photoSelected"
                :selectedPhoto="selectedPhoto"
        />


        <v-card-actions>
          <v-spacer align="right">
            <v-btn flat color="secondary" v-on:click="$emit('closeDialog')">Cancel</v-btn>
          </v-spacer>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
  import ProfilePhotosSelection from "./ProfilePhotosSelection/ProfilePhotosSelection";
  import {endpoint} from "../../../../utils/endpoint";
  import VueCropper from "vue-cropperjs";
  import {sendProfilePicture} from "./ProfilePhotoDialogService";


  export default {
    components: {
      ProfilePhotosSelection,
      VueCropper
    },
    data() {
      return {
        selectedPhoto: null,
        uploadingProfilePic: false,
        // Describe the type of cropping (either "self" or "auto")
        cropOption: "self"
      };
    },
    props: {
      dialog: {
        type: Boolean,
        required: true
      },
      photos: {
        type: Array,
        required: true
      }
    },
    methods: {
      photoSelected(photo) {
        this.selectedPhoto = photo;
      },
      photoUrl(photoId) {
        const authToken = localStorage.getItem("authToken");
        const queryAuthorization = `?Authorization=${authToken}`;
        return endpoint(`/users/photos/${photoId}${queryAuthorization}`);
      },
      cropImage() {
        const cropper = this.cropOption === "self" ? this.$refs.cropper : this.$refs.autoCropper;
        cropper.getCroppedCanvas().toBlob(async blob => {
          try {
            this.uploadingProfilePic = true;
            const profilePhoto = await sendProfilePicture(this.$route.params.id, blob);
            this.uploadingProfilePic = false;
            this.$emit("closeDialog", profilePhoto);
            this.selectedPhoto = null;
          } catch (error) {
            this.uploadingProfilePic = false;
            this.$emit("showError", "Could not upload profile picture");
          }
        });
      },
      saveProfilePicture() {
        this.cropImage()
      }
    }
  }
</script>

<style lang="scss" scoped>

  .profile-picture-preview {
    max-height: 300px;
    max-width: 80%;
    align-self: center;
  }

  #auto-cropper {
    display: none;
  }

  #selected-photo {
    padding-top: 20px;
    display: flex;
    flex-direction: column;
  }

  #crop-options {
    margin-top: 10px !important;
    margin: 0 auto;
    align-self: center;
  }

  #profile-pic-icon {
    margin-left: 5px;
  }

  #set-profile-pic-btn {
    width: 200px;
    align-self: center;
    margin-top: 10px;
  }

</style>


