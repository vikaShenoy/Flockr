<template>
  <div>

    <div class="header">
      <h3>Traveller Types</h3>
      <div>
        <v-btn
					v-if="userStore.userId === userId"
          small
          flat
          id="edit-btn"
          color="secondary"
          @click="toggleEditSave"
        >
          <v-icon v-if="!isEditing">edit</v-icon>
          <span v-else>Save</span>
        </v-btn>
      </div>
    </div>

    <v-card class="traveller-types-card">
      <div v-if="!isEditing">
        <v-chip
          v-for="travellerType in userTravellerTypes"
          v-bind:key="travellerType.travellerTypeId"
          color="primary"
          text-color="white"
        >{{ travellerType.travellerTypeName }}</v-chip>

				<span v-if="!userTravellerTypes.length">Please provide at least one traveller type</span>
      </div>


      <v-combobox
        v-else
        v-model="editingTravellerTypes"
        :items="allTravellerTypes"
        :item-text="getTravellerTypeName"
        :error-messages="travellerTypeErrors"
        label="Your traveller types"
        chips
        clearable
        solo
        multiple
      >

        <template v-slot:selection="data">
          <v-chip
            color="primary"
            text-color="white"
            :selected="data.selected"
            close
            @input="removeTravellerType(data.item)"
          >
            <strong>{{ data.item.travellerTypeName }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-combobox>

    </v-card>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";
import { getAllTravellerTypes, updateTravellerTypes } from "./TravellerTypesService";

export default {
	props: {
		userTravellerTypes:  {
			type: Array,
			required: true
		},
		userId: {
			type: Number,
			required: true
		}
	},
	data() {
		return {
			userStore: UserStore.data,
			allTravellerTypes: [],
			isEditing: false,
			editingTravellerTypes: [...this.userTravellerTypes],
			travellerTypeErrors: []
		}
	},
	mounted() {
		this.getAllTravellerTypes();
	},
	methods: {
		/**
		 * Gets all traveller types
		 */
		async getAllTravellerTypes() {
			try {
				const travellerTypes = await getAllTravellerTypes();
				this.allTravellerTypes = travellerTypes;
			} catch (err) {
				// Add error handling later
			}
		},
		/**
		 * Toggles between edit and save, if saving, then update traveller types
		 */
		async toggleEditSave() {
			if (this.isEditing) {
				if (this.editingTravellerTypes.length === 0) {
					this.travellerTypeErrors = ["Please select a traveller type"];
					return;
				} 

				this.travellerTypeErrors = [];

				const travellerTypeIds = this.editingTravellerTypes.map(t => t.travellerTypeId);

				try {
					const userId = this.$route.params.id;
					await updateTravellerTypes(userId, travellerTypeIds);
				} catch (e) {
					// Add error handling later
				}

				// Set traveller types in global user state
				UserStore.data.travellerTypes = this.editingTravellerTypes;
				this.$emit("update:userTravellerTypes", this.editingTravellerTypes);

			}
			this.isEditing = !this.isEditing;
		},
		/**
		 * Gets just the name from traveller type objects
		 */
		getTravellerTypeName: (travellerType) => travellerType.travellerTypeName,
    /**
     * Removes a traveller type in edit mode
     */
    removeTravellerType(item) {
      this.editingTravellerTypes.splice(this.editingTravellerTypes.indexOf(item), 1);
      this.editingTravellerTypes = [...this.editingTravellerTypes];
    }
	}
}
</script>

<style lang="scss" scoped>
  .header {
  width: 100%;
  display: flex;
  flex-flow: row nowrap;
  justify-content: center;
  align-items: center;

  > * {
    flex-grow: 1;
  }
}

.traveller-types-card {
  padding: 10px;
}

#edit-btn {
  float: right;
}
</style>


