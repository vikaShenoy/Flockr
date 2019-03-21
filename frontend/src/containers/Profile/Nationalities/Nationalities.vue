<template>
  <div>

  <div id="header">
    <h3>Nationalities</h3>
    <v-btn small flat id="edit-btn" color="secondary" @click="toggleEditSave">
      <v-icon v-if="!isEditing">edit</v-icon>
      <span v-else>Save</span>
      </v-btn>
  </div>


    <v-card id="nationalities">
      <div v-if="!isEditing">
        <v-chip
          v-for="nationality in userNationalities"
          v-bind:key="nationality.nationalityId"
          color="primary"
          text-color="white"
        >{{ nationality.nationalityCountry }}</v-chip>
      </div>

      <v-combobox
        v-else
        v-model="userNat"
        :items="this.allNationalities"
        :item-text="getNationalityText"
        label="Your favorite hobbies"
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

import superagent from "superagent";
import { endpoint } from '../../../utils/endpoint';

export default {
  // otherNationalities specifies nationalities that a user doesn't have

  mounted() {
    this.getNationalities();
  },

  data() {
    return {
      // These would be retreived from the request
      userNat: [...this.userNationalities],
      allNationalities: [],
      isEditing: false
    };
  },

  methods: {
    async getNationalities() {
      const res = await superagent.get(endpoint('/travellers/nationalities'));
      console.log(res.body);
      this.allNationalities = res.body;
    },

    async toggleEditSave() {
      if (this.isEditing) {
        let nationalityIds = this.getNationalityIds;
        console.log(this.userNat);
        const res = await superagent.patch(endpoint('/travellers/7'))
                                    .set('Authorization', localStorage.getItem('authToken'))
                                    .send({nationalities: nationalityIds});
        
        this.$emit('update:userNationalities', this.userNat);
          
      }
      this.isEditing = !this.isEditing;
      
    },

    getNationalityText: item => item.nationalityCountry,

    remove (item) {
      this.userNat.splice(this.userNat.indexOf(item), 1);
      this.userNat = [...this.userNat];
    }
  },

  computed: {
    getNationalityNames() {
      return this.userNationalities.map((userNationality => userNationality.nationalityCountry));
    },
    getAllNationalityNames() {
      return this.allNationalities.map((nationality => nationality.nationalityCountry));
    },
    getNationalityIds() {
      return this.userNat.map(nationality => nationality.nationalityId);
    }
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


