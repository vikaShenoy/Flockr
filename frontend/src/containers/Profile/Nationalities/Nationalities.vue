<template>
  <div>
    <div id="header">
      <h3>Nationalities</h3>
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
    <v-card id="nationalities">
      <div v-if="!isEditing">
        <v-chip
          v-for="nationality in userNationalities"
          v-bind:key="nationality.nationalityId"
          color="primary"
          text-color="white"
        >{{ nationality.nationalityCountry }}</v-chip>

        <span v-if="!userNationalities.length">Please provide at least one Nationality</span>
      </div>

      <v-combobox
        v-else
        v-model="userNat"
        :items="this.allNationalities"
        :item-text="getNationalityText"
        label="Your nationality"
        :error-messages="nationalityErrors"
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
            @input="remove(data.item)"
          >
            <strong>{{ data.item.nationalityCountry }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-combobox>
    </v-card>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";
import { getNationalities, updateNationalities } from "./NationalityService.js";

export default {
  mounted() {
    this.getNationalities();
  },
  data() {
    return {
      userStore: UserStore.data,
      // These would be retreived from the request
      userNat: [...this.userNationalities],
      allNationalities: [],
      isEditing: false,
      nationalityErrors: []
    };
  },
  methods: {
    /**
     * Gets all nationalities
     */
    async getNationalities() {
      try {
        const nationalities = await getNationalities();
        this.allNationalities = nationalities;
      } catch (e) {
        // Add error handling later
      }
    },
    /**
     * Toggles between editing and saving, if saving, then nationalities will
     * be updated
     */
    async toggleEditSave() {
      if (this.isEditing) {
        if (this.userNat.length === 0) {
          this.nationalityErrors = ["Please select a nationality"];
          return;
        }

        this.nationalityErrors = [];

        const userId = this.$route.params.id;
        const nationalityIds = this.getNationalityIds;
        try {
          await updateNationalities(userId, nationalityIds);
        } catch (e) {
          // Add error handling later
        }

        // Set nationalities state of UserStore
        UserStore.data.nationalities = this.userNat;
        this.$emit("update:userNationalities", this.userNat);
      }
      this.isEditing = !this.isEditing;
    },
    /**
     * Gets the nationality country from the list of nationalities
     */
    getNationalityText: item => item.nationalityCountry,
    /**
     * Removes a nationality in edit mode
     */
    remove(item) {
      this.userNat.splice(this.userNat.indexOf(item), 1);
      this.userNat = [...this.userNat];
    }
  },

  computed: {
    /**
     * Gets nationality ID's from nationality objects
     */
    getNationalityIds() {
      return this.userNat.map(nationality => nationality.nationalityId);
    }
  },
  props: ["userNationalities", "userId"]
};
</script>

<style lang="scss" scoped>
#nationalities {
  padding: 10px;
}

#header {
  width: 100%;
  display: flex;
  flex-flow: row nowrap;
  justify-content: center;
  align-items: center;
  > * {
    flex-grow: 1;
  }
}

#edit-btn {
  float: right;
}
</style>


