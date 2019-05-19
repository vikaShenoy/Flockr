<template>
  <div>  
    <v-dialog
      v-model="dialog"
      max-width="840px"
    >
    <v-card>
      
      <div v-if="selectedPhoto" id="selected-photo">
        <img
          v-if="cropOption === 'auto'"
          :src="photoUrl(selectedPhoto.photoId)"
          id="profile-picture-preview"
          alt="Profile Picture"
        />

        <vue-cropper
          ref="cropper"
          :aspectRatio="1"
          v-if="cropOption === 'self'"
          :src="photoUrl(selectedPhoto.photoId)"
          id="profile-picture-preview"
          alt="Profile Picture"
        />




        <v-btn-toggle
          v-model="cropOption"
          color="secondary"
          id="crop-options"
        >
          <v-btn value="self">Self Crop</v-btn>
          <v-btn value="auto">Auto Crop</v-btn>
        </v-btn-toggle>
          <v-btn
            color="secondary"
            depressed
            id="set-profile-pic-btn"
            @click="saveProfilePicture"
          >
          Set Profile Picture 
          <v-icon id="profile-pic-icon">face</v-icon>
        </v-btn>
      </div>
        

      <ProfilePhotosSelection
        :photos="photos"
        :photoUrl="photoUrl"
        v-if="photos.length"
        v-on:photoSelected="photoSelected"
        :selectedPhoto="selectedPhoto"
      />
    </v-card>
    </v-dialog>
  </div>
</template>

<script>
import ProfilePhotosSelection from "./ProfilePhotosSelection/ProfilePhotosSelection";
import { endpoint } from "../../../../utils/endpoint";
import VueCropper from "vue-cropperjs";


export default {
  components: {
    ProfilePhotosSelection,
    VueCropper
  },
  data() {
    return {
      selectedPhoto: null,
      // Describe the type of cropping (either "self" or "auto")
      cropOption: "self"
    };
  },
  props:  {
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
      console.log(this.$refs.cropper.getCroppedConvas().toDataURL());
    },
    saveProfilePicture() {
      this.cropImage(); 
    }
  }
}
</script>

<style lang="scss" scoped>

#profile-picture-preview {
  max-height: 300px;
  max-width: 80%;
  align-self: center;
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


