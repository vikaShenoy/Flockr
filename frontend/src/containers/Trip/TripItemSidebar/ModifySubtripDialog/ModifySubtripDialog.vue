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
							@newTripAdded="(subTrip, oldParentTrip, newParentTrip) =>
							$emit('newTripAdded', subTrip, oldParentTrip, newParentTrip)"
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
	import { sortTimeline } from "../Timeline/TimelineService";
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
			async addExistingTrip() {
        const validFields = this.validate();
        if (!validFields) return false;

        // TODO - fix this so can be expanded
        //this.selectedExistingTrip.isShowing = false;
        let trip = {...this.selectedExistingTrip, isShowing: false};
        this.parentTrip.tripNodes.push(this.selectedExistingTrip);

        await editTrip(this.parentTrip);
        const timelines = document.querySelectorAll(".v-timeline");
        const timeline = timelines[timelines.length - 1];
        sortTimeline(timeline, indexes => {
          this.$emit("tripNodeOrderChanged", indexes);
				});
        this.isShowingDialog = false;
			},
			validate() {
        return this.$refs.addTripForm.validate();
      }

		},
		async mounted() {
      this.existingTrips = await getTrips();
			// Filter the list of available subtrips to disallow the parent becoming its own subtrips or having duplicates.
			let parentTripNodes = this.parentTrip.tripNodes.map(tripNode => tripNode.tripNodeId);
      this.existingTrips = this.existingTrips.filter(tripNode =>
					tripNode.tripNodeId !== this.parentTrip.tripNodeId && !parentTripNodes.includes(tripNode.tripNodeId));
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