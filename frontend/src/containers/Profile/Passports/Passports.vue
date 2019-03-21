<template>
  <div>
    <div id="header">
      <h3>Passports</h3>
      <v-btn v-if="!this.isEditing" small flat id="edit-btn" color="secondary" @click="toggleEditSave"><v-icon>edit</v-icon></v-btn>
      <v-btn v-else small flat id="edit-btn" color="secondary" @click="toggleEditSave">Save</v-btn>
    </div>
    

    <v-card id="passports">
      <v-chip
        v-for="passport in userPassports"
        v-bind:key="passport.passportId"
        color="primary"
        text-color="white"
      >{{ passport.passportCountry }}</v-chip>
    </v-card>
  </div>
</template>

<script>
export default {
  // otherNationalities specifies nationalities that a user doesn't have
  data() {
    return {
      // These would be retreived from the request
      allPassports: [
        {
          passportId: 1,
          passportCountry: "NZ"
        },
        {
          passportId: 2,
          passportCountry: "Australia"
        }
      ]
    };
  },
  props: ["userPassports"]
}
</script>

<style lang="scss" scoped>
#passports {
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

</style>


