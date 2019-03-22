<template>
  <div id="root-container" v-if="userProfile">
    <div class="row">
    <div class="col-lg-4">
      <ProfilePic />

      <BasicInfo  :userProfile.sync="userProfile" />

      <Photos />
    </div>

    <div class="col-lg-8">
      <Nationalities :userNationalities.sync="userProfile.nationalities" />
      <Passports :userPassports.sync="userProfile.passports" />
      <TravellerTypes />
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
    async getUserInfo() {
      const travellerId = this.$route.params.id;
      const authToken = localStorage.getItem("authToken");
      const res = await superagent.get(`http://localhost:9000/api/travellers/${travellerId}`)
      .set("Authorization", authToken);

      console.log(res.body);
      // Change date format so that it displays on the basic info component. 
      const formattedDate = res.body.dateOfBirth ? moment(res.body.dateOfBirth).format("YYYY-MM-DD") : "";
      res.body.dateOfBirth = formattedDate;

      this.userProfile = res.body;
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


