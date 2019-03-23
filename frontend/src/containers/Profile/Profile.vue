<template>
  <div id="root-container" v-if="userProfile">
    <div class="row">
    <div class="col-lg-4">
      <ProfilePic />

      <BasicInfo :userProfile.sync="userProfile" />

      <Photos />
    </div>

    <div class="col-lg-8">
      <Nationalities :userNationalities.sync="userProfile.nationalities" :userId="userProfile.userId" />
      <Passports :userPassports.sync="userProfile.passports" :userId="userProfile.userId" />
      <TravellerTypes :userProfile="userProfile" />
      <Trips />
      </div>
    </div>
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

import superagent from "superagent";
import moment from "moment";

import { getUser } from "./ProfileService";

export default {
  components: {
    ProfilePic,
    Nationalities,
    Passports,
    BasicInfo,
    TravellerTypes,
    Trips,
    Photos
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
      const formattedDate = user.dateOfBirth ? moment(user.dateOfBirth).format("YYYY-MM-DD") : "";
      
      user.dateOfBirth = formattedDate;

      this.userProfile = user;
    }
  },
  data() {
    return {
      userProfile: null
    }
  }
};
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #root-container {
    width: 100%;
  }

</style>


