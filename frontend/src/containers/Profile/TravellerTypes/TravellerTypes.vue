<template>
  <div>
    <div class="header">
      <h3>Traveller Types</h3>

      <div id="edit-btn">
        <v-btn
          v-if="userStore.userId === userId"
          small
          flat
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
        >{{ travellerType.travellerTypeName }}
        </v-chip>

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
  import {getAllTravellerTypes} from "./TravellerTypesService";

  export default {
    props: {
      userTravellerTypes: {
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
          this.allTravellerTypes = await getAllTravellerTypes();
        } catch (err) {
          // Add error handling later
        }
      },
      /**
       * Toggles between edit and save, if saving, then update traveller types
       */
      async toggleEditSave() {
        if (this.isEditing) {
          // user was editing and has now submitted their changes
          if (this.editingTravellerTypes.length === 0) {
            this.travellerTypeErrors = ["Please select a traveller type"];
            return;
          }

          this.travellerTypeErrors = [];

          const oldTravellerTypes = this.userTravellerTypes;
          const newTravellerTypes = this.editingTravellerTypes;
          this.$emit("update-user-traveller-types", oldTravellerTypes, newTravellerTypes);
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
    justify-content: space-between;
    align-items: center;

    h3 {
      text-align: left;
    }
  }

  .traveller-types-card {
    padding: 10px;
  }
</style>


