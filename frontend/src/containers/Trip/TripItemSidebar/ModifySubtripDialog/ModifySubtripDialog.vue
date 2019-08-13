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
		</v-card>

	></v-dialog>
	
</template>

<script>
  export default {
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
        isShowingDialog: false
			}
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