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
          :key="nationality.nationalityId"
          color="primary"
          text-color="white"
        >
          <CountryDisplay :country="nationality.nationalityCountry"/>
        </v-chip>

        <span v-if="!userNationalities.length">Please provide at least one Nationality</span>
      </div>

      <v-combobox
        ref="combobox"
        v-else
        v-model="userNat"
        :items="this.validNationalities"
        :item-text="getNationalityText"
        label="Your nationality"
        :error-messages="nationalityErrors"
        deletable-chips
        :value="userNat"
        @input="updateSelectedNationalities"
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
            <strong>{{ data.item.nationalityName }}</strong>
          </v-chip>
        </template>
      </v-combobox>
    </v-card>
  </div>
</template>

<script>
  import UserStore from "../../../stores/UserStore";
  import {getNationalities} from "./NationalityService";
  import CountryDisplay from "../../../components/Country/CountryDisplay"


  export default {
    components: {
      CountryDisplay
    },
    props: ["userNationalities", "userId"],
    mounted() {
      this.getNationalities();
      this.filterNationalities();
    },
    data() {
      return {
        userStore: UserStore.data,
        userNat: [...this.userNationalities],
        allNationalities: [],
        isEditing: false,
        nationalityErrors: [],
        validNationalities: null
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
          this.$root.$emit('show-error-snackbar', 'Could not load nationalities', 3000);
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
          this.userNationalities.filter(nationality => typeof nationality !== 'string');
          this.userNat = this.userNat.filter(nationality => typeof nationality !== 'string');
          this.$refs.combobox.lazySearch = '';

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
      },

      async filterNationalities() {
        const nationalities = await getNationalities();
        this.validNationalities = nationalities.filter(nationality => nationality.nationalityCountry.isValid === true);
      },
      /**
       * Updates the selected nationalities of the user to all valid ones so if there is an
       * invalid nationality i.e. the user typed a string in the combo box, it is removed
       * automatically.
       * @param nationalities the chosen nationality values
       */
      updateSelectedNationalities(nationalities) {
        const newNationalities = nationalities.filter(nationality => typeof nationality !== 'string');
        this.userNat = newNationalities;
        this.$refs.combobox.lazySearch = '';
        this.$emit('input', newNationalities);
      },
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


