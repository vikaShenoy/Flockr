<template>
  <v-dialog v-model="this.showForm" persistent max-width="600px">
      <v-card>
        <v-card-title>
          <span class="headline">Editing user: {{this.fullUserName}}</span>
        </v-card-title>
        <v-card-text>
          <v-container grid-list-md>
            <v-layout wrap>
              <v-flex xs12 sm6 md4>
                <v-text-field label="First name*" required :value="this.user.firstName"></v-text-field>
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field label="Middle name" :value="this.user.middleName" hint="you may leave this empty"></v-text-field>
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field
                  label="Last name*"
                  :value="this.user.lastName"
                  required
                ></v-text-field>
              </v-flex>
              <v-flex xs12>
                <v-text-field label="Email*" :value="this.user.email" required></v-text-field>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="this.travellerTypeNames"
                  label="Traveller types"
                  multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="['NOTE: not yet populated from user prop']"
                  label="Roles"
                  multiple
                ></v-autocomplete>
              </v-flex>
            </v-layout>
          </v-container>
          <small>* indicates required field</small>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" flat @click="dismissForm">Cancel</v-btn>
          <v-btn color="blue darken-1" flat @click="submitForm">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
</template>

<script>
export default {
  props: {
    showForm: {
      type: Boolean,
      required: true
    },
    userId: {
      type: Number,
      required: true
    },
    user: {
      "userId": Number,
      "firstName": String,
      "middleName": String,
      "lastName": String,
      "dateOfBirth": String,
      "email": String,
      "gender": String,
      "nationalities": [
        {
          "nationalityId": Number,
          "nationalityName": String
        }
      ],
      "passports": [
        {
          "passportId": Number,
          "passportCountry": String
        }
      ],
      "travellerTypes": [
        {
          "travellerTypeId": Number,
          "travellerTypeName": String
        }
      ],
      "timestamp": String,
    }
  },
  methods: {
    dismissForm: function() {
      this.$emit('dismissForm');
    },
    submitForm: function() {
      this.$emit('submitForm');
    }
  },
  computed: {
    fullUserName: function() {
      return `${this.user.firstName} ${this.user.middleName ? this.user.middleName : ''} ${this.user.lastName}`;
    },
    travellerTypeNames: function() {
      return this.user.travellerTypes.map((travellerType) => travellerType.travellerTypeName);
    }
  },
  mounted() {
    // TODO: get the user's data from the API to populate form

  }
}
</script>

