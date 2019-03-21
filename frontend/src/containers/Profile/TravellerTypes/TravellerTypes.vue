<template>
	<div class="traveller-types">

		<div class="header">
			<h3>Traveller Types</h3>
			<v-btn small flat id="edit-btn" color="secondary" @click="toggleEditSave">
				<v-icon v-if="!isEditing">edit</v-icon>
				<span v-else>Save</span>
			</v-btn>
		</div>

		<v-card class="traveller-types-card">
			<div v-if="!isEditing">
				<v-chip v-for="travellerType in userTravellerTypes" v-bind:key="travellerType.travellerTypeId" color="primary"
					text-color="white">{{ travellerType.travellerTypeName }}</v-chip>
			</div>

			<v-combobox v-else v-model="userTravellerTypes" :items="allTravellertypes" :item-text="getTravellerTypeName"
				label="Your traveller types" chips clearable solo multiple>

				<template v-slot:selection="data">
					<v-chip color="primary" text-color="white" :selected="data.selected" close @input="removeTravellerType(data.item.travellerTypeId)">
						<strong>{{ data.item.travellerTypeName }}</strong>&nbsp;
					</v-chip>
				</template>
			</v-combobox>

		</v-card>
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
			isEditing: false
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
		async toggleEditSave() {
			if (this.isEditing) {
				const travellerTypeIds = this.userTravellerTypes.map((t) => t.travellerTypeId);
				this.updateTravellerTypes(travellerTypeIds);
			}
			this.isEditing = !this.isEditing;
		},
		updateTravellerTypes(travellerTypeIds) {
			// called when the user updates their traveller types
			// the event is emitted for *some* parent component to handle it
			this.$emit('update-traveller-types', this.travellerTypeIds);
		},
		getTravellerTypeName: (travellerType) => travellerType.travellerTypeName
	},
	mounted() {
		this.getAllTravellerTypes();
	},
	computed: {
		addableTravellerTypes: function computeAddableTravellerTypes() {
			const userTravellerTypeIds = this.userTravellerTypes.map((travellerType) => travellerType.travellerTypeId);
			return this.allTravellertypes.filter((travellerType) => !userTravellerTypeIds.includes(travellerType.travellerTypeId));
		},
	}
}
</script>

<style lang="scss" scoped>
	.traveller-types {
		height: 50%;
	}

	.header {
		width: 100%;
		display: flex;
		flex-flow: row nowrap;
		justify-content: center;
		align-items: center;

		>* {
			flex-grow: 1;
		}
	}

	.traveller-types-card {
		padding: 10px;
	}
</style>


