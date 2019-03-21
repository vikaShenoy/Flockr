<template>
  <div id="root-container">
    <div class="row">
    <div class="col-lg-4">
      <ProfilePic />

      <BasicInfo />

      <Photos />
    </div>

    <div class="col-lg-8">
      <Nationalities :userNationalities.sync="nationalities" />
      <Passports :userPassports="passports" />
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
  data() {
    return {
      nationalities: [
        {
          nationalityId: 1,
          nationalityCountry: "New Zealand"        
        }
    ],
    passports: [
      {
        passportId: 3,
        passportCountry: "French"        
      },
      {
        passportId: 4,
        passportCountry: "German"
      }
    ]
    };
  },
  methods: {
  }
};
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #root-container {
    width: 100%;
  }

</style>


