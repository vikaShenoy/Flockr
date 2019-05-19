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
        <ProfilePic :photos="userProfile.personalPhotos" :userId="userProfile.userId" v-on:newProfilePic="changeProfilePic" v-on:showError="showError"/>

        <BasicInfo :userProfile.sync="userProfile" />

        <Photos :photos="userProfile.personalPhotos" />
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
        this.userProfile.middleName &&
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
    changeProfilePic(imageObject) {
      this.userProfile.profilePhoto = imageObject;
    },
    /**
     * Shows an snackbar error message
     * @param {string} text the text to display on the snackbar
     */
    showError(text) {
      this.errorSnackbar.text = text;
      this.errorSnackbar.show = true;
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


