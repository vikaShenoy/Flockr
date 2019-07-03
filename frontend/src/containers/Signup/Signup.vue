<template>
  <div id="signup-container">
  <v-card
    id="signup"
    class="col-lg-6 offset-lg-3"
  >

    <h2>Sign Up</h2>

    <v-text-field
      v-model="firstName"
      color="secondary"
      label="First Name"
      @blur="validateFirstName()"
      :error-messages="firstNameErrors"
      :maxlength="50"
    ></v-text-field>

    <v-text-field
      v-model="lastName"
      color="secondary"
      label="Last Name"
      @blur="validateLastName()"
      :error-messages="lastNameErrors"
      :maxlength="50"
    >
    </v-text-field>

    <v-text-field
      v-model="email"
      color="secondary"
      label="Email"
      @blur="validateEmail()"
      :error-messages="emailErrors"
      autocomplete="off"
      :maxlength="320"
    >
    </v-text-field>


    <v-text-field
      v-model="password"
      type="password"
      color="secondary"
      label="Password"
      @blur="validatePassword()"
      :error-messages="passwordErrors"
      :maxlength="50"
    ></v-text-field>

    <v-text-field
      v-model="confirmPassword"
      type="password"
      color="secondary"
      label="Confirm Password"
      @blur="validateConfirmPassword()"
      :error-messages="confirmPasswordErrors"
      :maxlength="50"
      v-on:keyup.enter="signup"
    ></v-text-field>

    <v-btn
      color="secondary"
      depressed
      class="col-sm-4 offset-sm-4" 
      @click="signup()"
    >Sign Up</v-btn>
  </v-card>

  </div>
</template>

<script>

import { signup, emailTaken } from "./SignupService.js";
import { validate } from "email-validator";

export default {
  name: "sign-up",
  data() {
    return {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      confirmPassword: "",
      firstNameErrors: [],
      lastNameErrors: [],
      emailErrors: [],
      passwordErrors: [],
      confirmPasswordErrors: []
    };
  },
  methods: {
    
    /**
     * Checks if the first name field is empty and renders error if it is
     * @returns {number} If there are errors or not
     */
    validateFirstName() {
      this.firstNameErrors = [];

      if (!this.firstName) {
        this.firstNameErrors = ["First Name is required"]; 
      } else {
        this.lastNameErrors = [];
      }
      return this.firstNameErrors.length === 0;
    },
    /**
     * Set all fields and errors to empty.
     */
    clearData() {
      this.firstName =  "",
      this.lastName =  "",
      this.email =  "",
      this.password =  "",
      this.confirmPassword = "",
      this.firstNameErrors = [],
      this.lastNameErrors = [],
      this.emailErrors = [],
      this.passwordErrors = [],
      this.confirmPasswordErrors = []
    },
    /**
     * Checks if the last name field is empty and renders error if it is
     * @returns {boolean} True if there are no errors, false otherwise
     */
    validateLastName() {
      this.lastNameErrors = [];

      if (!this.lastName) {
        this.lastNameErrors = ["Last Name is required"];
      } else {
        this.lastNameErrors = [];
      }
      return this.lastNameErrors.length === 0;
    },
    /**
     * Checks the f ollowing errors in order and renders if an error exists
     * - Checks if the email is blank 
     * - Checks if the email is not valid
     * - Checks if the email is taken 
     * @returns {boolean} True if there are no errors, false otherwise
     */
    async validateEmail() {
      if (!this.email) {
        this.emailErrors = ["Email is required"];
      } else if (!validate(this.email)) {
        this.emailErrors = ["Email is not valid"]; 
      } else if (await emailTaken(this.email)) {
        this.emailErrors = ["Email is already taken"];
      } else {
        this.emailErrors = [];
      }
      return this.emailErrors.length === 0;
    },
    /**
     * Checks the following errors and renVikasders and renders if an error exists
     * - Checks if password is blank
     * - Checks if password and confirm password don't match
     * @returns {Promise<boolean>} True if there are no errors, false otherwise 
     */
    validatePassword() {

      if (!this.password) {
        this.passwordErrors = ["Password is required"];
      } else if (this.password.length < 6) {
        this.passwordErrors = ["Password has to be atleast 6 characters long"];
      } else if (this.confirmPassword && (this.password !== this.confirmPassword)) {
        this.passwordErrors = ["Passwords are not identical"];
        this.confirmPasswordErrors = ["Passwords are not identical"];
      } else {
        this.passwordErrors = [];
        this.confirmPasswordErrors = [];
      }
      return this.passwordErrors.length === 0;
    },
    /**
     * Checks if the following errors and renders if an error exists
     * - Checks if confirm password is blank 
     * - Checks if password and confirm password don't match
     * @returns {number} True if there are no errors, false otherwise
     */
    validateConfirmPassword() {

      if (!this.confirmPassword) {
        this.confirmPasswordErrors = ["Confirm Password is required"];
      } else if (this.confirmPassword.length < 6) {
        this.confirmPasswordErrors = ["Confirm Password has to be at least 6 characters"];
      } else if (this.password && (this.password !== this.confirmPassword)) {
        this.passwordErrors = [];
        this.confirmPasswordErrors = ["Passwords are not identical"];
        this.passwordErrors = ["Passwords are not identical"];
      } else {
        this.passwordErrors = []; 
        this.confirmPasswordErrors = [];
      }

      return this.confirmPasswordErrors.length === 0;
    },
    /**
     * Validates email and password fields
     * @returns {Promise<boolean>} - True is fields are valid, false otherwise
     */
    async validate() {
      const fieldPromises = [
        this.validateFirstName(),
        this.validateLastName(),
        this.validateEmail(),
        this.validatePassword(),
        this.validateConfirmPassword()
      ];

      const fieldResults = await Promise.all(fieldPromises);
      return fieldResults.every(fieldResult => fieldResult);
    },
    /**
     * Check if fields are valid. Send a request to sign the user up if so.
     */
    async signup() {
      const validFields = await this.validate();
      if (!validFields) return;
      try {
        const { token, userId } = await signup({
          firstName: this.firstName,
          lastName: this.lastName,
          email: this.email,
          password: this.password,
        });

        // Only set fields and redirect if they are
        // NOT currently logged in. (Useful for admin panel).
        if (!localStorage.getItem("authToken")) {
          localStorage.setItem("authToken", token);
          localStorage.setItem("userId", userId);

          /* Used so if user is admin and wants to view as another user,
          then we'll know what ID to go back to once they are done
          */
          localStorage.setItem("ownUserId", userId);
          this.$router.push(`/profile/${userId}`);
        } else {
          this.clearData();
          this.$emit('exit', true);
        }
      } catch (e) {
        console.log(e);
      }
    }
  }
};
</script>


<style lang="scss" scoped>
#signup {
  align-self: center;
  padding: 20px;

  h2 {
    text-align: center;
  }
}

#signup-container {
  display: flex;
  background-image: url("../../assets/background.jpg");
  background-size: cover;
  width: 100%;
}
</style>


