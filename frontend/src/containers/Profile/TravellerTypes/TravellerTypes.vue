<template>
	<div class="traveller-types">
		<h2>Traveller Types</h2>
		<ul>
			<v-chip
				v-for="travellerType in travellerTypes"
				:key="travellerType.travellerTypeId"
				color="primary"
				close="true"
				text-color="white"
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
	}
}
</script>

<style lang="scss" scoped>
  .traveller-types {
  	height: 50%;
  }
</style>


