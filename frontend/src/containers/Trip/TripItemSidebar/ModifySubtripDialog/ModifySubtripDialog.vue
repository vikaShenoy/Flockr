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
												:items="filteredSubTrips"
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
							@newTripAdded="(subTrip) =>
								$emit('newTripAdded', subTrip)"
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
	import { getTrips, tripNodeContains } from "../../TripService";
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
			 * Add existing trip
			 */
			async addExistingTrip() {
        const validFields = this.validate();
        if (!validFields) return false;
        this.$emit("newTripAdded", this.selectedExistingTrip);
        this.isShowingDialog = false;
			},
			/**
			 * Validates the Add Trip form
			 */
			validate() {
        return this.$refs.addTripForm.validate();
      }
		},
		/**
		 * Gets all the existing trips
		 */
		async mounted() {
      this.existingTrips = await getTrips();
		},
		computed: {
      /**
			 * Filtering for the existing sub trips dropdown.
			 * Removes:
			 *  - the parent trip (can't add it as a sub trip of itself)
			 *  - any sub trip which contains the parent trip
			 *  - any sub trip which is already a sub trip of the parent trip.
       * @returns {*} the list of existing sub trips which can be selected.
       */
			filteredSubTrips() {
        if (this.existingTrips.length) {
          return this.existingTrips.filter(
              tripNode => !tripNodeContains(
                  this.parentTrip.tripNodeId, tripNode) &&
									tripNode.tripNodeId !== this.parentTrip.tripNodeId &&
									!tripNodeContains(tripNode.tripNodeId, this.parentTrip));
				}
        return this.existingTrips;
			}
		},
		watch: {
      // Synchronize both isShowing state and props
			isShowing(value) {
        this.isShowingDialog = value;
			},
			/**
			 * Emits the change in value for showing dialog
			 * @param value the new value
			 */
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