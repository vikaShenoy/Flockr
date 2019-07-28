<template>
  <div>
    <div id="header">
      <h3>Passports</h3>

      <div id="edit-btn">
        <v-btn v-if="userStore.userId === userId" small flat color="secondary" @click="toggleEditSave">
          <v-icon v-if="!isEditing">edit</v-icon>
          <span v-else>Save</span>
        </v-btn>
      </div>
    </div>
    

    <v-card id="passports">
      <div v-if="!isEditing">
        <v-chip
          v-for="passport in userPassports"
          v-bind:key="passport.passportId"
          color="primary"
          text-color="white"
        >{{ passport.passportCountry }}</v-chip>

        <span v-if="!userPassports.length">Add your passports here</span>
      </div>

      <v-combobox
        v-else
        v-model="userPass"
        :items="this.allPassports"
        :item-text="getPassportText"
        label="Your passports"
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
            <strong>{{ data.item.passportCountry }}</strong>&nbsp;
          </v-chip>
        </template>
      </v-combobox>


    </v-card>
  </div>
</template>

<script>
import superagent from "superagent";
import { endpoint } from "../../../utils/endpoint";
import UserStore from "../../../stores/UserStore";
import { getPassports } from "./PassportService.js";
import UndoRedo from "../../../components/UndoRedo/UndoRedo";
import Command from "../../../components/UndoRedo/Command";

export default {
  props: ["userPassports", "userId"],
  mounted() {
    this.getPassports();
  },
  data() {
    return {
      userStore: UserStore.data,
      allPassports: [], // retrieved from API
      isEditing: false,
      userPass: [...this.userPassports]
    };
  },
  methods: {
    /**
     * Gets all passports
     */
    async getPassports() {
      try {
        const passports = await getPassports();
        this.allPassports = passports;
      } catch (e) {
        // Add error handling later
      }
    },
    /**
     * Toggles between editing and saving, if saving, send request to update passports
     */
    async toggleEditSave() {
      if (this.isEditing) {
        // user was editing and has now submitted their changes
        const oldPassports = this.userPassports;
        const newPassports = this.userPass;
        this.$emit("update-user-passports", oldPassports, newPassports);
      }

      this.isEditing = !this.isEditing;
    },
    getPassportText: item => item.passportCountry,
    /**
     * Removes a passport in edit mode
     */
    remove (item) {
      this.userPass.splice(this.userPass.indexOf(item), 1);
      this.userPass = [...this.userPass];
    }

  }
}
</script>

<style lang="scss" scoped>
#passports {
  padding: 10px;
  margin-bottom: 20px;
}

#header {
  width: 100%;
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
  align-items: center;

  h3 {
    text-align: left;
  }
}

</style>


