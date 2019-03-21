<template>
	<div class="traveller-types">
		<h2>Traveller Types</h2>
		<ul>
			<v-chip
				v-for="travellerType in userTravellerTypes"
				:key="travellerType.travellerTypeId"
				color="primary"
				:close="true"
				text-color="white"
				@input="removeTravellerType(travellerType.travellerTypeId)"
			>
				{{ travellerType.travellerTypeName }}
			</v-chip>
		</ul>
	</div>
</template>

<script>
const superagent = require('superagent');
const {endpoint} = require('../../../utils/endpoint');

export default {
	props: {
		userTravellerTypes:  {
			type: Array,
			required: true
		}
	},
	data() {
		return {
			allTravellertypes: [
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
			],
		}
	},
	methods: {
		async getAllTravellerTypes() {
			const url = endpoint('/travellers/types');
			const authToken = localStorage.getItem('authToken');
			try {
				const res = await superagent
					.get(url)
					.set('Authorization', authToken);
				// set all traveller types to be the API response
				this.allTravellerTypes = res.body;
			} catch (err) {
				console.error('Could not get all traveller types: ', err);
			}
		},
		removeTravellerType(travellerTypeId) {
			// called when the user delete a traveller type
			// the event is emitted for *some* parent component to handle it
			this.$emit('delete-traveller-type', travellerTypeId);
		}
	},
	mounted() {
		this.getAllTravellerTypes();
	},
	computed: {
		addableTravellerTypes: function computeAddableTravellerTypes() {
			const userTravellerTypeIds = this.userTravellerTypes.map((travellerType) => travellerType.travellerTypeId);
			return this.allTravellertypes.filter((travellerType) => !userTravellerTypeIds.includes(travellerType.travellerTypeId));
		}
	}
}
</script>

<style lang="scss" scoped>
	.traveller-types {
		height: 50%;
	}
</style>


