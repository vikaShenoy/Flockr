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
                  :items="allTravellerTypeNames"
                  :value="this.initialUserTravellerTypeNames"
                  label="Traveller types"
                  multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="allUserRoleTypes"
                  :value="initialUserRoleTypes"
                  label="User roles"
                  multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="allPassportCountries"
                  :value="initialUserPassportCountries"
                  label="Passports"
                  multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="allNationalityNames"
                  :value="initialUserNationalityNames"
                  label="Nationalities"
                  multiple
                ></v-autocomplete>
              </v-flex>
              <v-flex xs12 sm6>
                <v-autocomplete
                  :items="['Female', 'Male', 'Other']"
                  label="Gender"
                  :value="initialUserData.gender"
                ></v-autocomplete>
              </v-flex>

              <v-flex xs12 sm6>
                <v-menu ref="dateMenu" v-model="dateMenu" :close-on-content-click="false" :nudge-right="40"
                  :return-value.sync="dateOfBirth" lazy transition="scale-transition" offset-y full-width>
                  <template v-slot:activator="{ on }">
                    <v-text-field class="edit-field" v-model="dateOfBirth" readonly v-on="on" :rules="dateRules">
                    </v-text-field>
                  </template>
                  <v-date-picker color="secondary" ref="picker" :max="currentDate" v-model="dateOfBirth" no-title
                    scrollable>
                    <v-spacer></v-spacer>
                    <v-btn flat color="primary" @click="dateMenu = false">Cancel</v-btn>
                    <v-btn flat color="primary" @click="$refs.dateMenu.save(dateOfBirth)">OK</v-btn>
                  </v-date-picker>
                </v-menu>
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
import superagent from 'superagent';
import {endpoint} from '../../../utils/endpoint';
import moment from 'moment';

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
      "roles": [
        {
          "roleId": Number,
          "roleType": String
        }
      ],
      "timestamp": Number,
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
      },
      allNationalities: [],
      allPassports: [],
      allTravellerTypes: [],
      allUserRoles: [],
      dateMenu: false, // don't show the menu by default
      dateOfBirth: null,
      currentDate: moment().format("YYYY-MM-DD")
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
    },
    getAllNationalities: async function() {
      try {
        const res = await superagent.get(endpoint('/users/nationalities'));
        this.allNationalities = res.body;
      } catch(err) {
        console.error(`Could not get all valid nationalities: ${err}`);
      }
    },
    getAllPassports: async function() {
      try {
        const res = await superagent.get(endpoint('/users/passports'));
        this.allPassports = res.body;
        console.log('All passports: ', this.allPassports);
      } catch(err) {
        console.error(`Could not get all valid passports: ${err}`);
      }
    },
    getAllTravellerTypes: async function() {
      try {
        const res = await superagent.get(endpoint('/users/types')).set("Authorization", localStorage.getItem("authToken"));
        this.allTravellerTypes = res.body;
      } catch(err) {
        console.error(`Could not get all valid traveller types: ${err}`);
      }
    },
    getAllUserRoles: async function() {
      try {
        const res = await superagent.get(endpoint('/users/roles')).set("Authorization", localStorage.getItem("authToken"));
        this.allUserRoles = res.body;
      } catch(err) {
        console.error(`Could not get all user roles: ${err}`);
      }
    }
  },
  computed: {
    fullUserName: function() {
      return `${this.initialUserData.firstName} ${this.initialUserData.middleName ? this.initialUserData.middleName : ''} ${this.initialUserData.lastName}`;
    },
    initialUserTravellerTypeNames: function() {
      return this.initialUserData.travellerTypes.map((travellerType) => travellerType.travellerTypeName);
    },
    initialUserNationalityNames: function() {
      return this.initialUserData.nationalities.map((nationality) => nationality.nationalityName);
    },
    initialUserPassportCountries: function() {
      return this.initialUserData.passports.map((passport) => passport.passportCountry);
    },
    initialUserRoleTypes: function() {
      return this.initialUserData.roles.map((role) => role.roleType);
    },
    allNationalityNames: function() {
      return this.allNationalities.map((nationality) => nationality.nationalityName);
    },
    allPassportCountries: function() {
      return this.allPassports.map((passport) => passport.passportCountry);
    },
    allUserRoleTypes: function() {
      return this.allUserRoles.map((role) => role.roleType);
    },
    allTravellerTypeNames: function() {
      return this.allTravellerTypes.map((travellerType) => travellerType.travellerTypeName);
    }
  },
  mounted() {
    this.getAllUserRoles();
    this.getAllNationalities();
    this.getAllPassports();
    this.getAllTravellerTypes();
  }
}
</script>

