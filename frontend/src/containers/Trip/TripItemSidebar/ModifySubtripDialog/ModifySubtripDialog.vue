<template>
	<v-dialog
		v-model="isShowingDialog"
		width="600px"
	>
		<v-card id="modify-subtrip-dialog">
			<v-card-title class="primary title">
				<v-layout row>
					<v-spacer align="center">
						<h2 class="light-text">{{editMode ? "Edit Subtrip" : "Add Subtrip"}}</h2>
					</v-spacer>
				</v-layout>
			</v-card-title>
			<v-form ref="form">
				<v-container grid-list-md>
					<v-layout row wrap>
						<v-flex xs12>
							<v-select
								:items="trips"
								label="Select existing trip"
							/>
							<AddTrip
							:isSidebarComponent="true"
							:users="trip.users"
							:parentTrip="trip"
							@new-trip-was-added="newTripWasAdded"
							@cancel-trip-creation="isShowingDialog = false"
							>
							</AddTrip>

						</v-flex>
					</v-layout>
				</v-container>
			</v-form>
		</v-card>

	></v-dialog>
	
</template>

<script>
	import AddTrip from "../../../AddTrip/AddTrip"
  export default {
    components: {AddTrip},
    props: {
      isShowing: {
        type: Boolean,
				required: false
			},
			editMode: {
        type: Boolean,
				required: true
			},
			trip: {
        type: Object,
				required: true
			}
		},
		data() {
      return {
        isShowingDialog: false,
				trips: [],
			}
		},
		methods: {
      /**
			 * Pass the created trip Id to the trip item sidebar component
			 * when the user creates a trip in the dialog..
       * @param tripId id of the newly created trip.
       */
      newTripWasAdded(tripId) {
        this.$emit("new-trip-was-added", tripId);
			}
		},
		mounted() {
		},
		watch: {
      // Synchronize both isShowing state and props
			isShowing(value) {
        this.isShowingDialog = value;
			},
			isShowingDialog(value) {
        this.$emit("update:isShowing", value);
			}
		}
  }
</script>

<style scoped>
	.light-text {
		color: #fff
	}

</style>