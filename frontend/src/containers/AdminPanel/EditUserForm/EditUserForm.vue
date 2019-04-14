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
                <v-text-field
                  label="First name *"
                  required
                  :value="this.initialUserData.firstName"
                  v-on:input="addChange('firstName', $event)"
                />
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field
                  label="Middle name"
                  :value="this.initialUserData.middleName"
                  v-on:input="addChange('middleName', $event)"
                  hint="you may leave this empty"
                />
              </v-flex>
              <v-flex xs12 sm6 md4>
                <v-text-field
                  label="Last name *"
                  :value="this.initialUserData.lastName"
                  v-on:input="addChange('lastName', $event)"
                  required
                />
              </v-flex>
              <v-flex xs12>
                <v-text-field
                  label="Email *"
                  :value="this.initialUserData.email"
                  v-on:input="addChange('email', $event)"
                  required
                />
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
          <v-spacer />
          <v-btn color="blue darken-1" flat @click="dismissForm">Cancel</v-btn>
          <v-btn color="blue darken-1" flat @click="submitForm">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
</template>

<script>
export default {
  props: {
    initialUserData: {
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
    },
    showForm: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      changes: {
        // contains changes to be pushed to make a patch on the user
      }
    }
  },
  methods: {
    dismissForm: function() {
      this.$emit('dismissForm');
    },
    submitForm: function() {
      this.$emit('submitForm', this.changes);
    },
    // store the patches we want to make to the user for when the form is submitted
    // NOTE: if you are unsure about how this works, open up the component in Vue Dev Tools and look
    // at the object where changes are stored as you make changes to the fields
    addChange: function(fieldKey, fieldValue) {
      this.changes.userId = this.initialUserData.userId; // make sure there's always a user id
      this.changes[fieldKey] = fieldValue; // store the change
    }
  },
  computed: {
    fullUserName: function() {
      return `${this.initialUserData.firstName} ${this.initialUserData.middleName ? this.initialUserData.middleName : ''} ${this.initialUserData.lastName}`;
    },
    travellerTypeNames: function() {
      return this.initialUserData.travellerTypes.map((travellerType) => travellerType.travellerTypeName);
    }
  },
}
</script>

