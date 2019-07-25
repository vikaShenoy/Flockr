<template>
  <div
    id="root-container"
    v-if="userProfile"
  >
    <v-alert
      :value="shouldShowBanner()"
      color="info"
      icon="info"
    >
      Please fill in your full profile before using the site
    </v-alert>

    <div class="row">
      <div class="col-lg-4">
        <ProfilePic
          :profilePhoto="userProfile.profilePhoto"
          :photos="userProfile.personalPhotos"
          :userId="userProfile.userId"
          v-on:newProfilePic="newProfilePic"
          v-on:showError="showError"
        />

        <BasicInfo :userProfile.sync="userProfile" />

        <Photos
          :photos="userProfile.personalPhotos"
          @deletePhoto="deletePhoto"
          @undoDeletePhoto="undoDeletePhoto"
          @addPhoto="addImage"
          @undoAddPhoto="undoAddPhoto"
          @showError="showError"
          />
      </div>

      <div class="col-lg-8">
        <Nationalities
          :userNationalities.sync="userProfile.nationalities"
          :userId="userProfile.userId"
        />
        <Passports
          :userPassports.sync="userProfile.passports"
          :userId="userProfile.userId"
        />
        <TravellerTypes
          :userTravellerTypes.sync="userProfile.travellerTypes"
          :userId="userProfile.userId"
        />
        <div>
          <Trips
            :trips.sync="userProfile.trips"
            :userId="userProfile.userId"
          />
        </div>
      </div>
    </div>
    <Snackbar :snackbarModel="errorSnackbar" v-on:dismissSnackbar="errorSnackbar.show=false"></Snackbar>
  </div>
</template>

<script>
import ProfilePic from "./ProfilePic/ProfilePic";
import Nationalities from "./Nationalities/Nationalities";
import Passports from "./Passports/Passports";
import TravellerTypes from "./TravellerTypes/TravellerTypes";
import BasicInfo from "./BasicInfo/BasicInfo";
import Trips from "./Trips/Trips";
import Photos from "./Photos/Photos";

import moment from "moment";
import { getUser } from "./ProfileService";
import Snackbar from "../../components/Snackbars/Snackbar";
import {endpoint} from "../../utils/endpoint";

export default {
  components: {
    Snackbar,
    ProfilePic,
    Nationalities,
    Passports,
    BasicInfo,
    TravellerTypes,
    Trips,
    Photos
  },
  data() {
    return {
      userProfile: null,
      photos: null,
      errorSnackbar: {
        show: false,
        text: "",
        color: "error",
        duration: 3000,
        snackbarId: 1
      }
    };
  },
  mounted() {
    this.getUserInfo();
  },
  methods: {
    /**
     * Called when a deletePhoto event is emitted from the photos component.
     * Removes the photo at the given index.
     *
     * @param index {Number} the index of the photo to be removed.
     */
    deletePhoto(index, shouldShowSnackbar) {
      this.userProfile.personalPhotos.splice(index, 1);
      if (shouldShowSnackbar) {
        this.errorSnackbar.color = "success";
        this.errorSnackbar.text = "Photo deleted successfully";
        this.errorSnackbar.show = true;
      }


    },

    /**
     * Called when a undoDeletePhoto event is emitted from the photos component. 
     * Adds the photo back to the list at the given index.
     * 
     * @param index {number} the index where the photo should be added.
     * @param photo {} the photo to add.
     */
    undoDeletePhoto(index, photo) {
      this.userProfile.personalPhotos.splice(index, 0, photo);
    },

    /**
     * Gets a users info and sets the users state
     */
    async getUserInfo() {
      const userId = this.$route.params.id;

      const user = await getUser(userId);

      // Change date format so that it displays on the basic info component.
      const formattedDate = user.dateOfBirth
        ? moment(user.dateOfBirth).format("YYYY-MM-DD")
        : "";

      user.dateOfBirth = formattedDate;

      this.userProfile = user;
    },
    shouldShowBanner() {
      return !(
        this.userProfile.firstName &&
        this.userProfile.lastName &&
        this.userProfile.gender &&
        this.userProfile.dateOfBirth &&
        this.userProfile.nationalities.length &&
        this.userProfile.travellerTypes.length
      );
    },

    /**
     * Updates the profile picture of a user after it has been changed.
     *
     * @param imageObject the new profile picture.
     */
    newProfilePic(imageObject) {
      this.userProfile.profilePhoto = imageObject;
    },
    /**
     * Shows an snackbar error message
     * @param {string} text the text to display on the snackbar
     */
    showError(text) {
      this.errorSnackbar.text = text;
      this.errorSnackbar.show = true;
      this.errorSnackbar.color = "error";
    },
    addImage(image) {
      image.endpoint = endpoint(`/users/photos/${image["photoId"]}?Authorization=${localStorage.getItem("authToken")}`);
      image.thumbEndpoint = endpoint(`/users/photos/${image["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
      this.userProfile.personalPhotos.push(image);
    },
    undoAddPhoto(image) {
      this.userProfile.personalPhotos = this.userProfile.personalPhotos.filter(e => {
        return e.photoId !== image.photoId;
      })
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

#root-container {
  width: 100%;
  margin-left: 15px;
  margin-right: 15px;
}
</style>


