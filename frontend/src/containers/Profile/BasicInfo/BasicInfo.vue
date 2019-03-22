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
        <span v-if="!this.isEditing">{{ userProfile.firstName }}</span>
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
        <span v-if="!this.isEditing"> {{ userProfile.middleName }} </span>
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
      <span v-if="!this.isEditing">{{ userProfile.lastName }}</span>
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
        <span v-if="!this.isEditing">{{ userProfile.dateOfBirth }}</span>
          <v-menu
            v-else
            ref="dateMenu"
            v-model="dateMenu"
            :close-on-content-click="false"
            :nudge-right="40"
            :return-value.sync="dateOfBirth"
            lazy
            transition="scale-transition"
            offset-y
            full-width
          >
        <template v-slot:activator="{ on }">
          <v-text-field
            v-model="dateOfBirth"
            label="Select a date"
            @blur="validateDate"
            readonly
            v-on="on"
            :error-messages="dateOfBirthErrors"
          ></v-text-field>
        </template>
        <v-date-picker v-model="dateOfBirth" no-title scrollable>
          <v-spacer></v-spacer>
          <v-btn flat color="primary" @click="dateMenu = false">Cancel</v-btn>
          <v-btn flat color="primary" @click="$refs.dateMenu.save(dateOfBirth)">OK</v-btn>
        </v-date-picker>
      </v-menu>
      </div>
      </div>
    </div>

    <v-divider />

    <div class="profile-item">

      <div class="item-signifier">
        <b>Gender</b>
      </div>

      <div class="item-value">
        <span v-if="!this.isEditing">{{ userProfile.gender }}</span>
        <v-select v-else @blur="validateGender" :error-messages="genderErrors" v-model="gender" :items="genderOptions">
        </v-select>
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
import superagent from 'superagent';

export default {
  props: ["userProfile"],
  data() {
    return {
      isEditing: false,
      dateMenu: false,
      firstName: "",
      lastName: "",
      middleName: "",
      dateOfBirth: "",
      gender: "",
      firstNameErrors: [],
      middleNameErrors: [],
      lastNameErrors: [],
      dateOfBirthErrors: [],
      genderErrors: [],
      hasInvalidCredentials: false,
      isEditing: false,
      genderOptions: ["Male", "Female", "Other"]
    };
  },
  methods: {
    /**
     * Called when use saves edited details. Toggles edit state. Sends a patch request
     * to server, with user's data.
     */
    toggleEditSave() {
      
      if (this.isEditing) {
        const validFields = this.validateFields();

        if (!validFields) {
          return;
        }

        this.sendUserDataToServer();
        const userProfile = this.userProfile;

        userProfile.firstName = this.firstName;
        userProfile.middleName = this.middleName;
        userProfile.lastName = this.lastName;
        userProfile.dateOfBirth = this.dateOfBirth;

        userProfile.gender = this.gender;

        this.$emit('update:userProfile', userProfile);
        
        this.isEditing = false;
      } else {
        this.isEditing = true;
      }
    },

    /**
     * Sends user data to a server in a patch request. 
     */
    async sendUserDataToServer() {
      try {
        const userId = localStorage.getItem("userId");
        const date = this.dateOfBirth;
        

        const res = await superagent.patch(`http://localhost:9000/api/travellers/${userId}`)
          .set("Authorization", localStorage.getItem("authToken"))
          .send(
            {firstName: this.firstName,
            middleName: this.middleName,
            lastName: this.lastName,
            dateOfBirth: this.dateOfBirth,
            gender: this.gender
          }).then(console.log("Successfully sent user data."));
      } catch(err) {
        console.log(err);
        console.info(`Error saving uservalidateGenders profile data: ${err}`);
      }
    },
    /**
     * Checks whether all the form fields have been filled in correctly.
     * Returns true if all fields have no errors.
     */
    validateFields() {
      const fields = [
        this.validateFirstName(),
        this.validateLastName(),
        this.validateMiddleName(),
        this.validateDate(),
        this.validateGender()
      ];

      return fields.every(field => field);
    },

    /**
     * Checks if the first name the user has entered in to the edit 
     * field is non empty and non-numeric. Sets error messages
     * Returns a boolean for whether the name has errors or not. 
     */
    validateFirstName() {
      if (!this.firstName) {
        this.firstNameErrors = ["Name is required"];
      } else if (/\d/.test(this.firstName)) {
        this.firstNameErrors = ["No numbers allowed"];
      } else {
        this.firstNameErrors = [];
      }
      return this.firstNameErrors.length === 0;
    },

    /**
     * Checks if the middle name the user has entered in to the edit 
     * field is non empty and non-numeric. Sets error messages.
     * Returns a boolean for whether the name has errors or not. 
     */
    validateMiddleName() {
      if (!this.middleName) {
        this.middleNameErrors = ["Name is required"];
      } else if (/\d/.test(this.middleName)) {
        this.middleNameErrors = ["No numbers allowed"];
      } else {
        this.middleNameErrors = [];
      }
      return this.middleNameErrors.length === 0;
    },

    /**
     * Checks if the last name the user has entered in to the edit 
     * field is non empty and non-numeric. Sets error messages.
     * Returns a boolean for whether the name has errors or not. 
     */
    validateLastName() {
      if (!this.lastName) {
        this.lastNameErrors = ["Name is required"];
      } else if (/\d/.test(this.lastName)) {
        this.lastNameErrors = ["No numbers allowed"];
      } else {
        this.lastNameErrors = [];
      }
      return this.lastNameErrors.length === 0;
    },

    /**
     * Checks if date is non-empty. Returns true if so.
     */
    validateDate() {
      console.log(1);
      if (!this.dateOfBirth) {
        this.dateOfBirthErrors = ["Date is required"];
      } else {
        this.dateOfBirthErrors = [];
      }
      return this.dateOfBirthErrors.length === 0;
    },
    /**
     * Checks if gender is non-empty. Returns true if so.
     */
    validateGender() {
      if (!this.gender) {
        this.genderErrors = ["Gender is required"];
      } else {
        this.genderErrors = [];
      }
      return this.genderErrors.length === 0;
    },
    /**
     * Sets the fields so that when edit is clicked, they 
     * still display the user's info.
     */
    setEditState() {
      this.firstName = this.userProfile.firstName;
      this.middleName = this.userProfile.middleName;
      this.lastName = this.userProfile.lastName;
      this.dateOfBirth = this.userProfile.dateOfBirth;
      this.gender = this.userProfile.gender;
    }
  },

  mounted: function() {
    this.setEditState();
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