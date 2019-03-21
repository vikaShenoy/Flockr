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
	  	:userTravellerTypes="userTravellerTypes"
		v-on:updates-traveller-types="(travellerTypeIds) => handleUpdateTravellerTypes(travellerTypeIds)"
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
			userTravellerTypes: [
				{
					travellerTypeName: 'Groupie',
					travellerTypeId: 0
				},
				{
					travellerTypeName: 'Thrill Seeker',
					travellerTypeId: 1
				}
			]
		};
	},
	methods: {
		async handleUpdateTravellerTypes(travellerTypeIds) {
			// catch event emitted when the user wants to delete a traveller type
			const userId = 1; // TODO: change this to be the actual user id
			const authToken = localStorage.getItem('authToken');
			const url = endpoint(`travellers/${userId}/travellerTypes`);

			try {
				const res = await superagent
					// TODO: send this request
					.patch(url)
					.set('Authorization', authToken)
					.send({
						travellerTypeIds: travellerTypeIds
					});
				this.userTravellerTypes = this.userTravellerTypes.filter(t => travellertypeIds.includes(t.travellerTypeId));
			} catch (err) {
				console.error(`Could not send PATCH traveller types for user ${userId}`)
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


