<template>
  <div>
    <div id="header">
      <h3>Nationalities</h3>

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
    <v-card id="nationalities">
      <div v-if="!isEditing">
        <v-chip
          v-for="nationality in userNationalities"
          v-bind:key="nationality.nationalityId"
          color="primary"
          text-color="white"
        >{{ nationality.nationalityName }}</v-chip>

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
            <strong>{{ data.item.nationalityName }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-combobox>
    </v-card>
  </div>
</template>

<script>
import UserStore from "../../../stores/UserStore";
import { getNationalities } from "./NationalityService";
import UndoRedo from "../../../components/UndoRedo/UndoRedo";
import Command from "../../../components/UndoRedo/Command";

export default {
  props: ["userNationalities", "userId"],
  mounted() {
    this.getNationalities();
  },
  components: {
    UndoRedo
  },
  data() {
    return {
      userStore: UserStore.data,
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
        // if user was editing and has now submitted their changes
        if (this.userNat.length === 0) {
          this.nationalityErrors = ["Please select a nationality"];
          return;
        }

        this.nationalityErrors = [];
        const oldNationalities = this.userNationalities;
        const newNationalities = this.userNat;
        this.$emit("update-user-nationalities", oldNationalities, newNationalities);
      }
      this.isEditing = !this.isEditing;
    },
    /**
     * Gets the nationality country from the list of nationalities
     */
    getNationalityText: item => item.nationalityName,
    /**
     * Removes a nationality in edit mode
     */
    remove(item) {
      this.userNat.splice(this.userNat.indexOf(item), 1);
      this.userNat = [...this.userNat];
    }
  }
};
</script>

<style lang="scss" scoped>
#nationalities {
  padding: 10px;
  margin-bottom: 20px;
}

#header {
  width: 100%;
  margin-top: 20px;
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
  align-items: center;

  h3 {
    text-align: left;
  }
}
</style>


