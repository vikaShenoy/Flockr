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
      <TravellerTypes
	  	:travellerTypes="travellerTypes"
			v-on:delete-traveller-type="(travellerTypeId) => handleDeleteTravellerType(travellerTypeId)"
	  />
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
import {endpoint} from '../../utils/endpoint';
const superagent = require('superagent');

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
			travellerTypes: [{
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
		async handleDeleteTravellerType(travellerTypeId) {
			console.log("DELETE METHOD CALLED");
			// catch event emitted when the user wants to delete a traveller type
			const userId = 1; // TODO: change this to be the actual user id
			const authToken = localStorage.getItem('authToken');
			const url = endpoint(`/travellers/${userId}/travellerTypes/${travellerTypeId}`);
			try {
				await superagent
					.del(url).set('Authorization', authToken);

				// delets traveller type from the UI (will happen if request is successful)
				this.travellerTypes = this.travellerTypes.filter((travellerType) => travellerType.travellerTypeId !== travellerTypeId);
			} catch (err) {
				console.log(err);
				const message = `Error trying to delete traveller type ${travellerTypeId} for user ${userId}: ${err}`;
				console.error(message);
				// TODO: inform the user that couldn't delete traveller type
			}
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


