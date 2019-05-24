<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
	<div style="width: 100%">
		<div class="page-title"><h1>Search Travellers</h1></div>
		<div class="search-container">
			<!-- Min and Max Age Slider -->
			<!-- Nationality, Gender and Traveller Typ Select Input -->
			<v-card elevation="10" flat color="transparent" class="age-filter-card">
				<v-card-text
					class="age-slider">Select Age Range
					<div class="row">
						<div class="col-md-1">
							<v-text-field
								id="min-age-text"
								v-model="ageRange[0]"
								class="mt-0 age-text-field"
								hide-details
								:min="minAge"
								:max="ageRange[1]"
								single-line
								type="number"
							></v-text-field>
						</div>
						<div class="col-md-8">
							<v-range-slider
								v-model="ageRange"
								:min="minAge"
								:max="maxAge"
								class="age-range-slider"
							></v-range-slider>
						</div>
						<div class="col-md-1">
							<v-text-field
								id="max-age-text"
								v-model="ageRange[1]"
								class="mt-0 age-text-field"
								hide-details
								:min="ageRange[0]"
								:max="maxAge"
								single-line
								type="number"
							></v-text-field>
						</div>
					</div>
				</v-card-text>

				<div class="row filters-card">
					<v-select
						class="selector-input col-md-3"
						label="Nationality"
						:items="nationalities.names"
						:value="nationality"
						v-model="nationality"
					></v-select>

					<v-select
						class="selector-input col-md-3"
						label="Gender"
						:items="genders"
						:value="gender"
						v-model="gender"
					></v-select>

					<v-select
						class="selector-input col-md-3"
						label="Traveller Type"
						:items="travellerTypes.names"
						:value="travellerType"
						v-model="travellerType"
					></v-select>
					<div class="">
						<v-btn id="searchButton" color="secondary" depressed v-on:click="search" class="button button-card">Search</v-btn>
						<v-btn id="clearButton" color="secondary" depressed v-on:click="clearFilters" class="button button-card">Clear</v-btn>
					</div>
				</div>
			</v-card>

			<v-card elevation="10" class="table-card">
				<!-- Table Result -->
				<v-data-table
					:headers="headers"
					:items="travellers"
					class="elevation-1"
					hide-actions
				>
					<template v-slot:items="props">
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">
							<img v-if="props.item.profilePhoto" class="profile-pic" alt="Profile Photo" :src="photoUrl(props.item.profilePhoto.photoId)">
							<img v-else class="profile-pic" alt="Profile Photo" src="../Profile/ProfilePic/defaultProfilePicture.png">
						</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">{{ props.item.firstName
							}}
						</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">{{ props.item.middleName }}</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">{{ props.item.lastName }}
						</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">{{ props.item.age }}</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">{{ props.item.gender }}
						</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">
							<v-chip class="table-chip" v-for="nationality in props.item.nationalities"
											v-bind:key="nationality">{{
								nationality }}
							</v-chip>
						</td>
						<td class="text-xs-left" @click="$router.push(`/profile/${props.item.userId}`)">
							<v-chip class="table-chip" v-for="type in props.item.travellerTypes" v-bind:key="type">{{
								type }}
							</v-chip>
						</td>
					</template>
				</v-data-table>
			</v-card>
		</div>
	</div>
</template>

<script>
	import {requestTravellers} from "./SearchTravellersService";
	import {getNationalities} from "../Profile/Nationalities/NationalityService";
	import {getAllTravellerTypes} from "../Profile/TravellerTypes/TravellerTypesService";
	import { endpoint } from "../../utils/endpoint";
	import moment from "moment";

	export default {
		data() {
			return {
				nationalities: {
					names: [],
					ids: []
				},
				minAge: 13,
				maxAge: 115,
				ageRange: [13, 115],
				genders: ["Female", "Male", "Other"],
				travellerTypes: {
					names: [],
					ids: []
				},
				nationality: "",
				travellerType: "",
				gender: "",

				headers: [
					{text: 'Photo', align: 'left', sortable: false, value: 'profilePhoto'},
					{text: 'First Name', align: 'left', sortable: true, value: 'firstName'},
					{text: 'Middle Name', align: 'left', sortable: true, value: 'middleName'},
					{text: 'Last Name', align: 'left', sortable: true, value: 'lastName'},
					{text: 'Age', align: 'left', sortable: true, value: 'age'},
					{text: 'Gender', align: 'left', sortable: true, value: 'gender'},
					{text: 'Nationality', align: 'left', sortable: false, value: 'nationalityName'},
					{text: 'Traveller Type(s)', align: 'left', sortable: false, value: 'travellerTypes'},
				],
				travellers: []
			}
		},
		mounted: async function () {
			// Get all the current travellers
			this.search();

			// Get the nationalities
			let currentNationalities;
			try {
				currentNationalities = await getNationalities();

				for (let index = 0; index < currentNationalities.length; index++) {
					this.nationalities.names.push(currentNationalities[index].nationalityName);
					this.nationalities.ids.push(currentNationalities[index].nationalityId);
				}
			} catch (error) {
				console.log(error);
			}

			// Get all the traveller types
			let currentTravellerTypes;
			try {
				currentTravellerTypes = await getAllTravellerTypes();

				for (let index = 0; index < currentTravellerTypes.length; index++) {
					this.travellerTypes.names.push(currentTravellerTypes[index].travellerTypeName);
					this.travellerTypes.ids.push(currentTravellerTypes[index].travellerTypeId);
				}
			} catch (error) {
				console.log(error);
			}
		},
		methods: {
			search: async function () {
				// get the queries from the selector variables
				// parse them into an acceptable format to be sent
				let queries = "";
				queries += "ageMin=" + moment().subtract(this.ageRange[0], "years");
				queries += "&ageMax=" + moment().subtract(this.ageRange[1], "years");

				if (this.gender !== "") {
					queries += "&gender=" + this.gender;
				}

				if (this.nationality !== "") {
					let nationalityIndex = this.nationalities.names.indexOf(this.nationality);
					queries += "&nationality=" + this.nationalities.ids[nationalityIndex];
				}

				if (this.travellerType !== "") {
					let typeIndex = this.travellerTypes.names.indexOf(this.travellerType);
					queries += "&travellerType=" + this.travellerTypes.ids[typeIndex];
				}

				try {
					// call the get travellers function passing in the formatted queries
					this.travellers = await requestTravellers(queries);
					console.log(this.travellers);

					for (let i = 0; i < this.travellers.length; i++) {
						// Calculate the age from the date of birth and set it in the traveller
						this.travellers[i].age = moment().diff(moment(this.travellers[i].dateOfBirth), "years");

						const nationalityNames = this.travellers[i].nationalities.map(nationality => nationality.nationalityName);
						this.travellers[i].nationalities = nationalityNames;

						const travellerTypes = this.travellers[i].travellerTypes.map(travellerType => travellerType.travellerTypeName);
						this.travellers[i].travellerTypes = travellerTypes;

					}
				} catch (error) {
					console.log(error);
				}
			},
			clearFilters: async function () {
				// Reset the selector variables to their default values
				this.ageRange = [13, 115];
				this.nationality = "";
				this.travellerType = "";
				this.gender = "";
				// Call the search function to get unfiltered results
				this.search();
			},

			/**
			 * Gets the URL of a photo for a user
			 * @param {number} photoId the ID of the photo to get
			 * @returns {string} the url of the photo
			 */
			photoUrl(photoId) {
				const authToken = localStorage.getItem("authToken");
				const queryAuthorization = `?Authorization=${authToken}`;
				return endpoint(`/users/photos/${photoId}${queryAuthorization}`);
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../styles/_variables.scss";

	.search-container {
		margin: 110px 0 0;
	}

	.page-title {
		position: fixed;
		z-index: 1;
		width: 100%;
		padding: 15px;
		text-align: left;
		background: $primary;
		font-size: 20px;

		h1 {
			color: $darker-white;
		}
	}

	.title-card {
		display: inline-block;
	}

	.selector-input {
		padding: 0 0 0 10px;
		text-align: left;
	}

	.age-slider {
		padding: 10px 20% 0 20%;
		font-size: 16px;
	}

	.button {
		background-color: $primary;
		margin: auto;
	}

	.filters-card {
		margin: 10px;
		padding: 20px 20px 10px 20px;
	}

	.age-filter-card {
		margin: 10px;
	}

	.age-text-field {
		min-width: 50px;
	}

	.age-range-slider {
		padding: 0 0 0 10px;
	}

	.table-chip {
		background-color: $primary;
		color: $darker-white;
	}

	.button-card {
		padding: 10px;
		margin: 5px;
	}

	.table-card {
		margin: 10px;
	}

	.profile-pic {
		width: 100%;
		height: auto;
	}

</style>
