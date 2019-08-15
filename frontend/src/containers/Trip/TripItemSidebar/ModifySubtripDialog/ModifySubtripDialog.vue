<template>
	<v-dialog
		v-model="isShowingDialog"
		width="850px"
	>
		<v-card id="modify-subtrip-dialog">
			<v-card-title class="primary title">
				<v-layout row>
					<v-spacer align="center">
						<h2 class="light-text">{{editMode ? "Edit Subtrip" : "Add Subtrip"}}</h2>
					</v-spacer>
				</v-layout>
			</v-card-title>
				<v-container grid-list-md>
					<v-layout row wrap>
						<v-flex xs12>
							<v-form ref="addTripForm">

								<v-select
												label="Select existing trip"
												:items="existingTrips"
												item-text="name"
												v-model="selectedExistingTrip"
												:rules="fieldRules"
												return-object
								/>
								<v-btn
												depressed
												color="secondary"
												@click="addExistingTrip"
								>
									Add
								</v-btn>


							</v-form>

							<AddTrip
							:isSidebarComponent="true"
							:users="parentTrip.users"
							:parentTrip="parentTrip"
							@new-trip-was-added="newTripWasAdded"
							@cancel-trip-creation="isShowingDialog = false"
							@close-dialog="isShowingDialog = false"
							>
							</AddTrip>

						</v-flex>
					</v-layout>
				</v-container>
		</v-card>

	></v-dialog>
	
</template>

<script>
	import AddTrip from "../../../AddTrip/AddTrip";
	import { getTrips, editTrip } from "../../TripService";
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
			parentTrip: {
        type: Object,
				required: true
			}
		},
		data() {
      return {
        isShowingDialog: false,
				existingTrips: [],
				fieldRules: [field => !!field || "Field is required"],
				selectedExistingTrip: null,
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
			},
			async addExistingTrip() {
        const validFields = this.validate();
        if (!validFields) return false;

        // TODO - fix this so can be expanded
        this.selectedExistingTrip.isShowing = false;

        this.parentTrip.tripNodes.push(this.selectedExistingTrip);
        await editTrip(this.parentTrip);
        this.isShowingDialog = false;
			},
			validate() {
        return this.$refs.addTripForm.validate();
      }

		},
		async mounted() {
      this.existingTrips = await getTrips();
      // TODO - filter out child tripcomposites
      this.existingTrips = this.existingTrips.filter(trip => trip.tripNodeId !== this.parentTrip.tripNodeId);
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