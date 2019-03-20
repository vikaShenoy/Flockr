<template>
	<div class="traveller-types">
		<h2>Traveller Types</h2>
		<ul>
			<v-chip
				v-for="travellerType in travellerTypes"
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
export default {
	props: {
		travellerTypes:  {
			type: Array,
			required: true
		}
	},
	methods: {
		removeTravellerType(travellerTypeId) {
			// called when the user delete a traveller type
			// the event is emitted for *some* parent component to handle it
			this.$emit('delete-traveller-type', travellerTypeId);
		}
	}
}
</script>

<style lang="scss" scoped>
	.traveller-types {
		height: 50%;
	}
</style>


