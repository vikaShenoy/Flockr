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
      <TravellerTypes :travellerTypes="travellerTypes"/>
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
      travellerTypes: [
				{
					travellerTypeName: 'Groupie',
					travellerTypeId: 0
				},
				{
					travellerTypeName: 'Thrill Seeker',
					travellerTypeId: 1
				},
				{
					travellerTypeName: 'Gap Year',
					travellerTypeId: 2
				},
				{
					travellerTypeName: 'Frequent weekender',
					travellerTypeId: 3
				},
				{
					travellerTypeName: 'Holidaymaker',
					travellerTypeId: 4
				},
				{
					travellerTypeName: 'Functional/Business',
					travellerTypeId: 5
				},
				{
					travellerTypeName: 'Backpacker',
					travellerTypeId: 6
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


