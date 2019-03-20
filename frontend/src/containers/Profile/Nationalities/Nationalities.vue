<template>
  <div>

  <div id="header">
    <h3>Nationalities</h3>
    <v-btn v-if="!this.isEditing" small flat id="edit-btn" color="secondary" @click="toggleEditSave"><v-icon>edit</v-icon></v-btn>
    <v-btn v-else small flat id="edit-btn" color="secondary" @click="toggleEditSave">Save</v-btn>
  </div>


    <v-card id="nationalities">
      <v-chip
        v-for="nationality in userNationalities"
        v-bind:key="nationality.nationalityId"
        color="primary"
        text-color="white"
      >{{ nationality.nationalityName }}</v-chip>
    </v-card>
  </div>
</template>

<script>
export default {
  // otherNationalities specifies nationalities that a user doesn't have
  data() {
    return {
      // These would be retreived from the request
      allNationalities: [
        {
          nationalityId: 1,
          nationalityName: "NZ"
        },
        {
          nationalityId: 2,
          nationalityName: "Australia"
        }
      ]
    };
  },
  props: ["userNationalities"]
}
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

</style>


