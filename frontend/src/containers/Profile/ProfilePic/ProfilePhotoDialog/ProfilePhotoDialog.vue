<template>
  <div>  
    <v-dialog
      v-model="dialog"
      max-width="840px"
      
    >


    <v-card>
      
      <div v-if="selectedPhoto" id="selected-photo">
        <img
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
            id="upload-btn"
          >
          Set Profile Picture 
          <v-icon id="upload-icon">face</v-icon>
        </v-btn>
      </div>
        

      <ProfilePhotosSelection
        :photos="photos"
        :photoUrl="photoUrl"
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

export default {
  components: {
    ProfilePhotosSelection
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

#upload-icon {
  margin-left: 5px;
}

#upload-btn {
  width: 200px;
  align-self: center;
  margin-top: 10px;
}

</style>


