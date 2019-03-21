<template>
  <div>

  <div id="header">
    <h3>Basic Info</h3>
    <v-btn v-if="!this.isEditing" small flat id="edit-btn" color="secondary" @click="toggleEditSave"><v-icon>edit</v-icon></v-btn>
    <v-btn v-else small flat id="edit-btn" color="secondary" @click="toggleEditSave">Save</v-btn>
  </div>

  <v-card id="basic-info">

    <div class="profile-item"> 
      <div class="item-signifier">
        <b>First Name</b>
      </div>
      
      <div class="item-value">
        <span v-if="!this.isEditing">{{ firstName }}</span>
        <v-text-field
          v-else
          v-model="firstName"
          color="secondary"
          @blur="validateFirstName"
          :error-messages="firstNameErrors"
        >
        </v-text-field>
      </div>
    </div>

    <v-divider />


    <div class="profile-item">
      <div class="item-signifier">
        <b>Middle Name</b> 
      </div>

      <div class="item-value">
        <span v-if="!this.isEditing"> {{ middleName }} </span>
        <v-text-field
          v-else
          v-model="middleName"
          color="secondary"
          @blur="validateMiddleName"
          :error-messages="middleNameErrors"
        >
        </v-text-field>
      </div>
    </div>

    <v-divider />
    


    <div class="profile-item">
      <div class="item-signifier">
        <b >Last Name</b>
      </div>

      <div class="item-value"> 
      <span v-if="!this.isEditing">{{ lastName }}</span>
      <v-text-field
        v-else
        v-model="lastName"
        color="secondary"
        @blur="validateLastName"
        :error-messages="lastNameErrors"
      >
      </v-text-field>
    </div>

    <v-divider />

    <div class="profile-item">
      <div class="item-signifier">
        <b>Date of Birth</b>
      </div>

      <div class="item-value">
        <span v-if="!this.isEditing">{{ dateOfBirth }}</span>
        <input
          v-else
          type="date"
          v-model="dateOfBirth"
        >
      </div>
      </div>
    </div>

    <v-divider />

    <div class="profile-item">

      <div class="item-signifier">
        <b>Gender</b>
      </div>

      <div class="item-value">
        <span v-if="!this.isEditing">{{ gender.genderName }}</span>
        <select v-else v-model="gender.genderName">
          <option>Male</option>
          <option>Female</option>
          <option>Other</option>
        </select>
      </div>
    </div>
  </v-card>
  </div>
</template>
    methods: {
      toggleEditSave = function() {
        alert('poo');
      }
    }
<script>
const superagent = require('superagent');

export default {
  data() {
    return {
      firstNameErrors: [],
      middleNameErrors: [],
      lastNameErrors: [],
      hasInvalidCredentials: false,
      isEditing: false,
      firstName: "Vikas",
      middleName: "Nothing",
      lastName: "Shenoy",
      dateOfBirth: "26/07/1998",
      gender: {
        genderName: "Male"
      },
      "nationalities": [
        {
          "nationalityId": 1,
          "nationalityName": "NZ"
        }
      ],
      "passports": [
        {
          "passportId": 1,
          "passportCountry": "Passports"
        }
      ],
      "travellerTypes": [
        {
          "travellerTypeId": 1,
          "travellerTypeName": "string"
        }
      ]
    };
  },
  methods: {
    toggleEditSave() {
      if (this.isEditing && !this.hasInvalidCredentials) {
        this.sendUserDataToServer();
        this.isEditing = false;
      } else {
        this.isEditing = true;
      }
    },

    async getUserDataFromServer() {
      try {
        const res = await superagent.get('http://localhost:9000/travellers/1');
        this.firstName = res.body.firstName;
        this.middleName = res.body.middleName;
        this.lastName = res.body.lastName;
        this.dateOfBirth = res.body.dateOfBirth;
        this.gender = res.body.gender;
      } catch (err) {
        console.info(`Error getting user profile data, not updating any data: ${err}`);
      }
    },

    async sendUserDataToServer() {
      try {
        console.log("INSERT PATCH REQUEST HERE");
        const res = await superagent.patch('http://localhost:9000/travellers/1')
          .send(
            {firstName: this.firstName,
            middleName: this.middleName,
            lastName: this.lastName,
            dateOfBirth: this.dateOfBirth,
            gender: this.gender
          }).then(console.log("Successfully sent user data."));
      } catch(err) {
        console.info(`Error saving user profile data: ${err}`);
      }
    },

    validateFirstName() {
      if (!this.firstName) {
        this.firstNameErrors = ["Name is required"];
        this.hasInvalidCredentials = true;
      } else if (/\d/.test(this.firstName)) {
        this.firstNameErrors = ["No numbers allowed"];
        this.hasInvalidCredentials = true;
      } else {
        this.firstNameErrors = [];
        this.hasInvalidCredentials = false;
      }
      return this.firstNameErrors.length === 0;
    },

    validateMiddleName() {
      if (!this.middleName || /\d/.test(this.middleName)) {
        this.middleNameErrors = ["Name is required"];
        this.hasInvalidCredentials = true;
      } else if (/\d/.test(this.middleName)) {
        this.middleNameErrors = ["No numbers allowed"];
        this.hasInvalidCredentials = true;
      } else {
        this.middleNameErrors = [];
        this.hasInvalidCredentials = false;
      }
      return this.middleNameErrors.length === 0;
    },

    validateLastName() {
      if (!this.lastName || /\d/.test(this.lastname)) {
        this.lastNameErrors = ["Name is required"];
        this.hasInvalidCredentials = true;
      } else if (/\d/.test(this.lastName)) {
        this.lastNameErrors = ["No numbers allowed"];
        this.hasInvalidCredentials = true;
      } else {
        this.lastNameErrors = [];
        this.hasInvalidCredentials = false;
      }
      return this.lastNameErrors.length === 0;
    }
    
  },

  mounted: function() {
    // will run after the component is rendered
    console.log('Component\'s been mounted');
    this.getUserDataFromServer();
  }
}
</script>
<style lang="scss" scoped>
  h3 {
    font-size: 1.2rem;
  }

  b {
    font-size: 1.05rem;
  }
  
  #basic-info {
    padding: 10px;
  }

  .profile-item {
    margin-top: 7px;
    margin-bottom: 7px;
  }

  .item-signifier {
    display: inline-block;
    width: 50%;
  }

  .item-value {
    display: inline-block;
    width: 50%;
  }

  #basic-info-header {
    width: 40%;
    display: inline-block;

    h3 {
      margin-bottom: 0 !important;
    }
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