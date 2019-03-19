<template>
  <div id="root-container">
    <div class="row">
    <div class="col-lg-4">
      <ProfilePic />

      <BasicInfo />

      <Photos />
    </div>
    <v-card class="col-lg-8" >
      <NationalityPassports />
      <TravellerTypes />
      <Trips />
    </v-card>
    </div>
  </div>
</template>

<script>
import ProfilePic from "./ProfilePic/ProfilePic";
import NationalityPassports from "./NationalityPassports/NationalityPassports";
import TravellerTypes from "./TravellerTypes/TravellerTypes";
import BasicInfo from "./BasicInfo/BasicInfo";
import Trips from "./Trips/Trips";
import Photos from "./Photos/Photos";

export default {
  components: {
    ProfilePic,
    NationalityPassports,
    BasicInfo,
    TravellerTypes,
    Trips,
    Photos
  },
  data() {
    return {
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


